package coffeepartner.capt.sample.safecatcher;

import java.io.IOException;

import coffeepartner.capt.plugin.api.Capt;
import coffeepartner.capt.plugin.api.Plugin;
import coffeepartner.capt.plugin.api.annotations.Def;
import coffeepartner.capt.plugin.api.process.AnnotationProcessor;

/**
 * Created by luoye on 2019/1/6.
 */
@Def
public class TestPlugin extends Plugin<Capt> {
    @Override
    public void onCreate(Capt capt) throws IOException, InterruptedException {

    }

    @Override
    public AnnotationProcessor onProcessAnnotations() {
        return new AnnotationProcessor();
    }
}
