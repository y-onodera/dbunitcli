package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.*;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiCellData;
import org.jxls.transform.poi.SelectSheetsForStreamingPoiTransformer;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UserFormulasValueClearStreamingPoiTransformer extends SelectSheetsForStreamingPoiTransformer {
    private static final String USER_FORMULA_PREFIX = "$[";
    private static final String USER_FORMULA_SUFFIX = "]";
    private final Map<String, Map<CellRef, List<DestConditionalFormat>>> transFormedFormat = new HashMap<>();
    private final Map<String, List<ConditionalFormatCellAddress>> originFormatAddress = new HashMap<>();
    private final Map<CellRef, List<ConditionalFormatCellAddress>> formatSetCells = new HashMap<>();

    private static boolean isUserFormula(final String str) {
        return str.startsWith(USER_FORMULA_PREFIX) && str.endsWith(USER_FORMULA_SUFFIX);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final boolean allSheets, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, allSheets, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
        this.copyOriginFormat(workbook);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final Set<String> sheetNames, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, sheetNames, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
        this.copyOriginFormat(workbook);
    }

    @Override
    protected void transformCell(final CellRef srcCellRef, final CellRef targetCellRef, final Context context, final boolean updateRowHeightFlag, final CellData cellData, final Sheet destSheet, final Row destRow) {
        this.prepareConditionalFormat(srcCellRef, destSheet);
        final int before = destSheet.getSheetConditionalFormatting().getNumConditionalFormattings();

        if (!this.isStreaming() || !isUserFormula(cellData.getCellValue().toString())) {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, cellData, destSheet, destRow);
        } else {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, new WriteCellValueClearCellData((PoiCellData) cellData), destSheet, destRow);
        }
        this.mergeConditionalFormat(srcCellRef, targetCellRef, destSheet, before);
    }

    private void copyOriginFormat(final Workbook workbook) {
        final int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            final Sheet sheet = workbook.getSheetAt(i);
            final SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
            this.originFormatAddress.put(sheet.getSheetName(), IntStream.range(0, formatting.getNumConditionalFormattings())
                    .mapToObj(index ->
                            new ConditionalFormatCellAddress(index, formatting.getConditionalFormattingAt(index)
                                    .getFormattingRanges()))
                    .toList());
        }
    }


    private void prepareConditionalFormat(final CellRef srcCellRef, final Sheet destSheet) {
        if (!this.transFormedFormat.containsKey(destSheet.getSheetName())) {
            this.transFormedFormat.put(destSheet.getSheetName(), new HashMap<>());
        }
        if (!this.formatSetCells.containsKey(srcCellRef)) {
            final List<ConditionalFormatCellAddress> src = this.originFormatAddress.get(srcCellRef.getSheetName());
            final List<ConditionalFormatCellAddress> formatIndex = src
                    .stream()
                    .filter(it -> Stream.of(it.addresses())
                            .anyMatch(range -> range.getFirstRow() == srcCellRef.getRow() && range.getFirstColumn() == srcCellRef.getCol()))
                    .toList();
            if (!formatIndex.isEmpty()) {
                this.formatSetCells.put(srcCellRef, formatIndex);
            }
        }
    }

    private void mergeConditionalFormat(final CellRef srcCellRef, final CellRef targetCellRef, final Sheet destSheet, final int before) {
        final SheetConditionalFormatting sheetCF = destSheet.getSheetConditionalFormatting();
        if (this.formatSetCells.containsKey(srcCellRef)) {
            this.removeUnnecessaryFormat(targetCellRef, before, sheetCF);
        } else {
            final Map<CellRef, List<DestConditionalFormat>> srcCellRules = this.transFormedFormat.get(destSheet.getSheetName());
            if (srcCellRules.containsKey(srcCellRef)) {
                this.removeUnnecessaryFormat(targetCellRef, before, sheetCF);
                srcCellRules.get(srcCellRef).forEach(it -> it.merge(targetCellRef));
            } else {
                srcCellRules.put(srcCellRef, this.formatSetCells.get(srcCellRef)
                        .stream()
                        .map(it -> it.toDest(sheetCF))
                        .toList());
                srcCellRules.get(srcCellRef).forEach(it -> it.convert(targetCellRef));
            }
        }
    }

    private void removeUnnecessaryFormat(final CellRef targetCellRef, final int before, final SheetConditionalFormatting sheetCF) {
        final List<Integer> removeIndexes = new ArrayList<>();
        for (int i = Math.max(0, before - 1), j = sheetCF.getNumConditionalFormattings(); i < j; i++) {
            final ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(i);
            if (Stream.of(cf.getFormattingRanges())
                    .anyMatch(it -> it.isInRange(targetCellRef.getRow(), targetCellRef.getCol()))) {
                removeIndexes.add(i);
            }
        }
        removeIndexes.reversed().forEach(sheetCF::removeConditionalFormatting);
    }
}
