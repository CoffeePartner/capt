package bean.hook;

import javax.annotation.Nullable;

public class TryCatchInfo {

    @Nullable
    public String regex;

    public String type;

    public int priority;

    public MethodLocator locator;
}
