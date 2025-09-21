package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformerFactory;

public class UserFormulasValueClearPoiTransformerFactory extends PoiTransformerFactory {
    @Override
    protected Transformer createTransformer(final Workbook workbook, final JxlsStreaming streaming) {
        return new UserFormulasValueClearStreamingPoiTransformer(workbook, true,
                streaming.getRowAccessWindowSize(), streaming.isCompressTmpFiles(), streaming.isUseSharedStringsTable());
    }
}
