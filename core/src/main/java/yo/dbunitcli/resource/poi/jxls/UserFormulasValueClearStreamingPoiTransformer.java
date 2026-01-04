package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.SelectSheetsForStreamingPoiTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserFormulasValueClearStreamingPoiTransformer extends SelectSheetsForStreamingPoiTransformer implements MergeConditionalFormattingTransformer {
    private static final String USER_FORMULA_PREFIX = "$[";
    private static final String USER_FORMULA_SUFFIX = "]";
    private final Map<String, List<ConditionalFormatCellAddress>> originFormatAddress;
    private final Map<String, Map<CellRef, List<DestConditionalFormat>>> transformedFormat = new HashMap<>();
    private final Map<CellRef, List<ConditionalFormatCellAddress>> formatSetCells = new HashMap<>();

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final boolean allSheets, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, allSheets, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
        this.originFormatAddress = this.extractOriginFormatAddress(workbook);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final Set<String> sheetNames, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, sheetNames, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
        this.originFormatAddress = this.extractOriginFormatAddress(workbook);
    }

    private static boolean isUserFormula(final String str) {
        return str.startsWith(USER_FORMULA_PREFIX) && str.endsWith(USER_FORMULA_SUFFIX);
    }

    @Override
    public Map<String, List<ConditionalFormatCellAddress>> getOriginFormatAddress() {
        return this.originFormatAddress;
    }

    @Override
    public Map<CellRef, List<ConditionalFormatCellAddress>> getFormatSetCells() {
        return this.formatSetCells;
    }

    @Override
    public Map<String, Map<CellRef, List<DestConditionalFormat>>> getTransformedFormat() {
        return this.transformedFormat;
    }

    @Override
    protected void transformCell(final CellRef srcCellRef, final CellRef targetCellRef, final Context context, final boolean updateRowHeightFlag, final CellData cellData, final Sheet destSheet, final Row destRow) {
        this.transformAndMergeFormat(srcCellRef, targetCellRef, context, cellData, destSheet
                , destRow, (wrappedCellData) -> {
                    if (this.isStreaming() && cellData.getCellValue() != null && isUserFormula(cellData.getCellValue().toString())) {
                        super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, new WriteCellValueClearCellData(wrappedCellData), destSheet, destRow);
                    } else {
                        super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, wrappedCellData, destSheet, destRow);
                    }
                });
    }

}
