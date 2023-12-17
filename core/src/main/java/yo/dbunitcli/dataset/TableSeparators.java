package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record TableSeparators(Map<String, List<TableSeparator>> byNames,
                              Map<String, List<TableSeparator>> pattern,
                              TableSeparator commonExpressions) {

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new HashMap<>(builder.getByNames()), new HashMap<>(builder.getPattern()), builder.commonExpressions);
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public TableSeparators map(final Consumer<TableSeparators.Builder> editor) {
        final TableSeparators.Builder builder = this.builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public boolean hasAdditionalSetting(final String tableName) {
        return this.getSeparators(tableName)
                .stream()
                .filter(it -> !it.equals(TableSeparator.NONE))
                .toList()
                .size() > 0;
    }

    public Collection<TableSeparator> getSeparators(final String tableName) {
        final Set<TableSeparator> result = new HashSet<>();
        TableSeparator noReName = Optional.ofNullable(this.byNames.get(tableName))
                .orElse(new ArrayList<>())
                .stream()
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.splitter() == TableSplitter.NONE)
                .reduce(TableSeparator.NONE, TableSeparator::add)
                .add(this.pattern.entrySet().stream()
                        .filter(it -> tableName.contains(it.getKey()) || it.getKey().equals("*"))
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .filter(TableSeparator::hasSettings)
                        .filter(it -> it.splitter() == TableSplitter.NONE)
                        .reduce(TableSeparator.NONE, TableSeparator::add)
                );
        if (noReName == TableSeparator.NONE) {
            noReName = this.pattern.entrySet().stream()
                    .filter(it -> tableName.contains(it.getKey()) || it.getKey().equals("*"))
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .filter(TableSeparator::hasSettings)
                    .filter(it -> it.splitter() == TableSplitter.NONE)
                    .reduce(TableSeparator.NONE, TableSeparator::add);
        }
        result.addAll(Optional.ofNullable(this.byNames.get(tableName))
                .orElse(new ArrayList<>())
                .stream()
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.splitter() != TableSplitter.NONE)
                .map(noReName::add)
                .map(this.commonExpressions::add)
                .toList());
        result.addAll(this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()) || it.getKey().equals("*"))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.splitter() != TableSplitter.NONE)
                .map(noReName::add)
                .map(this.commonExpressions::add)
                .toList());
        if (result.size() > 0) {
            return result;
        }
        result.add(noReName.add(this.commonExpressions));
        return result;
    }

    public ComparableTableMapper createMapper(final ITableMetaData metaData) {
        final List<AddSettingTableMetaData> results = this.getAddSettingTableMetaData(metaData);
        if (results.size() == 1) {
            return this.createMapperFrom(results.get(0));
        }
        return new ComparableTableMapperMulti(results.stream()
                .map(this::createMapperFrom)
                .collect(Collectors.toList()));
    }

    private ComparableTableMapper createMapperFrom(final AddSettingTableMetaData results) {
        return new ComparableTableMapperSingle(results);
    }

    private List<AddSettingTableMetaData> getAddSettingTableMetaData(final ITableMetaData metaData) {
        return this.addSetting(metaData).stream()
                .map(it -> {
                    ITableMetaData origin = metaData;
                    AddSettingTableMetaData resultMetaData = it;
                    if (origin.getTableName().equals(resultMetaData.getTableName()) && origin instanceof AddSettingTableMetaData base) {
                        resultMetaData = this.addSetting(origin, base.getTableSeparator().add(resultMetaData.getTableSeparator()));
                    }
                    while (!origin.getTableName().equals(resultMetaData.getTableName())) {
                        origin = resultMetaData;
                        final List<AddSettingTableMetaData> addSetting = this.addSetting(resultMetaData);
                        if (addSetting.size() == 1) {
                            resultMetaData = addSetting.get(0);
                        } else {
                            return addSetting.stream()
                                    .map(this::getAddSettingTableMetaData)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());
                        }
                    }
                    return Collections.singletonList(resultMetaData);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<AddSettingTableMetaData> addSetting(final ITableMetaData originMetaData) {
        return this.getSeparators(originMetaData.getTableName()).stream()
                .map(it -> this.addSetting(originMetaData, it))
                .collect(Collectors.toList());
    }

    private AddSettingTableMetaData addSetting(final ITableMetaData originMetaData, final TableSeparator tableSeparator) {
        final Column[] comparisonKeys = tableSeparator.getComparisonKeys();
        try {
            Column[] primaryKey = originMetaData.getPrimaryKeys();
            if (comparisonKeys.length > 0) {
                primaryKey = comparisonKeys;
            }
            return new AddSettingTableMetaData(originMetaData, primaryKey, tableSeparator);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public enum Strategy {
        BY_NAME, PATTERN
    }

    public static class Builder {
        private TableSeparator commonExpressions = TableSeparator.NONE;

        private final Map<String, List<TableSeparator>> byNames = new HashMap<>();

        private final Map<String, List<TableSeparator>> pattern = new HashMap<>();

        public Builder add(final TableSeparators tableSeparators) {
            this.byNames.putAll(tableSeparators.byNames);
            this.pattern.putAll(tableSeparators.pattern);
            this.commonExpressions = this.commonExpressions.add(tableSeparators.commonExpressions);
            return this;
        }

        public TableSeparators build() {
            return new TableSeparators(this);
        }

        public Map<String, List<TableSeparator>> getByNames() {
            return this.byNames;
        }

        public Map<String, List<TableSeparator>> getPattern() {
            return this.pattern;
        }

        public void addCommon(final TableSeparator aExpressions) {
            this.commonExpressions = this.commonExpressions.add(aExpressions);
        }

        public void add(final Strategy strategy, final String targetName, final TableSeparator filter) {
            if (strategy == Strategy.BY_NAME) {
                this.add(this.byNames, targetName, filter);
            } else {
                this.add(this.pattern, targetName, filter);
            }
        }

        public Builder setCommonRenameFunction(final TableRenameStrategy newFunction) {
            this.commonExpressions = this.commonExpressions.map(builder -> builder.setSplitter(
                    builder.getSplitter()
                            .builder()
                            .setRenameFunction(newFunction)
                            .build()));
            return this;
        }

        protected void add(final Map<String, List<TableSeparator>> byNames, final String targetName, final TableSeparator separator) {
            final List<TableSeparator> current = byNames.computeIfAbsent(targetName, key -> new ArrayList<>());
            if (!current.contains(separator)) {
                current.add(separator);
            }
        }
    }
}
