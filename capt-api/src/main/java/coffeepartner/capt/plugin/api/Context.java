package coffeepartner.capt.plugin.api;

import coffeepartner.capt.plugin.api.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public interface Context {

    /**
     * @return variant name of this build
     */
    String getVariantName();

    /**
     * @param clazz class
     * @return the logger
     */
    Logger getLogger(Class<?> clazz);

    /**
     * @return thread pool to do computation jobs
     */
    ForkJoinPool getComputation();

    /**
     * @return thread pool to do io jobs
     */
    ExecutorService getIo();
}
