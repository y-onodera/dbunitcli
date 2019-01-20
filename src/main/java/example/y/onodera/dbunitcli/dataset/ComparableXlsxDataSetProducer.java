package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ComparableXlsxDataSetProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableXlsxDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private File src;

    public ComparableXlsxDataSetProducer(File file) {
        this.src = file;
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produceFromFile(theDataFile={}) - start", src);

        try {
            OPCPackage pkg = OPCPackage.open(this.src);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iterator = XSSFReader.SheetIterator.class.cast(xssfReader.getSheetsData());
            int index = 0;
            while (iterator.hasNext()) {
                try (InputStream stream = iterator.next()) {
                    String sheetName = iterator.getSheetName();
                    logger.debug("produceFromSheet - start", sheetName + " [index=" + index + "]:");
                    processSheet(styles, strings, new SheetToTable(sheetName, this.consumer), stream);
                }
            }
        } catch (IOException | SAXException | OpenXML4JException | ParserConfigurationException e) {
            throw new DataSetException(e);
        }
    }

    public void processSheet(
            Styles styles,
            SharedStrings strings,
            XSSFSheetXMLHandler.SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws IOException, SAXException, ParserConfigurationException {
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

    private class SheetToTable implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final String sheetName;
        private final IDataSetConsumer consumer;
        private ITableMetaData metaData;
        private int currentRow = -1;
        private int currentCol = -1;
        private List<Object> rowValues = Lists.newArrayList();

        public SheetToTable(String aSheetName, IDataSetConsumer aConsumer) {
            this.sheetName = aSheetName;
            this.consumer = aConsumer;
        }

        @Override
        public void startRow(int rowNum) {
            this.currentRow = rowNum;
            this.currentCol = -1;
        }

        @Override
        public void endRow(int rowNum) {
            try {
                if (rowNum == 0) {
                    final Column[] columns = new Column[this.rowValues.size()];
                    for (int i = 0, j = this.rowValues.size(); i < j; i++) {
                        columns[i] = new Column(this.rowValues.get(i).toString(), DataType.UNKNOWN);
                    }
                    this.metaData = new DefaultTableMetaData(this.sheetName, columns);
                    this.consumer.startTable(metaData);
                } else {
                    if (metaData.getColumns().length < this.rowValues.size()) {
                        throw new AssertionError(this.rowValues + " large items than header:" + Arrays.toString(this.metaData.getColumns()));
                    } else if (this.rowValues.size() < metaData.getColumns().length) {
                        for (int i = this.rowValues.size(), j = metaData.getColumns().length; i < j; i++) {
                            this.rowValues.add("");
                        }
                    }
                    this.consumer.row(this.rowValues.toArray(new Object[this.rowValues.size()]));
                }
            } catch (DataSetException e) {
                throw new RuntimeException(e);
            }
            this.rowValues.clear();
        }

        @Override
        public void cell(String cellReference, String formattedValue,
                         XSSFComment comment) {
            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if (cellReference == null) {
                cellReference = new CellAddress(this.currentRow, this.currentCol).formatAsString();
            }

            int thisCol = (new CellReference(cellReference)).getCol();
            int missedCols = thisCol - this.currentCol - 1;
            for (int i = 0; i < missedCols; i++) {
                this.rowValues.add(null);
            }
            this.currentCol = thisCol;
            this.rowValues.add(formattedValue);
        }

        @Override
        public void endSheet() {
            try {
                this.consumer.endTable();
            } catch (DataSetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

