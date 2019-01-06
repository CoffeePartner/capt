package coffeepartner.capt.plugin.util;

import coffeepartner.capt.plugin.resource.GlobalResource;
import com.android.build.api.transform.*;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This makes faster when IO does not block computation job in ForkJoinPool.
 */
public final class ClassWalker {

    private static final FileTime ZERO = FileTime.fromMillis(0);
    private static final Logger LOGGER = Logging.getLogger(ClassWalker.class);

    private final GlobalResource resource;
    private final TransformInvocation invocation;

    public ClassWalker(GlobalResource resource, TransformInvocation invocation) {
        this.resource = resource;
        this.invocation = invocation;
    }

    public void visit(boolean includeNotChanged, boolean incremental, boolean write, Visitor.Factory factory) throws IOException, InterruptedException, TransformException {
        visit(includeNotChanged, incremental, write, factory, null);
    }

    public void visit(boolean includeNotChanged, boolean incremental, boolean write, Visitor.Factory factory, @Nullable Map<QualifiedContent, Set<String>> targets) throws IOException, InterruptedException, TransformException {
        WaitableTasks io = WaitableTasks.get(resource.io());
        invocation.getInputs()
                .forEach(i -> {
                    i.getDirectoryInputs()
                            .forEach(d -> {
                                if (targets == null) {
                                    io.submit(new DirectoryTask(d, includeNotChanged, incremental, write, factory, null));
                                } else {
                                    Set<String> t = targets.get(d);
                                    if (t != null) {
                                        io.submit(new DirectoryTask(d, includeNotChanged, incremental, write, factory, t));
                                    }
                                }
                            });
                    i.getJarInputs()
                            .forEach(j -> {
                                if (targets == null) {
                                    io.submit(new JarTask(j, includeNotChanged, incremental, write, factory, null));
                                } else {
                                    Set<String> t = targets.get(j);
                                    if (t != null) {
                                        io.submit(new JarTask(j, includeNotChanged, incremental, write, factory, t));
                                    }
                                }
                            });
                });
        io.await();
    }

    public void visitTargets(Visitor.Factory factory, Map<QualifiedContent, Set<String>> targets) throws InterruptedException, TransformException, IOException {
        visit(false, false, true, factory, targets);
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

        // classBytes null for REMOVED
        @Nullable
        ForkJoinTask<ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status);

        interface Factory {
            @Nullable
            Visitor newVisitor(boolean incremental, QualifiedContent content);
        }
    }

    /**
     * For jar, targets is useless. We will transform all classes in jar.
     */
    class JarTask implements Callable<Void> {

        private final JarInput jar;
        private final boolean includeNotChanged;
        private final boolean incremental;
        private final boolean write;
        private final Visitor.Factory factory;
        @Nullable
        private final Set<String> targets;

        JarTask(JarInput jar, boolean includeNotChanged, boolean incremental, boolean write, Visitor.Factory factory, @Nullable Set<String> targets) {
            this.jar = jar;
            this.includeNotChanged = includeNotChanged;
            this.incremental = incremental;
            this.write = write;
            this.factory = factory;
            this.targets = targets;
        }

        @Override
        public Void call() throws Exception {

            Visitor visitor = factory.newVisitor(incremental, jar);
            if (visitor == null) {
                return null;
            }

            // 1. we can't read removed jar anyway
            // 2. incremental && not changed, it is illegal, we skip it
            if (jar.getStatus() == Status.NOTCHANGED) {
                if (incremental && !includeNotChanged) {
                    return null;
                }
            } else if (jar.getStatus() == Status.REMOVED) {
                if (write) {
                    Util.deleteIFExists(invocation.getOutputProvider().getContentLocation(
                            jar.getName(), jar.getContentTypes(), jar.getScopes(), Format.JAR));
                }
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

    /**
     * For directory, targets is good. We can just transform targeted classes.
     */
    class DirectoryTask implements Callable<Void> {

        private final DirectoryInput d;
        private final boolean includeNotChanged;
        private final boolean incremental;
        private final boolean write;
        private final Visitor.Factory factory;
        @Nullable
        private final Set<String> targets;
        private URI base;

        DirectoryTask(DirectoryInput directory, boolean includeNotChanged, boolean incremental, boolean write, Visitor.Factory factory, @Nullable Set<String> targets) {
            this.d = directory;
            this.includeNotChanged = includeNotChanged;
            this.incremental = incremental;
            this.write = write;
            this.factory = factory;
            this.targets = targets;
            this.base = d.getFile().toURI();
        }

        @Override
        public Void call() throws Exception {
            Visitor visitor = factory.newVisitor(incremental, d);
            if (visitor == null) {
                return null;
            }

            if (incremental && d.getChangedFiles().isEmpty()) {
                return null;
            }

            ForkJoinPool pool = resource.computation();
            List<Future<ClassEntry>> futures = !write ? null : new ArrayList<>();

            File outRoot = invocation.getOutputProvider().getContentLocation(d.getName(), d.getContentTypes(), d.getScopes(), Format.DIRECTORY);

            // just process .class, skip others
            if (!incremental) {
                if (d.getFile().exists()) { // we check if directory removed for capt in  full mode & transform in incremental mode
                    for (File file : Files.fileTreeTraverser().preOrderTraversal(d.getFile())) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = fileToClassName(file);
                            if (targets == null || targets.contains(className)) {
                                byte[] bytes = Files.toByteArray(file);
                                ForkJoinTask<ClassEntry> task = visitor.onVisit(pool, bytes, className, Status.NOTCHANGED);
                                if (futures != null && task != null) {
                                    futures.add(task);
                                }
                            }
                        }
                    }
                } else { // clean the directory
                    FileUtils.deleteDirectory(outRoot);
                }
            } else {
                Map<File, Status> changed = d.getChangedFiles();
                if (includeNotChanged) {
                    for (File file : Files.fileTreeTraverser().preOrderTraversal(d.getFile())) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = fileToClassName(file);
                            if (targets == null || targets.contains(className)) {
                                byte[] bytes = Files.toByteArray(file);
                                Status status = changed.get(file);
                                if (status == null) {
                                    status = Status.NOTCHANGED;
                                }
                                ForkJoinTask<ClassEntry> task = visitor.onVisit(pool, bytes, className, status);
                                if (futures != null && task != null) {
                                    futures.add(task);
                                }
                            }
                        }
                    }
                }
                for (Map.Entry<File, Status> entry : d.getChangedFiles().entrySet()) {
                    Status status = entry.getValue();
                    if (entry.getKey().getName().endsWith(".class")) {
                        String className = fileToClassName(entry.getKey());
                        if (targets == null || targets.contains(className)) {
                            byte[] bytes;
                            if (status == Status.REMOVED) {
                                bytes = null;
                                if (write) { // delete the relative out file
                                    Util.deleteIFExists(new File(outRoot, className.replace('/', File.separatorChar) + ".class"));
                                }
                            } else if (includeNotChanged) { // visited
                                continue;
                            } else {
                                bytes = Files.toByteArray(entry.getKey());
                            }
                            ForkJoinTask<ClassEntry> task = visitor.onVisit(pool, bytes, className, entry.getValue());
                            if (futures != null && task != null) {
                                futures.add(task);
                            }
                        }
                    }
                }
            }

            if (futures != null) {
                for (Future<ClassEntry> future : futures) {
                    ClassEntry e = future.get();
                    if (e != null) {
                        e.writeTo(outRoot);
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
