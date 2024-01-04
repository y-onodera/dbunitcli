package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record TableSeparators(List<TableSeparator> settings
        , List<TableSeparator> commonSettings
        , List<JoinCondition> joins) {

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new ArrayList<>(builder.getSettings())
                , new ArrayList<>(builder.getCommonSettings())
                , new ArrayList<>(builder.getJoins()));
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
                .map(this.getCommonSettings(tableName)::add)
                .toList());
        if (result.size() > 0) {
            return result;
        }
        result.add(this.getCommonSettings(tableName));
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
        return this.addSettings(metaData).stream()
                .map(it -> {
                    ITableMetaData origin = metaData;
                    AddSettingTableMetaData resultMetaData = it;
                    while (!origin.getTableName().equals(resultMetaData.getTableName())) {
                        origin = resultMetaData;
                        final List<AddSettingTableMetaData> addSetting = this.addSettings(resultMetaData);
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

    private List<AddSettingTableMetaData> addSettings(final ITableMetaData originMetaData) {
        return this.getSeparators(originMetaData.getTableName()).stream()
                .map(it -> it.addSetting(originMetaData))
                .collect(Collectors.toList());
    }

    private TableSeparator getCommonSettings(final String tableName) {
        return this.commonSettings.stream()
                .filter(it -> it.targetFilter().test(tableName))
                .reduce(TableSeparator.NONE, TableSeparator::add);
    }

    public static class Builder {
        private List<TableSeparator> commonSettings = new ArrayList<>();

        private final List<TableSeparator> settings = new ArrayList<>();

        private final List<JoinCondition> joins = new ArrayList<>();

        public Builder() {
            this.commonSettings.add(TableSeparator.NONE);
        }

        public Builder add(final TableSeparators tableSeparators) {
            this.settings.addAll(tableSeparators.settings());
            this.commonSettings.addAll(tableSeparators.commonSettings());
            this.joins.addAll(tableSeparators.joins());
            return this;
        }

        public TableSeparators build() {
            return new TableSeparators(this);
        }

        public List<TableSeparator> getSettings() {
            return this.settings;
        }

        public List<TableSeparator> getCommonSettings() {
            return this.commonSettings;
        }

        public List<JoinCondition> getJoins() {
            return this.joins;
        }

        public void addSetting(final TableSeparator setting) {
            this.settings.add(setting);
        }

        public void addCommon(final TableSeparator aExpressions) {
            this.commonSettings.add(aExpressions);
        }

        public void addJoin(final JoinCondition join) {
            this.joins.add(join);
        }

        public Builder setCommonRenameFunction(final TableRenameStrategy newFunction) {
            this.commonSettings = this.commonSettings.stream()
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
