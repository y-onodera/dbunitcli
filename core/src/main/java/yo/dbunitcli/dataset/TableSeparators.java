package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.TableMetaDataFilter;
import yo.dbunitcli.common.filter.AlwaysFilter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TableSeparators(List<TableSeparator> settings
        , List<TableSeparator> commonSettings
        , List<ComparableTableJoinCondition> joinConditions
        , TableMetaDataFilter filter
        , TableMetaDataFilter sourceFilter
        , TableSeparators next) {

    public enum FilterCombine {
        AND, OR;

        public static FilterCombine of(final String name) {
            return "or".equalsIgnoreCase(name) ? OR : AND;
        }
    }

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new ArrayList<>(builder.getSettings())
                , new ArrayList<>(builder.getCommonSettings())
                , new ArrayList<>(builder.getJoinConditions())
                , builder.getFilter()
                , builder.getSourceFilter()
                , builder.getNext());
    }

    public TableSeparators filter(TableMetaDataFilter filter) {
        return this.builder().setFilter(filter).build();
    }

    /**
     * settingsの対象（name/pattern）、joinの対象（left/right）、中身のあるcommonSettingsの対象のソーステーブルだけを読み込む。
     * 対象外のテーブルは出力しないため、設定を適用した結果だけを確認できる。設定が無ければ何も読み込まない
     */
    public TableSeparators onlySettingTargets() {
        final TableMetaDataFilter settingTargets = Stream.concat(this.settings.stream()
                                                                 , this.commonSettings.stream().filter(TableSeparator::hasSettings))
                                                         .map(TableSeparator::sourceFilter)
                                                         .reduce(new AlwaysFilter(false), TableMetaDataFilter::or);
        final TableMetaDataFilter joinTargets = tableName -> this.joinConditions.stream()
                                                                                .anyMatch(it -> it.hasRelation(tableName));
        return this.builder().addSourceFilter(settingTargets.or(joinTargets)).build();
    }

    public TableSeparators then(final TableSeparators next) {
        if (this.next == next || next == null || next == NONE) {
            return this;
        }
        if (this == NONE) {
            return next;
        }
        if (this.next == NONE || this.next == null) {
            return this.builder().setNext(next).build();
        }
        return this.builder().setNext(this.next.then(next)).build();
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

    /**
     * 出力テーブル名がソースごとに読込前に決まるか（joinはソースをまたぎ、件数分割は分割後の名前が行数で決まるため決まらない）。
     * 後段（next）の設定も含めて見る
     */
    public boolean isIndependentTables() {
        return this.chain().allMatch(it -> it.joinConditions().isEmpty() && !it.hasSplitter());
    }

    /**
     * 読込前に求めた出力テーブルの候補で、読まずに除外できるソースがありうるか
     * （出力テーブル名が読込前に決まり、出力の絞り込み（outputFilter）がある）
     */
    public boolean canExcludeBeforeRead() {
        return this.isIndependentTables()
                && this.chain().anyMatch(it -> !Builder.isUnset(it.filter()) || !Builder.isUnset(it.sourceFilter()));
    }

    /**
     * 出力テーブルの候補がすべて出力の絞り込み（outputFilter）で除かれるか。{@link #canExcludeBeforeRead()}がtrueの場合に使う
     */
    public boolean excludesAll(final Stream<? extends ITableMetaData> candidates) {
        return candidates.flatMap(this::getAddSettingTableMetaData).findAny().isEmpty();
    }

    /**
     * この設定と後段（next）の設定
     */
    private Stream<TableSeparators> chain() {
        return Stream.iterate(this, it -> it != null && it != NONE, TableSeparators::next);
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
        if (!this.sourceFilter.test(metaData)) {
            return Stream.empty();
        }
        return this.chainNext(this.addNewNameSetting(this.addSettings(metaData), metaData.getTableName()));
    }

    private Stream<AddSettingTableMetaData> chainNext(final Stream<AddSettingTableMetaData> stage) {
        if (this.next == null || this.next == TableSeparators.NONE) {
            return stage;
        }
        return stage.flatMap(this.next::getAddSettingTableMetaData);
    }

    private Stream<AddSettingTableMetaData> addNewNameSetting(final Stream<AddSettingTableMetaData> target,
                                                              final String beforeTableName) {
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
                     })
                     .filter(this.filter::test);
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
        private TableSeparators next = TableSeparators.NONE;
        private List<TableSeparator> commonSettings = new ArrayList<>();
        private TableMetaDataFilter filter = new AlwaysFilter(true);
        private TableMetaDataFilter sourceFilter = new AlwaysFilter(true);

        public Builder add(final TableSeparators tableSeparators) {
            this.settings.addAll(tableSeparators.settings());
            this.commonSettings.addAll(tableSeparators.commonSettings());
            this.joinConditions.addAll(tableSeparators.joinConditions());
            this.filter = this.combine(this.filter, tableSeparators.filter(), FilterCombine.AND);
            this.sourceFilter = this.combine(this.sourceFilter, tableSeparators.sourceFilter(), FilterCombine.AND);
            this.next = this.next.then(tableSeparators.next());
            return this;
        }

        public Builder addFilter(final TableMetaDataFilter other, final FilterCombine combine) {
            this.filter = this.combine(this.filter, other, combine);
            return this;
        }

        /**
         * 絞り込み未設定(常にtrue)の側は結合結果に影響させない。
         */
        private TableMetaDataFilter combine(final TableMetaDataFilter base, final TableMetaDataFilter other, final FilterCombine combine) {
            if (isUnset(other)) {
                return base;
            }
            if (isUnset(base)) {
                return other;
            }
            return combine == FilterCombine.OR ? base.or(other) : base.and(other);
        }

        private static boolean isUnset(final TableMetaDataFilter filter) {
            return filter instanceof final AlwaysFilter always && always.result();
        }

        public TableSeparators getNext() {
            return this.next;
        }

        public Builder setNext(final TableSeparators next) {
            this.next = next == null ? NONE : next;
            return this;
        }

        public TableMetaDataFilter getFilter() {
            return this.filter;
        }

        /**
         * 読み込むソーステーブルの絞り込み（設定を適用する前のテーブルで判定する）
         */
        public Builder addSourceFilter(final TableMetaDataFilter other) {
            this.sourceFilter = this.combine(this.sourceFilter, other, FilterCombine.AND);
            return this;
        }

        public TableMetaDataFilter getSourceFilter() {
            return this.sourceFilter;
        }

        public Builder setFilter(TableMetaDataFilter filter) {
            this.filter = filter;
            return this;
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

    }
}
