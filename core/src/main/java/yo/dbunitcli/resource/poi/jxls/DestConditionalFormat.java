package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.jxls.common.CellRef;

import java.util.stream.Stream;

record DestConditionalFormat(ConditionalFormatting formatting, ConditionalFormatCellAddress address) {
    public void merge(final CellRef targetCellRef) {
        this.formatting.setFormattingRanges(
                CellRangeUtil.mergeCellRanges(
                        Stream.concat(
                                        Stream.of(this.formatting().getFormattingRanges())
                                        , Stream.of(this.address().convert(targetCellRef)))
                                .toList()
                                .toArray(new CellRangeAddress[0])
                )
        );
    }

    public void convert(final CellRef targetCellRef) {
        this.formatting.setFormattingRanges(this.address().convert(targetCellRef));
    }
}
