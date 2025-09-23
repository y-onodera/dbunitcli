package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiCellData;
import org.jxls.transform.poi.SelectSheetsForStreamingPoiTransformer;

import java.util.Set;

public class UserFormulasValueClearStreamingPoiTransformer extends SelectSheetsForStreamingPoiTransformer {
    private static final String USER_FORMULA_PREFIX = "$[";
    private static final String USER_FORMULA_SUFFIX = "]";

    private static boolean isUserFormula(final String str) {
        return str.startsWith(USER_FORMULA_PREFIX) && str.endsWith(USER_FORMULA_SUFFIX);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook) {
        super(workbook);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final boolean allSheets, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, allSheets, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final Set<String> sheetNames, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, sheetNames, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    @Override
    protected void transformCell(final CellRef srcCellRef, final CellRef targetCellRef, final Context context, final boolean updateRowHeightFlag, final CellData cellData, final Sheet destSheet, final Row destRow) {
        if (!this.isStreaming() || !isUserFormula(cellData.getCellValue().toString())) {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, cellData, destSheet, destRow);
        } else {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, new WriteCellValueClearCellData((PoiCellData) cellData), destSheet, destRow);
        }
    }

}
