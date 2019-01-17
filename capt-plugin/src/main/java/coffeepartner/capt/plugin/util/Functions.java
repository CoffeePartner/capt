package coffeepartner.capt.plugin.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Functions {

    private Functions() {

    }

    public static <T> Supplier<T> cache(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }


    static class LazySupplier<T> implements Supplier<T> {

        private static Object holder = LazySupplier.class;
        private final AtomicReference<Supplier<T>> ref;
        private T t = (T) holder;

        LazySupplier(Supplier<T> supplier) {
            this.ref = new AtomicReference<>(supplier);
        }

        @Override
        public T get() {
            while (t == holder) {
                Supplier<T> supplier = ref.getAndSet(null);
                if (supplier != null) {
                    t = supplier.get();
                }
            }
            return t;
        }
    }
}
