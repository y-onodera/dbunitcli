package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparableXlsDataSetProducer implements IDataSetProducer, HSSFListener {

    private static final Logger logger = LoggerFactory.getLogger(ComparableXlsxDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private File src;
    private POIFSFileSystem fs;
    private String tableName;
    private List<String> rowValues = Lists.newArrayList();
    private ITableMetaData metaData;
    private boolean isStartTable;

    private int lastRowNumber;
    private int lastColumnNumber;

    /**
     * For parsing Formulas
     */
    private EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;

    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;

    /**
     * So we known which sheet we're on
     */
    private int sheetIndex = -1;
    private BoundSheetRecord[] orderedBSRs;
    private List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();

    public ComparableXlsDataSetProducer(File src) {
        this.src = src;
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }


    @Override
    public void produce() throws DataSetException {
        logger.debug("produceFromFile(theDataFile={}) - start", src);

        try {
            this.fs = new POIFSFileSystem(this.src);
            MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
            this.formatListener = new FormatTrackingHSSFListener(listener);

            HSSFEventFactory factory = new HSSFEventFactory();
            HSSFRequest request = new HSSFRequest();

            this.workbookBuildingListener = new EventWorkbookBuilder.SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(this.workbookBuildingListener);
            this.consumer.startDataSet();
            factory.processWorkbookEvents(request, this.fs);
            if (this.isStartTable) {
                this.consumer.endTable();
            }
            this.consumer.endDataSet();
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void processRecord(Record record) {
        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;

        switch (record.getSid()) {
            case BoundSheetRecord.sid:
                this.boundSheetRecords.add((BoundSheetRecord) record);
                break;
            case BOFRecord.sid:
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // Create sub workbook if required
                    if (this.stubWorkbook == null) {
                        this.stubWorkbook = this.workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    // Output the worksheet name
                    // Works by ordering the BSRs by the location of
                    //  their BOFRecords, and then knowing that we
                    //  process BOFRecords in byte offset order
                    this.sheetIndex++;
                    if (this.orderedBSRs == null) {
                        this.orderedBSRs = BoundSheetRecord.orderByBofPosition(this.boundSheetRecords);
                    }
                    this.tableName = this.orderedBSRs[this.sheetIndex].getSheetname();
                }
                break;

            case SSTRecord.sid:
                this.sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                BlankRecord blankRec = (BlankRecord) record;

                thisRow = blankRec.getRow();
                thisColumn = blankRec.getColumn();
                thisStr = "";
                break;
            case BoolErrRecord.sid:
                BoolErrRecord boolErrorRec = (BoolErrRecord) record;

                thisRow = boolErrorRec.getRow();
                thisColumn = boolErrorRec.getColumn();
                thisStr = "";
                break;

            case FormulaRecord.sid:
                FormulaRecord formulaRec = (FormulaRecord) record;
                thisRow = formulaRec.getRow();
                thisColumn = formulaRec.getColumn();
                thisStr = HSSFFormulaParser.toFormulaString(this.stubWorkbook, formulaRec.getParsedExpression());
                break;
            case StringRecord.sid:
                break;

            case LabelRecord.sid:
                LabelRecord labelRec = (LabelRecord) record;
                thisRow = labelRec.getRow();
                thisColumn = labelRec.getColumn();
                thisStr = labelRec.getValue() ;
                break;
            case LabelSSTRecord.sid:
                LabelSSTRecord labelSSTRec = (LabelSSTRecord) record;

                thisRow = labelSSTRec.getRow();
                thisColumn = labelSSTRec.getColumn();
                if (this.sstRecord == null) {
                    thisStr = "(No SST Record, can't identify string)";
                } else {
                    thisStr = this.sstRecord.getString(labelSSTRec.getSSTIndex()).toString();
                }
                break;
            case NoteRecord.sid:
                // note ignore
                thisColumn = -1;
                break;
            case NumberRecord.sid:
                NumberRecord numRec = (NumberRecord) record;

                thisRow = numRec.getRow();
                thisColumn = numRec.getColumn();

                // Format
                thisStr = this.formatListener.formatNumberDateCell(numRec);
                break;
            case RKRecord.sid:
                // rk ignore
                thisColumn = -1;
                break;
            default:
                break;
        }

        // Handle new row
        if (thisRow != -1 && thisRow != this.lastRowNumber) {
            this.lastColumnNumber = -1;
        }

        // Handle missing column
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            thisStr = "";
        }

        // If we got something to print out, do so
        if (thisStr != null) {
            if (thisColumn >= 0) {
                this.rowValues.add(thisStr);
            }
        }

        // Update column and row count
        if (thisRow > -1) {
            this.lastRowNumber = thisRow;
        }
        if (thisColumn > -1) {
            this.lastColumnNumber = thisColumn;
        }
        // Handle end of row
        if (record instanceof LastCellOfRowDummyRecord) {
            // Columns are 0 based
            if (this.lastColumnNumber == -1) {
                this.lastColumnNumber = 0;
            }
            try {
                if (this.lastRowNumber == 0) {
                    if (this.isStartTable) {
                        this.consumer.endTable();
                    }
                    final Column[] columns = new Column[this.rowValues.size()];
                    for (int i = 0, j = this.rowValues.size(); i < j; i++) {
                        columns[i] = new Column(this.rowValues.get(i).toString(), DataType.UNKNOWN);
                    }
                    this.metaData = new DefaultTableMetaData(this.tableName, columns);
                    this.consumer.startTable(metaData);
                    this.isStartTable = true;
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
                throw new AssertionError(e);
            }

            // We're onto a new row
            this.rowValues.clear();
            this.lastColumnNumber = -1;
        }

    }
}
