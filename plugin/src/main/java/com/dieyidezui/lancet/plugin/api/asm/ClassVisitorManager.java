package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.List;

public final class ClassVisitorManager {

    public  ClassVisitor link(List<LancetClassVisitor> visitors, ClassWriter cw) {
        int sz = visitors.size() - 1;
        if (sz < 0) {
            return cw;
        }
        for (int i = 0; i < sz; i++) {
            visitors.get(i).linkNext(visitors.get(i + 1));
        }
        visitors.get(sz).linkNext(cw);
        return visitors.get(0);
    }

    public  void attach(LancetClassVisitor visitor, TransformContext context) {
        visitor.attach(context);
    }

    public  void detach(LancetClassVisitor visitor) {
        visitor.detach();
    }
}
