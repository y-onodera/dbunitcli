package yo.dbunitcli.dataset;

import org.dbunit.dataset.OrderedTableNameMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ComparableTableMapperMulti implements ComparableTableMapper {

    private final ComparableTableMapper head;
    private final List<ComparableTableMapper> rests;
    private final Collection<Object[]> rows;
    private IDataSetConverter converter;
    private Map<String, Integer> alreadyWrite;
    private List<ComparableTableJoin> joins;

    public ComparableTableMapperMulti(final List<ComparableTableMapper> delegates) {
        this.head = delegates.get(0);
        this.rests = delegates.subList(1, delegates.size());
        this.rows = new ArrayList<>();
    }

    @Override
    public void startTable(final IDataSetConverter converter, final Map<String, Integer> alreadyWrite, final List<ComparableTableJoin> joins) {
        this.converter = converter;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins;
        this.head.startTable(converter, alreadyWrite, joins);
        if (this.converter != null && this.converter.isSplittable()) {
            this.rests.forEach(it -> it.startTable(converter.split(), alreadyWrite, joins));
        }
    }

    @Override
    public void addRow(final Object[] values) {
        this.head.addRow(values);
        if (this.converter != null && this.converter.isSplittable()) {
            this.rests.forEach(it -> it.addRow(values));
        } else {
            this.rows.add(values);
        }
    }

    @Override
    public void endTable(final OrderedTableNameMap orderedTableNameMap) {
        this.head.endTable(orderedTableNameMap);
        if (this.converter != null && this.converter.isSplittable()) {
            this.rests.forEach(it -> it.endTable(orderedTableNameMap));
        } else {
            this.rests.forEach(it -> {
                it.startTable(this.converter, this.alreadyWrite, this.joins);
                this.rows.forEach(it::addRow);
                it.endTable(orderedTableNameMap);
            });
        }
    }
}
