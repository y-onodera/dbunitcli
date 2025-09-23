package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.transform.poi.PoiTransformerFactory;

public class UserFormulasValueClearPoiTransformerFactory extends PoiTransformerFactory {
    @Override
    protected Transformer createTransformer(final Workbook workbook, final JxlsStreaming streaming) {
        if (streaming.isAutoDetect()) {
            return new UserFormulasValueClearStreamingPoiTransformer(workbook, getAllSheetsInWhichStreamingIsConfigured(workbook),
                    streaming.getRowAccessWindowSize(), streaming.isCompressTmpFiles(), streaming.isUseSharedStringsTable());

        } else if (streaming.getSheetNames() != null) {
            return new UserFormulasValueClearStreamingPoiTransformer(workbook, streaming.getSheetNames(),
                    streaming.getRowAccessWindowSize(), streaming.isCompressTmpFiles(), streaming.isUseSharedStringsTable());

        } else if (streaming.isStreaming()) {
            // Don't use PoiTransformer here because SelectSheetsForStreamingPoiTransformer is better.
            return new UserFormulasValueClearStreamingPoiTransformer(workbook, true,
                    streaming.getRowAccessWindowSize(), streaming.isCompressTmpFiles(), streaming.isUseSharedStringsTable());

        } else { // no streaming
            return new PoiTransformer(workbook, false);
        }
    }
}
