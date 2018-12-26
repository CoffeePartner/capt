package com.dieyidezui.lancet.plugin.util;

import com.android.build.api.transform.*;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.*;

/**
 * This makes faster when IO does not block computation job in ForkJoinPool.
 */
public final class ClassWalker {

    private static final FileTime ZERO = FileTime.fromMillis(0);

    private final GlobalResource resource;
    private final TransformInvocation invocation;

    public ClassWalker(GlobalResource resource, TransformInvocation invocation) {
        this.resource = resource;
        this.invocation = invocation;
    }

    public void visit(boolean incremental, boolean write, Visitor.Factory factory) throws IOException, InterruptedException, TransformException {
        WaitableTasks io = WaitableTasks.get(resource.io());
        invocation.getInputs()
                .forEach(i -> {
                    i.getDirectoryInputs()
                            .forEach(d -> io.submit(new DirectoryTask(d, incremental, write, factory)));
                    i.getJarInputs()
                            .forEach(j -> io.submit(new JarTask(j, incremental, write, factory)));
                });
        io.await();
    }


    public final static class ClassEntry extends InternalEntry {
        public ClassEntry(String className, byte[] bytes) {
            super(className + ".class", bytes);
        }
    }

    static class InternalEntry {

        private final String name;
        private final byte[] bytes;

        InternalEntry(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        /**
         * write ZIP faster without compress!
         */
        final void writeTo(ZipOutputStream zos) throws IOException {
            ZipEntry outEntry = new ZipEntry(name);
            CRC32 crc32 = new CRC32();
            crc32.update(bytes);
            outEntry.setCrc(crc32.getValue());
            outEntry.setMethod(ZipEntry.STORED);
            outEntry.setSize(bytes.length);
            outEntry.setCompressedSize(bytes.length);
            outEntry.setLastAccessTime(ZERO);
            outEntry.setLastModifiedTime(ZERO);
            outEntry.setCreationTime(ZERO);
            zos.putNextEntry(outEntry);
            zos.write(bytes);
        }

        final void writeTo(File root) throws IOException {
            File target = new File(root, name.replace('/', File.separatorChar));
            Files.createParentDirs(target);
            Files.write(bytes, target);
        }
    }


    public interface Visitor {

        /**
         * @param classBytes null for REMOVED
         */
        @Nullable
        ForkJoinTask<ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status);

        interface Factory {
            @Nullable
            Visitor newVisitor(boolean incremental, QualifiedContent content);
        }
    }

    class JarTask implements Callable<Void> {

        private final JarInput jar;
        private final boolean incremental;
        private final boolean write;
        private final Visitor.Factory factory;

        JarTask(JarInput jar, boolean incremental, boolean write, Visitor.Factory factory) {
            this.jar = jar;
            this.incremental = incremental;
            this.write = write;
            this.factory = factory;
        }

        @Override
        public Void call() throws Exception {
            Visitor visitor = factory.newVisitor(jar);
            if (visitor == null) {
                return null;
            }

            // For not changed, it is illegal
            // For removed, we can't read the jar
            if (incremental && (jar.getStatus() == Status.NOTCHANGED || jar.getStatus() == Status.REMOVED)) {
                return null;
            }

            Status status = incremental ? jar.getStatus() : Status.NOTCHANGED;

            ForkJoinPool pool = resource.computation();
            ZipOutputStream zos = null;
            List<Future<ClassEntry>> futures = null;
            ZipFile file = new ZipFile(jar.getFile());

            if (write) {
                File output = invocation.getOutputProvider().getContentLocation(
                        jar.getName(), jar.getContentTypes(), jar.getScopes(), Format.JAR);
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
                futures = new ArrayList<>();
            }

            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    InputStream is = file.getInputStream(entry);
                    byte[] classBytes = ByteStreams.toByteArray(is);
                    is.close();
                    Future<ClassEntry> future = visitor.onVisit(
                            pool,
                            classBytes,
                            name.substring(0, name.length() - 6), // .class = 6
                            status
                    );
                    if (write && future != null) {
                        futures.add(future);
                    }
                } else if (write) { // keep other resources content in jar
                    InputStream is = file.getInputStream(entry);
                    byte[] content = ByteStreams.toByteArray(is);
                    is.close();
                    new InternalEntry(name, content).writeTo(zos);
                }
            }
            Closeables.close(file, true);

            // write class result
            if (write) {
                for (Future<ClassEntry> f : futures) {
                    ClassEntry e = f.get();
                    if (e != null) {
                        e.writeTo(zos);
                    }
                }
                zos.close(); // throw IO if close error
            }
            return null;
        }
    }

    class DirectoryTask implements Callable<Void> {

        private final DirectoryInput d;
        private final boolean incremental;
        private final boolean write;
        private final Visitor.Factory factory;
        private URI base;

        DirectoryTask(DirectoryInput directory, boolean incremental, boolean write, Visitor.Factory factory) {
            this.d = directory;
            this.incremental = incremental;
            this.write = write;
            this.factory = factory;
            this.base = d.getFile().toURI();
        }

        @Override
        public Void call() throws Exception {
            Visitor visitor = factory.newVisitor(d);
            if (visitor == null) {
                return null;
            }

            ForkJoinPool pool = resource.computation();
            List<Future<ClassEntry>> futures = write ? null : new ArrayList<>();

            // just process .class, skip others
            if (!incremental) {
                for (File file : Files.fileTreeTraverser().preOrderTraversal(d.getFile())) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        byte[] bytes = Files.toByteArray(file);
                        ForkJoinTask<ClassEntry> task = visitor.onVisit(pool, bytes, fileToClassName(file), Status.NOTCHANGED);
                        if (futures != null) {
                            futures.add(task);
                        }
                    }
                }
            } else {
                for (Map.Entry<File, Status> entry : d.getChangedFiles().entrySet()) {
                    Status status = entry.getValue();
                    if (status != Status.NOTCHANGED && entry.getKey().getName().endsWith(".class")) {
                        byte[] bytes = status == Status.REMOVED ? null : Files.toByteArray(entry.getKey());
                        ForkJoinTask<ClassEntry> task = visitor.onVisit(pool, bytes, fileToClassName(entry.getKey()), entry.getValue());
                        if (futures != null) {
                            futures.add(task);
                        }
                    }
                }
            }

            if (futures != null) {
                for (Future<ClassEntry> future : futures) {
                    ClassEntry e = future.get();
                    if (e != null) {
                        e.writeTo(d.getFile());
                    }
                }
            }

            return null;
        }

        private String fileToClassName(File target) {
            String path = base.relativize(target.toURI()).toString();
            return path.substring(0, path.length() - 6); // .class = 6
        }
    }
}
