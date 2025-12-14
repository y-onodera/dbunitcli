package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComparableTableMapperMulti implements ComparableTableMapper {

    private final ComparableTableMapper head;
    private final List<? extends ComparableTableMapper> rests;
    private final Collection<Object[]> rows;
    private final boolean isConverterSplit;

    public ComparableTableMapperMulti(final List<? extends ComparableTableMapper> delegates, final boolean converterSplittable) {
        this.head = delegates.getFirst();
        this.rests = delegates.subList(1, delegates.size());
        this.rows = new ArrayList<>();
        this.isConverterSplit = converterSplittable;
    }

    @Override
    public void startTable() {
        this.head.startTable();
        if (this.isConverterSplit) {
            this.rests.forEach(ComparableTableMapper::startTable);
        }
    }

    @Override
    public void addRow(final Object[] values) {
        this.head.addRow(values);
        if (this.isConverterSplit) {
            this.rests.forEach(it -> it.addRow(values));
        } else {
            this.rows.add(values);
        }
    }

    @Override
    public void endTable() {
        this.head.endTable();
        if (this.isConverterSplit) {
            this.rests.forEach(it -> it.endTable());
        } else {
            this.rests.forEach(it -> {
                it.startTable();
                this.rows.forEach(it::addRow);
                it.endTable();
            });
        }
    }
}
