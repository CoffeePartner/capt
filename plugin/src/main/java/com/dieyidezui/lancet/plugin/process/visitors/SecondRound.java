package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.dieyidezui.lancet.plugin.util.ClassWalker;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * Remember that second round doesn't process REMOVED classes directly.
 */
public class SecondRound implements ClassWalker.Visitor.Factory {
    private final Set<String> toRemove;

    public SecondRound(Set<String> toRemove) {
        this.toRemove = toRemove;
    }

    @Nullable
    @Override
    public ClassWalker.Visitor newVisitor(boolean incremental, QualifiedContent content) {
        if (incremental && content instanceof JarInput && ((JarInput) content).getStatus() == Status.REMOVED) {
            return null;
        }
        return;
    }


    class TransformVisitor implements ClassWalker.Visitor {

        @Nullable
        @Override
        public ForkJoinTask<ClassWalker.ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status) {
            if (status != Status.REMOVED && !toRemove.contains(className)) {

            }
            return null;
        }
    }
}
