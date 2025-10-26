package yo.dbunitcli.dataset;

import org.dbunit.dataset.OrderedTableNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.List;
import java.util.Map;

/**
 * タスクごとの独立した状態管理クラス
 * 並列実行時に各タスクが独自のコンテキストを持つことで、スレッドセーフな処理を実現
 */
public class ComparableTableMappingContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableTableMappingContext.class);

    private final TableSeparators tableSeparators;
    private final IDataSetConverter converter;
    private final OrderedTableNameMap localTableMap;
    private final Map<String, Integer> localAlreadyWrite;
    private final List<ComparableTableJoin> localJoins;
    private ComparableTableMapper currentMapper;

    public ComparableTableMappingContext(final TableSeparators tableSeparators
            , final IDataSetConverter converter
            , final OrderedTableNameMap tableMap
            , final Map<String, Integer> alreadyWrite
            , final List<ComparableTableJoin> joins) {
        this.tableSeparators = tableSeparators;
        this.converter = converter;
        this.localTableMap = tableMap;
        this.localAlreadyWrite = alreadyWrite;
        this.localJoins = joins;
    }

    public void startTable(final TableMetaDataWithSource metaData) {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.currentMapper = this.tableSeparators.createMapper(metaData);
        this.currentMapper.startTable(this.converter, this.localAlreadyWrite, this.localJoins);
    }

    public void startTable(final ComparableTableJoin join) {
        this.currentMapper = this.tableSeparators.createMapper(join);
        this.currentMapper.startTable(this.converter, this.localAlreadyWrite, this.localJoins);
    }

    public void row(final Object[] values) {
        LOGGER.debug("row(values={}) - start", values);
        this.currentMapper.addRow(values);
    }

    public void endTable() {
        LOGGER.debug("endTable() - start");
        this.currentMapper.endTable(this.localTableMap);
    }

}
