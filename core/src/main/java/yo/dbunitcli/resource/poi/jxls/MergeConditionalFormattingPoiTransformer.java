package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeConditionalFormattingPoiTransformer extends PoiTransformer implements MergeConditionalFormattingTransformer {
    private final Map<String, List<ConditionalFormatCellAddress>> originFormatAddress;
    private final Map<String, Map<CellRef, List<DestConditionalFormat>>> transformedFormat = new HashMap<>();
    private final Map<CellRef, List<ConditionalFormatCellAddress>> formatSetCells = new HashMap<>();

    public MergeConditionalFormattingPoiTransformer(final Workbook workbook, final boolean streaming) {
        super(workbook, streaming);
        this.originFormatAddress = this.extractOriginFormatAddress(workbook);
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
        this.transformAndMergeFormat(srcCellRef, targetCellRef, context, cellData, destSheet, destRow
                , (writeCellData) -> {
                    super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, writeCellData, destSheet, destRow);
                });
    }

}
