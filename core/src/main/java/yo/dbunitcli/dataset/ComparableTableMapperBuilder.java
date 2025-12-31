package yo.dbunitcli.dataset;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComparableTableMapperBuilder {

    private IDataSetConverter converter = null;
    private Map<String, Integer> alreadyWrite = new HashMap<>();
    private List<ComparableTableJoin> joins = new ArrayList<>();
    private List<ComparableTableMappingTask> chain = new ArrayList<>();
    private TreeMap<String, ComparableTable> contextShareTableMap = new TreeMap<>();
    private boolean chainRun = false;

    public ComparableTableMapperBuilder setConverter(final IDataSetConverter converter) {
        this.converter = converter;
        return this;
    }

    public ComparableTableMapperBuilder setAlreadyWrite(final Map<String, Integer> alreadyWrite) {
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

    public ComparableTableMapper build(final Stream<AddSettingTableMetaData> addSettingTableMetaData) {
        final int[] count = new int[]{0};
        final List<? extends ComparableTableMapper> results = addSettingTableMetaData
                .map(target -> {
                    final List<ComparableTableJoin> targetJoins = this.joins.stream()
                            .filter(join -> join.hasRelation(target.getTableName()))
                            .collect(Collectors.toList());
                    final boolean enableRowProcessing = this.converter != null && this.converter.isEnableRowProcessing(target, this.joins);
                    if (count[0] > 0 && this.isSplittable()) {
                        return new ComparableTableMapperSingle(target, this.converter.split(), this.contextShareTableMap, this.alreadyWrite, targetJoins, new ArrayList<>(), false, enableRowProcessing);
                    }
                    return new ComparableTableMapperSingle(target, this.converter, this.contextShareTableMap, this.alreadyWrite, targetJoins, this.chain, this.chainRun, enableRowProcessing);
                })
                .peek(it -> count[0]++)
                .toList();
        if (results.size() == 1) {
            return results.getFirst();
        }
        return new ComparableTableMapperMulti(results, this.isSplittable());
    }

    private boolean isSplittable() {
        return this.converter != null && this.converter.isSplittable();
    }

}
