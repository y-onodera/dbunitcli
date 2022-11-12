package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;
import yo.dbunitcli.resource.poi.XlsxSchema;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ComparableXlsxDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LogManager.getLogger();
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final TableNameFilter filter;
    private final XlsxSchema schema;
    private final ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableXlsxDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.filter = this.param.getTableNameFilter();
        this.schema = this.param.getXlsxSchema();
        this.loadData = this.param.isLoadData();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(final IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    public void processSheet(
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

    protected void produceFromFile(final File sourceFile) {
        LOGGER.info("produce - start fileName={}", sourceFile);
        try (final OPCPackage pkg = OPCPackage.open(sourceFile, PackageAccess.READ)) {
            final ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg, false);
            final XSSFReader xssfReader = new XSSFReader(pkg);
            final StylesTable styles = xssfReader.getStylesTable();
            final XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            int index = 0;
            while (iterator.hasNext()) {
                try (final InputStream stream = iterator.next()) {
                    final String sheetName = iterator.getSheetName();
                    if (this.filter.predicate(sheetName) && this.schema.contains(sheetName)) {
                        LOGGER.info("produce - start sheetName={},index={}", sheetName, index++);
                        this.processSheet(styles, strings, this.schema.createHandler(this.consumer, sheetName, this.loadData), stream);
                        LOGGER.info("produce - end   sheetName={},index={}", sheetName, index - 1);
                    }
                }
            }
        } catch (final IOException | SAXException | OpenXML4JException e) {
            throw new AssertionError(e);
        }
        LOGGER.info("produce - end   fileName={}", sourceFile);
    }
}

