package yo.dbunitcli.compare;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CompareSetting {

    private Map<String, List<String>> byName = Maps.newHashMap();

    private Map<String, List<String>> pattern = Maps.newHashMap();

    public CompareSetting(Builder builder) {
        this.byName.putAll(builder.byName);
        this.pattern.putAll(builder.pattern);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> get(String tableName) {
        if (this.byName.containsKey(tableName)) {
            return this.byName.get(tableName);
        }
        return this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(Lists.newArrayList());
    }

    public int byNameSize() {
        return this.byName.size();
    }

    public static class Builder {
        private Map<String, List<String>> byName = Maps.newHashMap();

        private Map<String, List<String>> pattern = Maps.newHashMap();

        public CompareSetting build() {
            return new CompareSetting(this);
        }

        public Builder add(Strategy strategy, String name, List<String> keys) {
            if (strategy == Strategy.BY_NAME) {
                return this.addByName(name, keys);
            } else if (strategy == Strategy.PATTERN) {
                return this.addPattern(name, keys);
            }
            return this;
        }

        public Builder addByName(String name, List<String> keys) {
            this.byName.put(name, keys);
            return this;
        }

        public Builder addPattern(String name, List<String> keys) {
            this.pattern.put(name, keys);
            return this;
        }
    }

    public enum Strategy {
        BY_NAME, PATTERN
    }
}
