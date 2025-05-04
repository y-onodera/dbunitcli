package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TableSeparators(List<TableSeparator> settings
        , List<TableSeparator> commonSettings
        , List<ComparableTableJoinCondition> joins) {

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

    public ComparableTableMapper createMapper(final ComparableTableJoin join) {
        final ITableMetaData joinMetadata = join.joinMetaData();
        return this.createMapper(this.addNewNameSetting(join.getCondition().tableSeparators()
                        .stream()
                        .map(it -> this.getCommonSettings(joinMetadata.getTableName())
                                .add(it)
                                .addSetting(joinMetadata))
                , joinMetadata.getTableName()));
    }

    public ComparableTableMapper createMapper(final ITableMetaData metaData) {
        return this.createMapper(this.getAddSettingTableMetaData(metaData));
    }

    private ComparableTableMapper createMapper(final Stream<AddSettingTableMetaData> metaData) {
        final List<ComparableTableMapper> results = metaData.map(this::createMapperFrom).collect(Collectors.toList());
        if (results.size() == 1) {
            return results.get(0);
        }
        return new ComparableTableMapperMulti(results);
    }

    private ComparableTableMapper createMapperFrom(final AddSettingTableMetaData results) {
        return new ComparableTableMapperSingle(results);
    }

    private Stream<AddSettingTableMetaData> getAddSettingTableMetaData(final ITableMetaData metaData) {
        return this.addNewNameSetting(this.addSettings(metaData), metaData.getTableName());
    }

    private Stream<AddSettingTableMetaData> addNewNameSetting(final Stream<AddSettingTableMetaData> target, final String beforeTableName) {
        return target.flatMap(it -> {
            if (!Objects.equals(beforeTableName, it.getTableName())) {
                final Set<TableSeparator> separatorsFromSettings = this.getSeparatorsFromSettings(it.getTableName());
                if (separatorsFromSettings.size() == 0) {
                    return Stream.of(it);
                }
                return this.addNewNameSetting(separatorsFromSettings
                                .stream().map(separator -> separator.addSetting(it))
                        , it.getTableName());
            }
            return Stream.of(it);
        });
    }

    private Stream<AddSettingTableMetaData> addSettings(final ITableMetaData originMetaData) {
        return this.getSeparators(originMetaData.getTableName()).stream()
                .map(it -> it.addSetting(originMetaData));
    }

    private Collection<TableSeparator> getSeparators(final String tableName) {
        final Set<TableSeparator> result = this.getSeparatorsFromSettings(tableName);
        if (result.size() > 0) {
            return result;
        }
        result.add(this.getCommonSettings(tableName));
        return result;
    }

    private Set<TableSeparator> getSeparatorsFromSettings(final String tableName) {
        return new HashSet<>(this.settings.stream()
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.targetFilter().test(tableName))
                .map(this.getCommonSettings(tableName)::add)
                .toList());
    }

    private TableSeparator getCommonSettings(final String tableName) {
        return this.commonSettings.stream()
                .filter(it -> it.targetFilter().test(tableName))
                .reduce(TableSeparator.NONE, TableSeparator::add);
    }

    public static class Builder {
        private final List<TableSeparator> settings = new ArrayList<>();
        private final List<ComparableTableJoinCondition> joins = new ArrayList<>();
        private List<TableSeparator> commonSettings = new ArrayList<>();

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
            if(this.commonSettings.isEmpty()) {
                this.commonSettings.add(TableSeparator.NONE);
            }
            return this.commonSettings;
        }

        public List<ComparableTableJoinCondition> getJoins() {
            return this.joins;
        }

        public void addSettings(final List<TableSeparator> settings) {
            settings.forEach(this::addSetting);
        }

        public void addSetting(final TableSeparator setting) {
            this.settings.add(setting);
        }

        public void addCommon(final TableSeparator aExpressions) {
            this.commonSettings.add(aExpressions);
        }

        public void addJoin(final ComparableTableJoinCondition join) {
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
