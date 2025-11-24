package yo.dbunitcli.application;

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
        public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader, final ComparableDataSetParam loadParam) {
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

        @Override
        public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
            return this.dataSetToStream(loader, param);
        }
    },

    table {
        @Override
        public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader, final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            final String[] tableNames = dataSet.getTableNames();
            return IntStream.range(0, tableNames.length)
                    .mapToObj(it -> loader.parameter()
                            .asInputParam()
                            .withRowNumber(it)
                            .addAll(dataSet.getTable(tableNames[it]).toMap(true).getFirst()));
        }

        @Override
        public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
            final Map<String, Object> tables = loader.getComparableDataSetProducer(param).lazyLoad(true);
            final int[] row = new int[]{0};
            return tables.keySet().stream()
                    .map(it -> loader.parameter()
                            .asInputParam()
                            .withRowNumber(row[0]++)
                            .addAll((Map<String, Object>) tables.get(it)));
        }
    },
    dataset;

    public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
        final ComparableDataSet dataSet = loader.loadDataSet(param);
        return Stream.of(loader.parameter()
                .asInputParam()
                .withRowNumber(0)
                .add("dataSet", dataSet.toMap(true)
                        .collect(Collectors.toMap(it -> it.get("tableName").toString(), it -> it, (old, other) -> other, LinkedHashMap::new))));
    }

    public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
        return Stream.of(loader.parameter()
                .asInputParam()
                .withRowNumber(0)
                .add("dataSet", loader.getComparableDataSetProducer(param).lazyLoad(true)));
    }
}
