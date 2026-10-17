package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param stageTableName {@code next}を連鎖させる際、自段の結果のうちこの名前のテーブルだけを後段へ流す（nullなら全て）。
 *                      同名テーブルは合流してしまうため、対象テーブルの系統だけを後段へ流す用途に使う
 * @param next このtableSeparatorsを適用した結果のテーブルに対し、続けて適用する2段目のtableSeparators（無ければnull）。
 *             同一段の{@code settings}は元のカラムに並列に適用されるため、前段で追加した列やサブテーブルを
 *             後段が参照・加工する場合に使う。解釈するのはunit=tableの行解決のみで、他の経路は自段だけを使う
 */
public record TableSeparators(List<TableSeparator> settings
        , List<TableSeparator> commonSettings
        , List<ComparableTableJoinCondition> joinConditions
        , TableSeparators next
        , String stageTableName) {

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new ArrayList<>(builder.getSettings())
                , new ArrayList<>(builder.getCommonSettings())
                , new ArrayList<>(builder.getJoinConditions())
                , builder.getNext()
                , builder.getStageTableName());
    }

    public TableSeparators then(final TableSeparators next) {
        return this.builder().setNext(next).build();
    }

    public TableSeparators then(final TableSeparators next, final String stageTableName) {
        return this.builder().setNext(next).setStageTableName(stageTableName).build();
    }

    public TableSeparators nextOrNone() {
        return this.next == null ? NONE : this.next;
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public List<ComparableTableJoin> joins() {
        return this.joinConditions()
                .stream()
                .map(ComparableTableJoin::new)
                .collect(Collectors.toList());
    }

    public TableSeparators map(final Consumer<TableSeparators.Builder> editor) {
        final TableSeparators.Builder builder = this.builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public boolean hasAdditionalSetting(final ITableMetaData metaData) {
        return !this.getSeparators(metaData)
                .stream()
                .filter(it -> !it.equals(TableSeparator.NONE))
                .toList().isEmpty();
    }

    public boolean hasSplitter() {
        return this.settings()
                .stream()
                .anyMatch(it -> it.splitter().isSplit())
                || this.commonSettings()
                .stream()
                .anyMatch(it -> it.splitter().isSplit());
    }

    public Stream<AddSettingTableMetaData> getAddSettingTableMetaData(final ComparableTableJoin join) {
        return this.chainNext(this.addNewNameSetting(join.getCondition().tableSeparators()
                        .stream()
                        .map(it -> this.getCommonSettings(join.joinMetaData())
                                .add(it)
                                .addSetting(join.joinMetaData()))
                , join.joinMetaData().getTableName()));
    }

    public Stream<AddSettingTableMetaData> getAddSettingTableMetaData(final ITableMetaData metaData) {
        return this.chainNext(this.addNewNameSetting(this.addSettings(metaData), metaData.getTableName()));
    }

    private Stream<AddSettingTableMetaData> chainNext(final Stream<AddSettingTableMetaData> stage) {
        if (this.next == null) {
            return stage;
        }
        return (this.stageTableName == null ? stage : stage.filter(it -> it.getTableName().equals(this.stageTableName) || it.getTableSplitter().isSplit()))
                .flatMap(this.next::getAddSettingTableMetaData);
    }

    private Stream<AddSettingTableMetaData> addNewNameSetting(final Stream<AddSettingTableMetaData> target, final String beforeTableName) {
        return target.flatMap(it -> {
            if (!Objects.equals(beforeTableName, it.getTableName())) {
                final Set<TableSeparator> separatorsFromSettings = this.getSeparatorsFromSettings(it);
                if (separatorsFromSettings.isEmpty()) {
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
        return this.getSeparators(originMetaData).stream()
                .map(it -> it.addSetting(originMetaData));
    }

    private Collection<TableSeparator> getSeparators(final ITableMetaData originMetaData) {
        final Set<TableSeparator> result = this.getSeparatorsFromSettings(originMetaData);
        if (!result.isEmpty()) {
            return result;
        }
        result.add(this.getCommonSettings(originMetaData));
        return result;
    }

    private Set<TableSeparator> getSeparatorsFromSettings(final ITableMetaData originMetaData) {
        return new HashSet<>(this.settings.stream()
                .filter(TableSeparator::hasSettings)
                .filter(it -> it.sourceFilter().test(originMetaData))
                .map(this.getCommonSettings(originMetaData)::add)
                .toList());
    }

    private TableSeparator getCommonSettings(final ITableMetaData originMetaData) {
        return this.commonSettings.stream()
                .filter(it -> it.sourceFilter().test(originMetaData))
                .reduce(TableSeparator.NONE, TableSeparator::add);
    }

    public static class Builder {
        private final List<TableSeparator> settings = new ArrayList<>();
        private final List<ComparableTableJoinCondition> joinConditions = new ArrayList<>();
        private List<TableSeparator> commonSettings = new ArrayList<>();
        private TableSeparators next;
        private String stageTableName;

        public Builder add(final TableSeparators tableSeparators) {
            this.settings.addAll(tableSeparators.settings());
            this.commonSettings.addAll(tableSeparators.commonSettings());
            this.joinConditions.addAll(tableSeparators.joinConditions());
            if (tableSeparators.stageTableName() != null) {
                this.stageTableName = tableSeparators.stageTableName();
            }
            if (tableSeparators.next() != null) {
                this.next = this.next == null ? tableSeparators.next() : this.next.builder().add(tableSeparators.next()).build();
            }
            return this;
        }

        public Builder setNext(final TableSeparators next) {
            this.next = next;
            return this;
        }

        public TableSeparators getNext() {
            return this.next;
        }

        public Builder setStageTableName(final String stageTableName) {
            this.stageTableName = stageTableName;
            return this;
        }

        public String getStageTableName() {
            return this.stageTableName;
        }

        public TableSeparators build() {
            return new TableSeparators(this);
        }

        public List<TableSeparator> getSettings() {
            return this.settings;
        }

        public List<TableSeparator> getCommonSettings() {
            if (this.commonSettings.isEmpty()) {
                this.commonSettings.add(TableSeparator.NONE);
            }
            return this.commonSettings;
        }

        public List<ComparableTableJoinCondition> getJoinConditions() {
            return this.joinConditions;
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
            this.joinConditions.add(join);
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
