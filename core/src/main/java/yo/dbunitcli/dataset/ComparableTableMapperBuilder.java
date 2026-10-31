package yo.dbunitcli.dataset;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ComparableTableMapperBuilder {

    private IDataSetConverter converter = null;
    private WrittenTables alreadyWrite = new WrittenTables();
    private List<ComparableTableJoin> joins = new ArrayList<>();
    private List<ComparableTableMappingTask> chain = new ArrayList<>();
    private TreeMap<String, ComparableTable> contextShareTableMap = new TreeMap<>();
    private boolean chainRun = false;
    private TableSeparators tableSeparators = TableSeparators.NONE;
    private ComparableTableMappingContext.Listener listener = ComparableTableMappingContext.Listener.NONE;

    public ComparableTableMapperBuilder setTableSeparators(final TableSeparators tableSeparators) {
        this.tableSeparators = tableSeparators;
        return this;
    }

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

    public ComparableTableMapperBuilder setChain(final List<ComparableTableMappingTask> chain) {
        this.chain = chain;
        return this;
    }

    public ComparableTableMapperBuilder setChainRun(final boolean chainRun) {
        this.chainRun = chainRun;
        return this;
    }

    public ComparableTableMapperBuilder setContextShareTableMap(final TreeMap<String, ComparableTable> contextShareTableMap) {
        this.contextShareTableMap = contextShareTableMap;
        return this;
    }

    public ComparableTableMapperBuilder setListener(final ComparableTableMappingContext.Listener listener) {
        this.listener = listener;
        return this;
    }

    public ComparableTableMapper build(final Stream<AddSettingTableMetaData> addSettingTableMetaData) {
        final List<AddSettingTableMetaData> targets = addSettingTableMetaData.toList();
        final List<? extends ComparableTableMapper> results = IntStream.range(0, targets.size())
                .mapToObj(index -> {
                    final AddSettingTableMetaData target = targets.get(index);
                    final boolean enableRowProcessing = this.converter != null && this.converter.isEnableRowProcessing(target, this.joins);
                    if (index > 0 && this.isSplittable()) {
                        return new ComparableTableMapperSingle(target, this.converter.split(), this.contextShareTableMap, this.alreadyWrite, this.joins, new ArrayList<>(), false, enableRowProcessing, this.tableSeparators, this.listener);
                    }
                    // 行を保持する読込（converterなし）では、続くソースはこのソースの最後のテーブルを確定させてから1回だけ読む
                    final List<ComparableTableMappingTask> chain = this.converter == null && index < targets.size() - 1
                            ? new ArrayList<>()
                            : this.chain;
                    return new ComparableTableMapperSingle(target, this.converter, this.contextShareTableMap, this.alreadyWrite, this.joins, chain, this.chainRun, enableRowProcessing, this.tableSeparators, this.listener);
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
