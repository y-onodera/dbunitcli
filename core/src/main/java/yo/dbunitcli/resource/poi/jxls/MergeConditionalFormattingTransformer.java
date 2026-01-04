package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiCellData;
import yo.dbunitcli.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MergeConditionalFormattingTransformer {

    Map<String, List<ConditionalFormatCellAddress>> getOriginFormatAddress();

    Map<CellRef, List<ConditionalFormatCellAddress>> getFormatSetCells();

    Map<String, Map<CellRef, List<DestConditionalFormat>>> getTransformedFormat();

    default Map<String, List<ConditionalFormatCellAddress>> extractOriginFormatAddress(final Workbook workbook) {
        final Map<String, List<ConditionalFormatCellAddress>> result = new HashMap<>();
        final int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            final Sheet sheet = workbook.getSheetAt(i);
            final SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
            result.put(sheet.getSheetName(), IntStream.range(0, formatting.getNumConditionalFormattings())
                    .mapToObj(index ->
                            new ConditionalFormatCellAddress(index, formatting.getConditionalFormattingAt(index)
                                    .getFormattingRanges()))
                    .toList());
        }
        return result;
    }

    default void transformAndMergeFormat(final CellRef srcCellRef, final CellRef targetCellRef, Context context, CellData cellData, final Sheet destSheet, Row destRow, final Consumer<CaptureWriteCellCellData> transformer) {
        this.prepareConditionalFormat(srcCellRef, destSheet);
        final int originFormatNum = destSheet.getSheetConditionalFormatting().getNumConditionalFormattings();
        CaptureWriteCellCellData writeCellData = new CaptureWriteCellCellData((PoiCellData) cellData);
        transformer.accept(writeCellData);
        if (cellData.getCellValue() != null && Strings.isNotEmpty(cellData.getCellValue().toString())) {
            final Object evaluationResult = cellData.evaluate(context);
            if (evaluationResult == null || evaluationResult instanceof final String strValue && strValue.isEmpty()) {
                destRow.removeCell(writeCellData.getCell());
            }
        }
        this.mergeConditionalFormat(srcCellRef, targetCellRef, destSheet, originFormatNum);
    }

    default void prepareConditionalFormat(final CellRef srcCellRef, final Sheet destSheet) {
        if (!this.getTransformedFormat().containsKey(destSheet.getSheetName())) {
            this.getTransformedFormat().put(destSheet.getSheetName(), new HashMap<>());
        }
        if (!this.getFormatSetCells().containsKey(srcCellRef)) {
            final List<ConditionalFormatCellAddress> src = this.getOriginFormatAddress().get(srcCellRef.getSheetName());
            final List<ConditionalFormatCellAddress> formatIndex = src
                    .stream()
                    .filter(it -> Stream.of(it.addresses())
                            .anyMatch(range -> range.getFirstRow() == srcCellRef.getRow() && range.getFirstColumn() == srcCellRef.getCol()))
                    .toList();
            if (!formatIndex.isEmpty()) {
                this.getFormatSetCells().put(srcCellRef, formatIndex);
            }
        }
    }

    default void mergeConditionalFormat(final CellRef srcCellRef, final CellRef targetCellRef, final Sheet destSheet, final int originFormatNum) {
        final SheetConditionalFormatting resultConditionalFormat = destSheet.getSheetConditionalFormatting();
        IntStream.range(originFormatNum, resultConditionalFormat.getNumConditionalFormattings())
                .boxed()
                .toList()
                .reversed()
                .forEach(resultConditionalFormat::removeConditionalFormatting);
        if (this.getFormatSetCells().containsKey(srcCellRef)) {
            final Map<CellRef, List<DestConditionalFormat>> srcCellRules = this.getTransformedFormat().get(destSheet.getSheetName());
            if (srcCellRules.containsKey(srcCellRef)) {
                srcCellRules.get(srcCellRef).forEach(it -> it.merge(srcCellRef, targetCellRef));
            } else {
                srcCellRules.put(srcCellRef, this.getFormatSetCells().get(srcCellRef)
                        .stream()
                        .map(it -> it.toDest(resultConditionalFormat))
                        .toList());
                srcCellRules.get(srcCellRef).forEach(it -> it.convert(srcCellRef, targetCellRef));
            }
        }
    }
}
