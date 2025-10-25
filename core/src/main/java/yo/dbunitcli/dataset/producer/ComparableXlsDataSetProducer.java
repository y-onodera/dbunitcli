package yo.dbunitcli.dataset.producer;

import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.NameFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ComparableXlsDataSetProducer extends ExcelMappingDataSetProducer implements ComparableDataSetProducer, HSSFListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableXlsDataSetProducer.class);
    private final ComparableDataSetParam param;

    private final NameFilter sheetNameFilter;

    private int lastRowNumber;

    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;

    /**
     * So we known which sheet we're on
     */
    private File sourceFile;
    private int sheetIndex = -1;
    private BoundSheetRecord[] orderedBSRs;
    private List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();
    // For handling formulas with string results
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;

    public ComparableXlsDataSetProducer(final ComparableDataSetParam param) {
        super(param.xlsxSchema(), param.startRow(), param.headerNames(), param.loadData(), param.addFileInfo());
        this.param = param;
        this.sheetNameFilter = param.tableNameFilter();
    }

    @Override
    public ComparableDataSetConsumer getConsumer() {
        return this.consumer;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Arrays.stream(this.getSrcFiles())
                .map(this::getSource);
    }

    @Override
    public void executeTable(final Source source) {
        this.sourceFile = new File(source.filePath());
        ComparableXlsDataSetProducer.LOGGER.info("produce - start fileName={}", this.sourceFile);
        try (final POIFSFileSystem newFs = new POIFSFileSystem(this.sourceFile, true)) {
            this.rowsTableBuilder = null;
            this.randomCellRecordBuilder = null;
            this.sheetIndex = -1;
            this.boundSheetRecords = new ArrayList<>();
            this.orderedBSRs = null;
            final MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
            this.formatListener = new FormatTrackingHSSFListener(listener);

            final HSSFEventFactory factory = new HSSFEventFactory();
            final HSSFRequest request = new HSSFRequest();
            request.addListenerForAllRecords(this.formatListener);
            factory.processWorkbookEvents(request, newFs);
            this.handleSheetEnd();
            ComparableXlsDataSetProducer.LOGGER.info("produce - end   sheetName={},index={}", this.orderedBSRs[this.sheetIndex].getSheetname(), this.sheetIndex);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        ComparableXlsDataSetProducer.LOGGER.info("produce - end   fileName={}", this.sourceFile);
    }

    @Override
    public void processRecord(final Record record) {
        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;

        switch (record.getSid()) {
            case BoundSheetRecord.sid:
                this.boundSheetRecords.add((BoundSheetRecord) record);
                break;
            case BOFRecord.sid:
                final BOFRecord br = (BOFRecord) record;
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
                        ComparableXlsDataSetProducer.LOGGER.info("produce - end   sheetName={},index={}", this.orderedBSRs[this.sheetIndex - 1].getSheetname(), this.sheetIndex - 1);
                    }
                    final String tableName = this.orderedBSRs[this.sheetIndex].getSheetname();
                    if (this.sheetNameFilter.predicate(tableName)) {
                        this.handleSheetStart(this.headerNames
                                , new Source(this.sourceFile, this.addFileInfo).sheetName(tableName));
                        ComparableXlsDataSetProducer.LOGGER.info("produce - start sheetName={},index={}", tableName, this.sheetIndex);
                    }
                }
                break;

            case SSTRecord.sid:
                this.sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                final BlankRecord blankRec = (BlankRecord) record;

                thisRow = blankRec.getRow();
                thisColumn = blankRec.getColumn();
                thisStr = "";
                break;
            case BoolErrRecord.sid:
                final BoolErrRecord boolErrorRec = (BoolErrRecord) record;

                thisRow = boolErrorRec.getRow();
                thisColumn = boolErrorRec.getColumn();
                thisStr = "";
                break;

            case FormulaRecord.sid:
                final FormulaRecord formulaRec = (FormulaRecord) record;
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
                    final StringRecord srec = (StringRecord) record;
                    thisStr = srec.getString();
                    thisRow = this.nextRow;
                    thisColumn = this.nextColumn;
                    this.outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                final LabelRecord labelRec = (LabelRecord) record;
                thisRow = labelRec.getRow();
                thisColumn = labelRec.getColumn();
                thisStr = labelRec.getValue();
                break;
            case LabelSSTRecord.sid:
                final LabelSSTRecord labelSSTRec = (LabelSSTRecord) record;

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
                final NumberRecord numRec = (NumberRecord) record;

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
        if (record instanceof final MissingCellDummyRecord mc) {
            thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            thisStr = "";
        }
        if (this.randomCellRecordBuilder != null && thisStr != null) {
            final String cellReference = new CellAddress(thisRow, thisColumn).formatAsString();
            final CellReference reference = new CellReference(cellReference);
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
