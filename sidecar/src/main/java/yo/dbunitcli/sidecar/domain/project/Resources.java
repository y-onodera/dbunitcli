package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.dataset.DataSourceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record Resources(
        Map<DataSourceType, List<String>> src
        , List<String> jdbc
        , List<String> setting
        , List<String> template
        , List<String> xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<DataSourceType, List<String>> src = new HashMap<>();
        private final List<String> jdbc = new ArrayList<>();
        private final List<String> setting = new ArrayList<>();
        private final List<String> template = new ArrayList<>();
        private final List<String> xlsxSchema = new ArrayList<>();

        public Builder addSrc(final DataSourceType type, final Consumer<List<String>> listConsumer) {
            if (!this.src.containsKey(type)) {
                this.src.put(type, new ArrayList<>());
            }
            listConsumer.accept(this.src.get(type));
            return this;
        }

        public Resources build() {
            return new Resources(new HashMap<>(this.src)
                    , new ArrayList<>(this.jdbc)
                    , new ArrayList<>(this.setting)
                    , new ArrayList<>(this.template)
                    , new ArrayList<>(this.xlsxSchema)
            );
        }
    }
}
