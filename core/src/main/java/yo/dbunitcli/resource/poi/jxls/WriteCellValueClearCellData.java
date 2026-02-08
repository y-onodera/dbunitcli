package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiCellData;
import org.jxls.transform.poi.PoiTransformer;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;

/**
 * {@link #writeToCell(Cell, Context, PoiTransformer)}の実行後にcellValueをClearすることで、Excelを開いたときにFormulasを計算させる
 */
class WriteCellValueClearCellData extends CaptureWriteCellCellData {

    public WriteCellValueClearCellData(final PoiCellData delegate) {
        super(delegate);
    }

    @Override
    public void writeToCell(final Cell cell, final Context context, final PoiTransformer transformer) {
        super.writeToCell(cell, context, transformer);
        cell.setCellValue("");
        if (cell instanceof XSSFCell xCell) {
            CTCell cTCell = xCell.getCTCell();
            if (cTCell.isSetV()) {
                cTCell.unsetV();
            }
        }
    }
}
