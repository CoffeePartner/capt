package bean;


import java.util.List;

public class InputInfo {
    public static final int TYPE_DIR = 0;
    public static final int TYPE_JAR = 1;

    public String name;

    public String file;

    public int type;

    public List<String> classes;
}
