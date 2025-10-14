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
import org.dbunit.dataset.DataSetException;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ComparableXlsxDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableXlsxDataSetProducer.class);
    private final File[] src;
    private final int startRow;
    private final String[] headerNames;
    private final XlsxSchema schema;
    private final ComparableDataSetParam param;
    private final boolean loadData;
    private final NameFilter sheetNameFilter;
    private final boolean addFileInfo;
    private ComparableDataSetConsumer consumer;

    public ComparableXlsxDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.sheetNameFilter = this.param.tableNameFilter();
        this.startRow = this.param.startRow();
        this.headerNames = this.param.headerNames();
        this.schema = this.param.xlsxSchema();
        this.loadData = this.param.loadData();
        this.addFileInfo = this.param.addFileInfo();
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableXlsxDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .forEach(it -> this.executeTable(this.getSource(it, this.addFileInfo)));
        this.consumer.endDataSet();
        ComparableXlsxDataSetProducer.LOGGER.info("produce() - end");
    }

    @Override
    public void executeTable(final Source source) {
        ComparableXlsxDataSetProducer.LOGGER.info("produce - start filePath={}", source.filePath());
        try (final OPCPackage pkg = OPCPackage.open(source.filePath(), PackageAccess.READ)) {
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
                                        , this.startRow
                                        , this.headerNames
                                        , this.loadData
                                        , source.sheetName(sheetName))
                                , stream);
                        ComparableXlsxDataSetProducer.LOGGER.info("produce - end   sheetName={},index={}", sheetName, index - 1);
                    }
                }
            }
        } catch (final IOException | SAXException | OpenXML4JException e) {
            throw new AssertionError(e);
        }
        ComparableXlsxDataSetProducer.LOGGER.info("produce - end   filePath={}", source.filePath());
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    protected void processSheet(
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