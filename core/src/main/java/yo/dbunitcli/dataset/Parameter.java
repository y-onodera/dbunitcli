package yo.dbunitcli.dataset;


import java.util.HashMap;
import java.util.Map;

public class Parameter {

    public static final Parameter NONE = new Parameter(0, new HashMap<>());

    private final Map<String, Object> map = new HashMap<>();

    public static Parameter none() {
        return Parameter.NONE;
    }

    public Parameter(final int rowNumber, final Map<String, Object> map) {
        this.map.put("inputParam", map);
        this.map.put("rowNumber", rowNumber);
    }

    public Map<String, Object> getMap() {
        return this.map;
    }

    public Parameter add(final String key, final Object value) {
        this.map.put(key, value);
        return this;
    }

    public Parameter addAll(final Map<String, Object> other) {
        this.map.putAll(other);
        return this;
    }
}
