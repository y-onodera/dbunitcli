package yo.dbunitcli.dataset.producer;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.NameFilter;
import yo.dbunitcli.resource.poi.XlsxSchema;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public record ComparableXlsxDataSetProducer(ComparableDataSetParam param) implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableXlsxDataSetProducer.class);

    @Override
    public Stream<Source> getSourceStream() {
        return this.getSrcFiles()
                .map(this::getSource);
    }

    @Override
    public Runnable createExecuteTableTask(final Source source, final ComparableDataSetConsumer consumer) {
        return new XlsxTableExecutor(source, consumer, this.param, this.param.tableNameFilter(), this.param.xlsxSchema());
    }

    private record XlsxTableExecutor(Source source, ComparableDataSetConsumer consumer, ComparableDataSetParam param,
                                     NameFilter sheetNameFilter, XlsxSchema schema) implements Runnable {

        @Override
        public void run() {
            ComparableXlsxDataSetProducer.LOGGER.info("produce - start filePath={}", this.source.filePath());
            try (final OPCPackage pkg = OPCPackage.open(this.source.filePath(), PackageAccess.READ)) {
                final ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg, false);
                final XSSFReader xssfReader = new XSSFReader(pkg);
                final StylesTable styles = xssfReader.getStylesTable();
                final XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
                int index = 0;
                while (iterator.hasNext()) {
                    try (final InputStream stream = iterator.next()) {
                        final String sheetName = iterator.getSheetName();
                        if (this.sheetNameFilter.predicate(sheetName) && this.schema.contains(sheetName)) {
                            ComparableXlsxDataSetProducer.LOGGER.info("produce - start sheetName={},index={}", sheetName, index++);
                            this.processSheet(styles, strings, new XlsxSchemaHandler(this.consumer
                                            , this.schema
                                            , this.param.startRow()
                                            , this.param.headerNames()
                                            , this.param.loadData()
                                            , this.source.sheetName(sheetName))
                                    , stream);
                            ComparableXlsxDataSetProducer.LOGGER.info("produce - end   sheetName={},index={}", sheetName, index - 1);
                        }
                    }
                }
            } catch (final IOException | SAXException | OpenXML4JException e) {
                throw new AssertionError(e);
            }
            ComparableXlsxDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
        }

        private void processSheet(
                final Styles styles,
                final SharedStrings strings,
                final XSSFSheetXMLHandler.SheetContentsHandler sheetHandler,
                final InputStream sheetInputStream) throws IOException, SAXException {
            final DataFormatter formatter = new DataFormatter();
            final InputSource sheetSource = new InputSource(sheetInputStream);
            try {
                final XMLReader sheetParser = XMLHelper.newXMLReader();
                final ContentHandler handler = new XSSFSheetXMLHandler(
                        styles, null, strings, sheetHandler, formatter, false);
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);
            } catch (final ParserConfigurationException e) {
                throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
            }
        }
    }
}