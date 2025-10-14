package yo.dbunitcli.common;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public record Parameter(Map<String, Object> map) {

    public static final Parameter NONE = new Parameter(new HashMap<>()).withRowNumber(0);

    public static Parameter none() {
        return Parameter.NONE;
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(this.map);
    }

    public void forEach(final BiConsumer<String, Object> action) {
        this.map.forEach(action);
    }

    public Object get(final String key) {
        return this.map.get(key);
    }

    public Parameter add(final String key, final Object value) {
        final Parameter result = new Parameter(new HashMap<>(this.map));
        result.map.put(key, value);
        return result;
    }

    public Parameter addAll(final Map<String, ? extends Object> other) {
        final Parameter result = new Parameter(new HashMap<>(this.map));
        result.map.putAll(other);
        return result;
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Iterable<? extends Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public Parameter withRowNumber(final Integer rowNumber) {
        return new Parameter(new HashMap<>(this.map))
                .add("rowNumber", rowNumber);
    }

    public Parameter asInputParam() {
        return new Parameter(new HashMap<>())
                .add("inputParam", new HashMap<>(this.map));
    }
}
