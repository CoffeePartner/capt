package coffeepartner.capt.plugin.dsl;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class ConfigurableMap extends GroovyObjectSupport {

    private Map<String, Object> properties = new HashMap<>();

    @Override
    public Object invokeMethod(String name, Object args) {
        int length = Array.getLength(args);
        if (length != 1) {
            throw new UnsupportedOperationException("number of argument is illegal: " + length);
        }
        setProperty(name, Array.get(args, 0));
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProperty(String property, Object newValue) {
        Action<ConfigurableMap> action;
        if (newValue instanceof Closure) {
            action = ConfigureUtil.configureUsing((Closure) newValue);
        } else if (newValue instanceof Action) {
            action = (Action<ConfigurableMap>) newValue;
        } else {
            properties.put(property, newValue);
            return;
        }

        Object oldValue = properties.get(property);
        if (oldValue instanceof ConfigurableMap) {
            action.execute((ConfigurableMap) oldValue);
        } else {
            ConfigurableMap map = new ConfigurableMap();
            action.execute(map);
            properties.put(property, map);
        }
    }

    @Override
    public Object getProperty(String property) {
        return properties.get(property);
    }

    public Map<String, Object> toRealMap() {
        Map<String, Object> map = new HashMap<>(properties.size());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getValue() instanceof ConfigurableMap) {
                map.put(entry.getKey(), ((ConfigurableMap) entry.getValue()).toRealMap());
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "ConfigurableMap{" +
                "properties=" + properties +
                '}';
    }


}
