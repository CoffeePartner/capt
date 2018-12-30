package coffeepartner.capt.plugin.api.graph;

public enum Status {
    /**
     * The file was not changed since the last build.
     */
    NOT_CHANGED,
    /**
     * The file was added since the last build.
     */
    ADDED,
    /**
     * The file was modified since the last build.
     */
    CHANGED,
    /**
     * The file was removed since the last build.
     */
    REMOVED,

    /**
     * The file is not exists in APK.
     */
    NOT_EXISTS
}
