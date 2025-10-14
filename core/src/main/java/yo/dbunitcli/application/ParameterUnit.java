package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum ParameterUnit {
    record {
        @Override
        public Stream<Parameter> loadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            if ((loadParam.mapIncludeMetaData())) {
                final Integer[] rowNum = new Integer[]{0};
                return dataSet.toMap()
                        .flatMap(it -> ((List<Map<String, Object>>) it.get("rows")).stream()
                                .map(row -> loader.parameter()
                                        .asInputParam()
                                        .withRowNumber(rowNum[0]++)
                                        .addAll(it)
                                        .add("row", row)));
            }
            final Integer[] rowNum = new Integer[]{0};
            return dataSet.toMap()
                    .map(it -> loader.parameter()
                            .asInputParam()
                            .withRowNumber(rowNum[0]++)
                            .addAll(it));
        }
    },

    table {
        @Override
        public Stream<Parameter> loadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            try {
                final String[] tableNames = dataSet.getTableNames();
                return IntStream.range(0, tableNames.length)
                        .mapToObj(it -> loader.parameter()
                                .asInputParam()
                                .withRowNumber(it)
                                .addAll(dataSet.getTable(tableNames[it]).toMap(true).getFirst()));
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }
    },
    dataset;

    public Stream<Parameter> loadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
        final ComparableDataSet dataSet = loader.loadDataSet(param);
        return Stream.of(loader.parameter()
                .asInputParam()
                .withRowNumber(0)
                .add("dataSet", dataSet.toMap(true)
                        .collect(Collectors.toMap(it -> it.get("tableName").toString(), it -> it, (old, other) -> other, LinkedHashMap::new))));
    }
}
