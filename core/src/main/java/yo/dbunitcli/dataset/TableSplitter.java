package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TableSplitter {

    public static final TableSplitter NONE = new TableSplitter(new Builder());

    private final Map<String, List<SplitResult>> byNames = new HashMap<>();

    private final Map<String, List<SplitResult>> pattern = new HashMap<>();

    public TableSplitter(final Builder builder) {
        this.byNames.putAll(builder.getByNames());
        this.pattern.putAll(builder.getPattern());
    }

    public TableSplitter apply(final Consumer<Builder> tableSplitterEdit) {
        final TableSplitter.Builder builder = this.builder();
        tableSplitterEdit.accept(builder);
        return builder.build();
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public static class Builder {

        private final Map<String, List<SplitResult>> byNames = new HashMap<>();

        private final Map<String, List<SplitResult>> pattern = new HashMap<>();

        public Builder add(final TableSplitter tableSplitter) {
            this.byNames.putAll(tableSplitter.byNames);
            this.pattern.putAll(tableSplitter.pattern);
            return this;
        }

        public TableSplitter build() {
            return new TableSplitter(this);
        }

        public Builder addByNames(final String targetName, final List<String> filters, final String resultName) {
            return this.addSplit(targetName, filters, resultName, this.byNames);
        }

        public Builder addPattern(final String targetPattern, final List<String> filters, final String resultName) {
            return this.addSplit(targetPattern, filters, resultName, this.pattern);
        }

        public Builder addSplit(final String targetName, final List<String> filters, final String resultName, final Map<String, List<SplitResult>> byNames) {
            final SplitResult splitResult = new SplitResult(new RowFilter(filters), resultName);
            if (byNames.containsKey(targetName)) {
                byNames.get(targetName).add(splitResult);
            } else {
                final List<SplitResult> results = new ArrayList<>();
                results.add(splitResult);
                byNames.put(targetName, results);
            }
            return this;
        }

        public Map<String, List<SplitResult>> getByNames() {
            return this.byNames;
        }

        public Map<String, List<SplitResult>> getPattern() {
            return this.pattern;
        }
    }

    static protected class SplitResult {

        private final RowFilter filters;

        private final String resultName;

        public SplitResult(final RowFilter filters, final String resultName) {
            this.filters = filters;
            this.resultName = resultName;
        }
    }
}
