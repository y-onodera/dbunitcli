package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;
import yo.dbunitcli.resource.poi.ExcelMappingDataSetConsumerWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComparableXlsDataSetProducer extends ExcelMappingDataSetConsumerWrapper implements ComparableDataSetProducer, HSSFListener {

    private static final Logger LOGGER = LogManager.getLogger();
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;

    private final File[] src;

    private int lastRowNumber;

    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;

    /**
     * So we known which sheet we're on
     */
    private int sheetIndex = -1;
    private BoundSheetRecord[] orderedBSRs;
    private List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();
    // For handling formulas with string results
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;

    public ComparableXlsDataSetProducer(ComparableDataSetParam param) {
        super(new DefaultConsumer(), param.getXlsxSchema(), param.isLoadData());
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.filter = this.param.getTableNameFilter();
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
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        for (File sourceFile : this.src) {
            if (!filter.predicate(sourceFile.getName())) {
                continue;
            }
            LOGGER.info("produce - start fileName={}", sourceFile);

            try (POIFSFileSystem newFs = new POIFSFileSystem(sourceFile)) {
                this.rowsTableBuilder = null;
                this.randomCellRecordBuilder = null;
                this.sheetIndex = -1;
                this.boundSheetRecords = new ArrayList<>();
                this.orderedBSRs = null;
                MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
                this.formatListener = new FormatTrackingHSSFListener(listener);

                HSSFEventFactory factory = new HSSFEventFactory();
                HSSFRequest request = new HSSFRequest();
                request.addListenerForAllRecords(this.formatListener);
                factory.processWorkbookEvents(request, newFs);
                this.handleSheetEnd();
                LOGGER.info("produce - end   sheetName={},index={}", this.orderedBSRs[this.sheetIndex].getSheetname(), this.sheetIndex);
            } catch (IOException e) {
                throw new DataSetException(e);
            }
            LOGGER.info("produce - end   fileName={}", sourceFile);
        }
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
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
                    this.handleSheetEnd();
                    // Output the worksheet name
                    // Works by ordering the BSRs by the location of
                    //  their BOFRecords, and then knowing that we
                    //  process BOFRecords in byte offset order
                    this.sheetIndex++;
                    if (this.orderedBSRs == null) {
                        this.orderedBSRs = BoundSheetRecord.orderByBofPosition(this.boundSheetRecords);
                    } else {
                        LOGGER.info("produce - end   sheetName={},index={}", this.orderedBSRs[this.sheetIndex - 1].getSheetname(), this.sheetIndex - 1);
                    }
                    String tableName = this.orderedBSRs[this.sheetIndex].getSheetname();
                    this.handleSheetStart(tableName);
                    LOGGER.info("produce - start sheetName={},index={}", tableName, this.sheetIndex);
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
                if (formulaRec.hasCachedResultString()) {
                    // Formula result is a string
                    // This is stored in the next record
                    this.outputNextStringRecord = true;
                    this.nextRow = thisRow;
                    this.nextColumn = thisColumn;
                } else {
                    thisStr = this.formatListener.formatNumberDateCell(formulaRec);
                }
                break;
            case StringRecord.sid:
                if (this.outputNextStringRecord) {
                    // String for formula
                    StringRecord srec = (StringRecord) record;
                    thisStr = srec.getString();
                    thisRow = this.nextRow;
                    thisColumn = this.nextColumn;
                    this.outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord labelRec = (LabelRecord) record;
                thisRow = labelRec.getRow();
                thisColumn = labelRec.getColumn();
                thisStr = labelRec.getValue();
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
                break;
            default:
                break;
        }

        // Handle missing column
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            thisStr = "";
        }
        if (this.randomCellRecordBuilder != null && thisStr != null) {
            String cellReference = new CellAddress(thisRow, thisColumn).formatAsString();
            CellReference reference = new CellReference(cellReference);
            this.handleCellValue(thisColumn, thisStr, reference);
        }
        // Update column and row count
        if (thisRow > -1) {
            this.lastRowNumber = thisRow;
        }
        // Handle end of row
        if (record instanceof LastCellOfRowDummyRecord) {
            this.addNewRowToRowsTable(this.lastRowNumber);
        }
    }

}
