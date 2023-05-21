package yo.dbunitcli.dataset;


import java.util.HashMap;
import java.util.Map;

public class Parameter {

    public static final Parameter NONE = new Parameter(0, new HashMap<>());

    private final Map<String, Object> map = new HashMap<>();

    public Parameter(final int rowNumber, final Map<String, Object> map) {
        this.map.putAll(map);
        this.map.put("rowNumber", rowNumber);
    }

    public static Parameter none() {
        return NONE;
    }

    public Map<String, Object> getMap() {
        return this.map;
    }
}
