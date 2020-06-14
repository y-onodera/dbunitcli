package yo.dbunitcli.dataset.producer;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;
import yo.dbunitcli.mapper.xlsx.XlsxSchema;
import yo.dbunitcli.mapper.xlsx.XlsxSchemaHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ComparableXlsxDataSetProducer implements ComparableDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableXlsxDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final TableNameFilter filter;
    private final XlsxSchema schema;
    private ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableXlsxDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles((file, s) -> s.endsWith(".xlsx"));
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.filter = this.param.getTableNameFilter();
        this.schema = this.param.getXlsxSchema();
        this.loadData = this.param.isLoadData();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        for (File sourceFile : this.src) {
            logger.info("produceFromFile(theDataFile={}) - start", sourceFile);

            try (OPCPackage pkg = OPCPackage.open(sourceFile)) {
                ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg, false);
                XSSFReader xssfReader = new XSSFReader(pkg);
                StylesTable styles = xssfReader.getStylesTable();
                XSSFReader.SheetIterator iterator = XSSFReader.SheetIterator.class.cast(xssfReader.getSheetsData());
                int index = 0;
                while (iterator.hasNext()) {
                    try (InputStream stream = iterator.next()) {
                        String sheetName = iterator.getSheetName();
                        if (this.filter.predicate(sheetName) && this.schema.contains(sheetName)) {
                            logger.info("produceFromSheet - start {} [index={}]", sheetName, index++);
                            processSheet(styles, strings, new XlsxSchemaHandler(this.consumer, sheetName, this.schema, this.loadData), stream);
                        }
                    }
                }
            } catch (IOException | SAXException | OpenXML4JException e) {
                throw new DataSetException(e);
            }
        }
    }

    public void processSheet(
            Styles styles,
            SharedStrings strings,
            XSSFSheetXMLHandler.SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws IOException, SAXException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }
}

