package yo.dbunitcli.dataset;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ComparableTableMapperBuilder {

    private IDataSetConverter converter = null;
    private WrittenTables written = new WrittenTables();
    private List<ComparableTableJoin> joins = new ArrayList<>();
    private List<ComparableTableMappingTask> sources = List.of();

    public ComparableTableMapperBuilder setConverter(final IDataSetConverter converter) {
        this.converter = converter;
        return this;
    }

    public ComparableTableMapperBuilder setWritten(final WrittenTables written) {
        this.written = written;
        return this;
    }

    public ComparableTableMapperBuilder setJoins(final List<ComparableTableJoin> joins) {
        this.joins = joins;
        return this;
    }

    /**
     * 書いたテーブルに行を入れたと記録するタスク（{@link WrittenTables#sources}）。記録しない読込では空
     */
    public ComparableTableMapperBuilder setSources(final List<ComparableTableMappingTask> sources) {
        this.sources = sources;
        return this;
    }

    public ComparableTableMapper build(final Stream<AddSettingTableMetaData> addSettingTableMetaData) {
        final List<AddSettingTableMetaData> targets = addSettingTableMetaData.toList();
        final List<? extends ComparableTableMapper> results = IntStream.range(0, targets.size())
                .mapToObj(index -> {
                    final AddSettingTableMetaData target = targets.get(index);
                    final boolean enableRowProcessing = this.converter != null && this.converter.isEnableRowProcessing(target, this.joins);
                    final IDataSetConverter converter = index > 0 && this.isSplittable() ? this.converter.split() : this.converter;
                    return new ComparableTableMapperSingle(target, converter, this.written, this.joins, this.sources, enableRowProcessing);
                })
                .toList();
        if (results.isEmpty()) {
            return ComparableTableMapper.NONE;
        }
        if (results.size() == 1) {
            return results.getFirst();
        }
        return new ComparableTableMapperMulti(results, this.isSplittable());
    }

    private boolean isSplittable() {
        return this.converter != null && this.converter.isSplittable();
    }

}
