package yo.dbunitcli.application;

import com.google.common.collect.Maps;

import java.util.Map;

public class Parameter {

    public static final Parameter NONE = new Parameter(0, Maps.newHashMap());

    private final int rowNumber;

    private final Map<String, Object> map = Maps.newHashMap();

    public Parameter(int rowNumber, Map<String, Object> map) {
        this.rowNumber = rowNumber;
        this.map.putAll(map);
    }

    public static Parameter none() {
        return NONE;
    }

    public int getRowNumber() {
        return this.rowNumber;
    }

    public Map<String, Object> getMap() {
        return this.map;
    }
}
