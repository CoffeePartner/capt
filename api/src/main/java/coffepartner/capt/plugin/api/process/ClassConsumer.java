package coffeepartner.capt.plugin.api.process;

import coffeepartner.capt.plugin.api.hint.Thread;
import coffeepartner.capt.plugin.api.hint.Type;
import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    @Thread(Type.COMPUTATION)
    void accept(ClassNode node);
}
