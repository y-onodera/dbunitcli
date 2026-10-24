package yo.dbunitcli.application;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.*;
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
        public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader,
                                                 final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            if ((loadParam.mapIncludeMetaData())) {
                final Integer[] rowNum = new Integer[]{0};
                return dataSet.toMap()
                              .flatMap(it -> ((List<Map<String, Object>>) it.get("rows")).stream()
                                                                                         .map(row -> loader.parameter()
                                                                                                           .asInputParam()
                                                                                                           .withRowNumber(
                                                                                                                   rowNum[0]++)
                                                                                                           .addAll(it)
                                                                                                           .add("row",
                                                                                                                row)));
            }
            final Integer[] rowNum = new Integer[]{0};
            return dataSet.toMap()
                          .map(it -> loader.parameter()
                                           .asInputParam()
                                           .withRowNumber(rowNum[0]++)
                                           .addAll(it));
        }

        @Override
        public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader,
                                                final ComparableDataSetParam param) {
            return this.dataSetToStream(loader, param);
        }
    },

    table {
        @Override
        public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader,
                                                 final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            final String[] tableNames = dataSet.getTableNames();
            return IntStream.range(0, tableNames.length)
                            .mapToObj(it -> loader.parameter()
                                                  .asInputParam()
                                                  .withRowNumber(it)
                                                  .addAll(dataSet.getTable(tableNames[it]).toMap(true).getFirst()));
        }

        @Override
        public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader,
                                                final ComparableDataSetParam param) {
            final Map<String, Object> tables = loader.getComparableDataSetProducer(param).lazyLoad(true);
            final int[] row = new int[]{0};
            return tables.keySet().stream()
                         .map(it -> loader.parameter()
                                          .asInputParam()
                                          .withRowNumber(row[0]++)
                                          .addAll((Map<String, Object>) tables.get(it)));
        }

        @Override
        public Stream<Parameter> templateStream(final ComparableDataSetLoader loader,
                                                final ComparableDataSetProducer producer,
                                                final TableSeparators unitTableSeparators) {
            return ParameterUnit.tableTemplates(loader, producer, unitTableSeparators).map(TableTemplate::load);
        }
    },
    dataset;

    public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
        final ComparableDataSet dataSet = loader.loadDataSet(param);
        return Stream.of(loader.parameter()
                               .asInputParam()
                               .withRowNumber(0)
                               .add("dataSet", dataSet.toMap(true)
                                                      .collect(Collectors.toMap(it -> it.get("tableName").toString(),
                                                                                it -> it, (_, other) -> other,
                                                                                LinkedHashMap::new))));
    }

    public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader, final ComparableDataSetParam param) {
        return Stream.of(loader.parameter()
                               .asInputParam()
                               .withRowNumber(0)
                               .add("dataSet", loader.getComparableDataSetProducer(param).lazyLoad(true)));
    }

    public Stream<Parameter> templateStream(final ComparableDataSetLoader loader,
                                            final ComparableDataSetProducer producer,
                                            final TableSeparators unitTableSeparators) {
        return this.dataSetToStream(loader, producer.param());
    }

    /**
     * unit=tableのテンプレート出力を、行を読み込む前のテーブル単位で列挙する。行はload()で1テーブルずつ読み込む
     */
    public static Stream<TableTemplate> tableTemplates(final ComparableDataSetLoader loader,
                                                       final ComparableDataSetProducer producer,
                                                       final TableSeparators unitTableSeparators) {
        final Map<String, Object> tables = producer.lazyLoad(true);
        final int[] row = new int[]{0};
        return tables.values().stream()
                     .map(it -> new TableTemplate(loader.parameter().asInputParam().withRowNumber(row[0]++),
                                                  (ComparableTableDto) it, unitTableSeparators));
    }

    public record TableTemplate(Parameter base, ComparableTableDto dto, TableSeparators unitTableSeparators) {

        /**
         * 行を読み込む前のパラメータ（rowsは未読込）。出力先の決定など、行を参照しない用途に使う
         */
        public Parameter unloaded() {
            return this.toParameter(this.dto);
        }

        /**
         * 行を読み込んだパラメータ。読み込んだ行は戻り値だけが参照する（dtoは変更しない）
         */
        public Parameter load() {
            return this.toParameter(this.dto.applyDataSetSeparators(this.unitTableSeparators));
        }

        /**
         * 並列実行で2つ目以降の作業スレッドが使うTableTemplateを返す
         */
        public TableTemplate forWorker(final ParallelTableMapping.WorkerResources resources) {
            return new TableTemplate(this.base, this.dto.forWorker(resources), this.unitTableSeparators);
        }

        private Parameter toParameter(final ComparableTableDto target) {
            final Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put(target.getTableName(), target);
            return this.base.addAll(target).add("dataSet", dataset);
        }
    }
}
