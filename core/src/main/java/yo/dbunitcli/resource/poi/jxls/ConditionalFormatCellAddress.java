package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.common.CellRef;

public record ConditionalFormatCellAddress(int index, CellRangeAddress[] addresses) {

    public CellRangeAddress[] convert(final CellRef src, final CellRef target, final CellRangeAddress[] current) {
        final CellRangeAddress[] result = new CellRangeAddress[this.addresses.length];
        for (int i = 0, j = this.addresses.length; i < j; i++) {
            final CellRangeAddress base = this.addresses[i];
            if (base.getFirstColumn() == src.getCol() && base.getFirstRow() == src.getRow()) {
                final int offsetY = base.getLastRow() - base.getFirstRow();
                final int offsetX = base.getLastColumn() - base.getFirstColumn();
                result[i] = new CellRangeAddress(target.getRow(), target.getRow() + offsetY
                        , target.getCol(), target.getCol() + offsetX);
            } else if (this.addresses().length == current.length) {
                result[i] = current[i];
            } else {
                result[i] = base;
            }
        }
        return result;
    }

    public DestConditionalFormat toDest(final SheetConditionalFormatting target) {
        return new DestConditionalFormat(target.getConditionalFormattingAt(this.index()), this);
    }
}
