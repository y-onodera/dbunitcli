package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import yo.dbunitcli.common.TableMetaDataWithSource;

public interface XlsxRowsToTableBuilder {

    boolean isTableStart(int rowNum);

    boolean hasRow(int rowNum);

    TableMetaDataWithSource startNewTable();

    void clearRowValue();

    void handle(CellReference reference, int currentCol, String formattedValue);

    String[] currentRow();

    boolean isNowProcessing();

    /**
     * @return このシートから作るテーブルをすべて開始したか（以降の行を読まなくてもテーブルのメタデータは揃っている）
     */
    boolean isAllTablesStarted();

    /**
     * @return このシートから作るテーブル名（シートを読む前に出力テーブルを決めるのに使う）
     */
    String[] getTableNames();

}
