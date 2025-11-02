package yo.dbunitcli.dataset;

import java.util.*;

public class ComparableTableMapperMulti implements ComparableTableMapper {

    private final ComparableTableMapper head;
    private final List<ComparableTableMapper> rests;
    private final Collection<Object[]> rows;
    private IDataSetConverter converter;
    private Map<String, Integer> alreadyWrite;
    private List<ComparableTableJoin> joins;

    public ComparableTableMapperMulti(final List<ComparableTableMapper> delegates) {
        this.head = delegates.getFirst();
        this.rests = delegates.subList(1, delegates.size());
        this.rows = new ArrayList<>();
    }

    @Override
    public void startTable(final IDataSetConverter converter, final Map<String, Integer> alreadyWrite, final List<ComparableTableJoin> joins) {
        this.converter = converter;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins;
        this.head.startTable(converter, alreadyWrite, joins);
        if (this.splitConvert()) {
            this.rests.forEach(it -> it.startTable(converter.split(), alreadyWrite, joins));
        }
    }

    @Override
    public void addRow(final Object[] values) {
        this.head.addRow(values);
        if (this.splitConvert()) {
            this.rests.forEach(it -> it.addRow(values));
        } else {
            this.rows.add(values);
        }
    }

    @Override
    public void endTable(final TreeMap<String, ComparableTable> orderedTableNameMap) {
        this.head.endTable(orderedTableNameMap);
        if (this.splitConvert()) {
            this.rests.forEach(it -> it.endTable(orderedTableNameMap));
        } else {
            this.rests.forEach(it -> {
                it.startTable(this.converter, this.alreadyWrite, this.joins);
                this.rows.forEach(it::addRow);
                it.endTable(orderedTableNameMap);
            });
        }
    }

    private boolean splitConvert() {
        return this.converter != null && this.converter.isSplittable();
    }
}
