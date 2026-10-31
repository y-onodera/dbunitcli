package yo.dbunitcli.application;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.Workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum ParameterUnit {
    record {
        /**
         * 行ごとのパラメータ（RecordConverterと同じ形）。テーブルは名前順に、1テーブルずつパラメータを作る
         */
        @Override
        public Stream<Parameter> dataSetToStream(final ComparableDataSetLoader loader,
                                                 final ComparableDataSetParam loadParam) {
            final ComparableDataSet dataSet = loader.loadDataSet(loadParam);
            final Parameter base = loader.parameter().asInputParam();
            if (loadParam.source() == DataSourceType.none) {
                return dataSet.toMap().map(it -> base.withRowNumber(0).addAll(it));
            }
            return Arrays.stream(dataSet.getTables()).flatMap(table -> {
                final List<Parameter> result = new ArrayList<>();
                final RecordConverter converter = new RecordConverter(base, result::add, loadParam.mapIncludeMetaData());
                converter.convert(table);
                converter.endDataSet();
                return result.stream();
            });
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
            return ParameterUnit.loadedTableTemplates(loader, loader.loadDataSet(loadParam)).map(TableTemplate::load);
        }

        @Override
        public Stream<Parameter> lazyLoadStream(final ComparableDataSetLoader loader,
                                                final ComparableDataSetParam param) {
            final ComparableDataSetProducer producer = loader.getComparableDataSetProducer(param);
            // 行は出力（StreamingEachCommand）の中で読むため、ストリームを閉じるまで接続を保つ
            return ParameterUnit.streamingTableTemplates(loader, producer)
                                .map(TableTemplate::load)
                                .onClose(producer::close);
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
        final ComparableDataSetProducer producer = loader.getComparableDataSetProducer(param);
        // 行は出力の中で読むため、ストリームを閉じるまで接続を保つ
        return Stream.of(loader.parameter()
                               .asInputParam()
                               .withRowNumber(0)
                               .add("dataSet", producer.lazyLoad(true)))
                     .onClose(producer::close);
    }

    /**
     * unit=tableのテンプレート出力を、行を読み込む前のテーブル単位で列挙する。行はload()で1テーブルずつ読み込み、unitTableSeparatorsを適用する
     */
    public static Stream<TableTemplate> tableTemplates(final ComparableDataSetLoader loader,
                                                       final ComparableDataSetProducer producer,
                                                       final TableSeparators unitTableSeparators) {
        return ParameterUnit.lazyLoad(loader, producer, (base, dto) -> new SeparatedTableTemplate(base, dto, unitTableSeparators));
    }

    /**
     * unit=tableのExcel出力（lazyLoad）を、行を読み込む前のテーブル単位で列挙する。行は出力（StreamingEachCommand）の中で読み込む
     */
    public static Stream<TableTemplate> streamingTableTemplates(final ComparableDataSetLoader loader,
                                                                final ComparableDataSetProducer producer) {
        return ParameterUnit.lazyLoad(loader, producer, StreamingTableTemplate::new);
    }

    /**
     * unit=tableのExcel出力（lazyLoadなし）を、読み込み済みのデータセットからテーブル単位で列挙する
     */
    public static Stream<TableTemplate> loadedTableTemplates(final ComparableDataSetLoader loader, final ComparableDataSet dataSet) {
        final String[] tableNames = dataSet.getTableNames();
        return IntStream.range(0, tableNames.length)
                        .mapToObj(it -> new LoadedTableTemplate(loader.parameter().asInputParam().withRowNumber(it),
                                                                dataSet.getTable(tableNames[it])));
    }

    private static Stream<TableTemplate> lazyLoad(final ComparableDataSetLoader loader,
                                                  final ComparableDataSetProducer producer,
                                                  final BiFunction<Parameter, ComparableTableDto, TableTemplate> factory) {
        final Map<String, Object> tables = producer.lazyLoad(true);
        final int[] row = new int[]{0};
        return tables.values().stream()
                     .map(it -> factory.apply(loader.parameter().asInputParam().withRowNumber(row[0]++), (ComparableTableDto) it));
    }

    /**
     * unit=tableの出力1回分。並列で出力するときは、tasks()を共有するもの同士を同じスレッドで順に処理する
     */
    public sealed interface TableTemplate permits SeparatedTableTemplate, StreamingTableTemplate, LoadedTableTemplate {

        /**
         * 行を読み込まずに作れるパラメータ。出力先の決定など、行を参照しない用途に使う
         */
        Parameter unloaded();

        /**
         * 出力に渡すパラメータ。読み込んだ行は戻り値だけが参照する
         */
        Parameter load();

        /**
         * 行の読込で実行するタスク（状態を持ちうるため、同じタスクを同時に実行してはならない）
         */
        Stream<ComparableTableMappingTask> tasks();

        /**
         * 並列実行で2つ目以降の作業スレッドが使うTableTemplateを返す
         */
        TableTemplate forWorker(Workers.WorkerResources resources);
    }

    /**
     * テキスト出力。load()で行を読み込み、unitTableSeparatorsを適用する（dtoは変更しない）
     */
    public record SeparatedTableTemplate(Parameter base, ComparableTableDto dto,
                                         TableSeparators unitTableSeparators) implements TableTemplate {

        @Override
        public Parameter unloaded() {
            return this.toParameter(this.dto);
        }

        @Override
        public Parameter load() {
            return this.toParameter(this.dto.applyDataSetSeparators(this.unitTableSeparators));
        }

        @Override
        public Stream<ComparableTableMappingTask> tasks() {
            return this.dto.getTask().tasks().stream();
        }

        @Override
        public TableTemplate forWorker(final Workers.WorkerResources resources) {
            return new SeparatedTableTemplate(this.base, this.dto.forWorker(resources), this.unitTableSeparators);
        }

        private Parameter toParameter(final ComparableTableDto target) {
            final Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put(target.getTableName(), target);
            return this.base.addAll(target).add("dataSet", dataset);
        }
    }

    /**
     * Excel出力（lazyLoad）。rowsは読込タスクのままで、出力中に1行ずつ読み込む
     */
    public record StreamingTableTemplate(Parameter base, ComparableTableDto dto) implements TableTemplate {

        @Override
        public Parameter unloaded() {
            return this.load();
        }

        @Override
        public Parameter load() {
            return this.base.addAll(this.dto);
        }

        @Override
        public Stream<ComparableTableMappingTask> tasks() {
            return this.dto.getTask().tasks().stream();
        }

        @Override
        public TableTemplate forWorker(final Workers.WorkerResources resources) {
            return new StreamingTableTemplate(this.base, this.dto.forWorker(resources));
        }
    }

    /**
     * Excel出力（lazyLoadなし）。読み込み済みのテーブルから出力の都度パラメータを作る（読込タスクを持たない）
     */
    public record LoadedTableTemplate(Parameter base, ComparableTable table) implements TableTemplate {

        @Override
        public Parameter unloaded() {
            return this.base.addAll(new ComparableTableDto(this.table.getTableMetaData()));
        }

        @Override
        public Parameter load() {
            return this.base.addAll(this.table.toMap(true).getFirst());
        }

        @Override
        public Stream<ComparableTableMappingTask> tasks() {
            return Stream.empty();
        }

        @Override
        public TableTemplate forWorker(final Workers.WorkerResources resources) {
            return this;
        }
    }
}
