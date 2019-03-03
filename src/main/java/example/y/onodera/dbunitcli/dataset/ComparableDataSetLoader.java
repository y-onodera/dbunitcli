package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparableDataSetLoader {

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, String aSource, Map<String, List<String>> excludeColumns) throws DataSetException {
        Map<String, IColumnFilter> filters = toFilters(excludeColumns);
        switch (aSource) {
            case "xlsx":
                return new ComparableXlsxDataSet(aDir, filters);
            case "xls":
                return new ComparableXlsDataSet(aDir, filters);
            default:
                return new ComparableCSVDataSet(aDir, aEncoding, filters);
        }
    }

    private Map<String, IColumnFilter> toFilters(Map<String, List<String>> excludeColumns) {
        return excludeColumns.entrySet()
                    .stream()
                    .collect(Collectors.toMap(it -> it.getKey()
                            , (Map.Entry<String, List<String>> it) -> {
                                List<Column> columns = Lists.newArrayList();
                                for (String columnName : it.getValue()) {
                                    columns.add(new Column(columnName, DataType.UNKNOWN));
                                }
                                DefaultColumnFilter result = new DefaultColumnFilter();
                                result.excludeColumns(columns.toArray(new Column[columns.size()]));
                                return result;
                            }));
    }
}
