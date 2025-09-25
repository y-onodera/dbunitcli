package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.common.CellRef;

public record ConditionalFormatCellAddress(int index, CellRangeAddress[] addresses) {

    public CellRangeAddress[] convert(final CellRef targetCellRef) {
        final CellRangeAddress[] result = new CellRangeAddress[this.addresses.length];
        for (int i = 0, j = this.addresses.length; i < j; i++) {
            final CellRangeAddress base = this.addresses[i];
            final int offsetY = base.getLastRow() - base.getFirstRow();
            final int offsetX = base.getLastColumn() - base.getFirstColumn();
            result[i] = new CellRangeAddress(targetCellRef.getRow()
                    , targetCellRef.getRow() + offsetY
                    , targetCellRef.getCol()
                    , targetCellRef.getCol() + offsetX);
        }
        return result;
    }

    public DestConditionalFormat toDest(final SheetConditionalFormatting target) {
        return new DestConditionalFormat(target.getConditionalFormattingAt(this.index()), this);
    }
}
