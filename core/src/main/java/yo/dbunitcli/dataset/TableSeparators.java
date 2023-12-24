package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record TableSeparators(List<TableSeparator> settings,
                              List<TableSeparator> commonSettings) {

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new ArrayList<>(builder.getSettings()), builder.commonExpressions);
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
        final Set<TableSeparator> result = new HashSet<>(this.settings.stream()
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.targetFilter().test(tableName))
                .map(this.getCommonExpressions(tableName)::add)
                .toList());
        if (result.size() > 0) {
            return result;
        }
        result.add(this.getCommonExpressions(tableName));
        return result;
    }

    private TableSeparator getCommonExpressions(final String tableName) {
        return this.commonSettings.stream()
                .filter(it -> it.targetFilter().test(tableName))
                .reduce(TableSeparator.NONE, TableSeparator::add);
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

    public static class Builder {
        private List<TableSeparator> commonExpressions = new ArrayList<>();

        private final List<TableSeparator> settings = new ArrayList<>();

        public Builder() {
            this.commonExpressions.add(TableSeparator.NONE);
        }

        public Builder add(final TableSeparators tableSeparators) {
            this.settings.addAll(tableSeparators.settings);
            this.commonExpressions.addAll(tableSeparators.commonSettings);
            return this;
        }

        public TableSeparators build() {
            return new TableSeparators(this);
        }

        public List<TableSeparator> getSettings() {
            return this.settings;
        }

        public void addCommon(final TableSeparator aExpressions) {
            this.commonExpressions.add(aExpressions);
        }

        public void add(final TableSeparator setting) {
            this.settings.add(setting);
        }

        public Builder setCommonRenameFunction(final TableRenameStrategy newFunction) {
            this.commonExpressions = this.commonExpressions.stream()
                    .map(it -> it.map(builder -> builder.setSplitter(
                            builder.getSplitter()
                                    .builder()
                                    .setRenameFunction(newFunction)
                                    .build())))
                    .toList();
            return this;
        }
    }
}
