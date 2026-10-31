package yo.dbunitcli.dataset;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ComparableTableMapperBuilder {

    private IDataSetConverter converter = null;
    private WrittenTables alreadyWrite = new WrittenTables();
    private List<ComparableTableJoin> joins = new ArrayList<>();
    private TreeMap<String, ComparableTable> contextShareTableMap = new TreeMap<>();

    public ComparableTableMapperBuilder setConverter(final IDataSetConverter converter) {
        this.converter = converter;
        return this;
    }

    public ComparableTableMapperBuilder setAlreadyWrite(final WrittenTables alreadyWrite) {
        this.alreadyWrite = alreadyWrite;
        return this;
    }

    public ComparableTableMapperBuilder setJoins(final List<ComparableTableJoin> joins) {
        this.joins = joins;
        return this;
    }

    public ComparableTableMapperBuilder setContextShareTableMap(final TreeMap<String, ComparableTable> contextShareTableMap) {
        this.contextShareTableMap = contextShareTableMap;
        return this;
    }

    public ComparableTableMapper build(final Stream<AddSettingTableMetaData> addSettingTableMetaData) {
        final List<AddSettingTableMetaData> targets = addSettingTableMetaData.toList();
        final List<? extends ComparableTableMapper> results = IntStream.range(0, targets.size())
                .mapToObj(index -> {
                    final AddSettingTableMetaData target = targets.get(index);
                    final boolean enableRowProcessing = this.converter != null && this.converter.isEnableRowProcessing(target, this.joins);
                    final IDataSetConverter converter = index > 0 && this.isSplittable() ? this.converter.split() : this.converter;
                    return new ComparableTableMapperSingle(target, converter, this.contextShareTableMap, this.alreadyWrite, this.joins, enableRowProcessing);
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
