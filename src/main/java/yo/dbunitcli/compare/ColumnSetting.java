package yo.dbunitcli.compare;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ColumnSetting {

    private Map<String, List<String>> byName = Maps.newHashMap();

    private Map<String, List<String>> pattern = Maps.newHashMap();

    private List<String> common = Lists.newArrayList();

    public ColumnSetting(Builder builder) {
        this.byName.putAll(builder.byName);
        this.pattern.putAll(builder.pattern);
        this.common.addAll(builder.common);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean includeSetting(String tableName) {
        return this.byName.containsKey(tableName)
                || this.pattern.entrySet().stream().anyMatch(it -> tableName.contains(it.getKey()));
    }

    public List<String> getColumns(String tableName) {
        List<String> result = Lists.newArrayList(this.common);
        if (this.byName.containsKey(tableName)) {
            result.addAll(this.byName.get(tableName));
            return result;
        }
        result.addAll(this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(Lists.newArrayList()));
        return result;
    }

    public int byNameSize() {
        return this.byName.size();
    }

    public static class Builder {
        private Map<String, List<String>> byName = Maps.newHashMap();

        private Map<String, List<String>> pattern = Maps.newHashMap();

        private List<String> common = Lists.newArrayList();

        public ColumnSetting build() {
            return new ColumnSetting(this);
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

        public Builder addCommon(List<String> columns) {
            this.common.addAll(columns);
            return this;
        }
    }

    public enum Strategy {
        BY_NAME, PATTERN
    }
}
