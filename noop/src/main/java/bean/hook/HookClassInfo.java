package bean.hook;

import javafx.util.Pair;

import java.util.List;
import java.util.Set;

public class HookClassInfo {

    public String className;

    public String belongToInputName;

    public TryCatchInfo tryCatchInfos;

    /**
     * info with extra matched classes
     */
    public List<Pair<InsertInfo, Set<String>>> insertInfos;

    /**
     * same as insert
     */
    public List<Pair<ProxyInfo, Set<String>>> proxyInfos;
}
