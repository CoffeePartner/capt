package com.dieyidezui.lancet.plugin.process.visit;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import jdk.internal.org.objectweb.asm.ClassVisitor;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class VisitDispatcher {

    private final GlobalResource resource;
    private final TransformInvocation invocation;

    public VisitDispatcher(GlobalResource resource, TransformInvocation invocation) {
        this.resource = resource;
        this.invocation = invocation;
    }

    public void visit(boolean incremental, boolean write, VisitorFactory) throws IOException, InterruptedException, TransformException {
        WaitableTasks io = WaitableTasks.get(resource.io());
        ForkJoinPool computation = resource.computation();
        invocation.getInputs()
                .stream()
                .flatMap(i -> Stream.<QualifiedContent>concat(i.getDirectoryInputs().stream(), i.getDirectoryInputs().stream()))
                .forEach(q -> {
                    io.execute(() -> expand(incremental, q, visitor));
                });

        io.await();
        computation.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private void expand(boolean incremental, QualifiedContent q, ClassVisitor visitor) {
        if (q instanceof JarInput) {
            if (incremental) {

            }
        }
    }

    private static class


    public interface VisitorFactory {
        ForkJoinTask<byte[]> newTask(Status status, byte[] classBytes);
    }

    public interface Processor extends BiFunction<Status, byte[], byte[]> {
    }
}
