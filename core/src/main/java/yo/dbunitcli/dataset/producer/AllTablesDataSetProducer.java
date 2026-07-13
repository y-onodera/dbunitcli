package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.NameFilter;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AllTablesDataSetProducer extends ComparableDBDataSetProducer {

    public AllTablesDataSetProducer(final ComparableDataSetParam param) {
        super(param);
    }

    @Override
    public Stream<? extends Source> getSourceStream() {
        try {
            final DatabaseMetaData meta = this.connection.getConnection().getMetaData();
            final NameFilter filter = this.param().tableNameFilter();
            final List<Source> sources = new ArrayList<>();
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    sources.add(Source.NONE.tableName(rs.getString("TABLE_NAME")));
                }
            }
            return sources.stream().filter(it -> filter.predicate(it.tableName()));
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }
}
