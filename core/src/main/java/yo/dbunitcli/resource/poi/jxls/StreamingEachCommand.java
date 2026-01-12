package yo.dbunitcli.resource.poi.jxls;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.jxls.area.Area;
import org.jxls.area.XlsArea;
import org.jxls.command.CellRefGenerator;
import org.jxls.command.Command;
import org.jxls.command.EachCommand;
import org.jxls.command.RunVar;
import org.jxls.common.*;
import org.jxls.expression.ExpressionEvaluator;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.util.LinkedHashMap;
import java.util.Map;

public class StreamingEachCommand extends EachCommand {
    private Area area;

    public StreamingEachCommand() {
        super();
    }

    public StreamingEachCommand(final String var, final String items, final Direction direction) {
        super(var, items, direction);
    }

    public StreamingEachCommand(final String items, final Area area) {
        this(null, items, area);
    }

    public StreamingEachCommand(final String var, final String items, final Area area) {
        super(var, items, area, Direction.DOWN);
    }

    public StreamingEachCommand(final String var, final String items, final Area area, final Direction direction) {
        super(var, items, area, direction);
    }

    public StreamingEachCommand(final String var, final String items, final Area area, final CellRefGenerator cellRefGenerator) {
        super(var, items, area, cellRefGenerator);
    }

    @Override
    public Command addArea(final Area area) {
        if (area == null) {
            return this;
        }
        if (!this.areaList.isEmpty()) {
            throw new JxlsException("You can add only a single area to 'each' command");
        }
        this.area = area;
        return super.addArea(area);
    }

    @Override
    public Size applyAt(final CellRef cellRef, final Context context) {
        this.ifDirectionRightFillStaticCells(context);
        final Object evaluated;
        try {
            evaluated = context.evaluate(this.getItems());
        } catch (final Exception e) {
            this.getLogger().handleEvaluationException(e, cellRef.toString(), this.getItems());
            return new Size(0, 0);
        }
        if (evaluated == null) {
            return new Size(0, 0);
        }
        if (evaluated instanceof final ComparableTableMappingTask.WithTargetTable task) {
            final JxlsDataSetConverter converter = new JxlsDataSetConverter(cellRef, context, task.targetTableName(), this.area, this.getCellRefGenerator());
            final ComparableTableMappingContext mappingContext = new ComparableTableMappingContext(task.targetTableSeparators(), converter);
            mappingContext.open();
            task.run(mappingContext);
            mappingContext.close();

            final Size size = converter.getSize();
            if (this.getDirection() == Direction.DOWN) {
                this.getTransformer().adjustTableSize(cellRef, size);
            }
            return size;
        }
        return super.applyAt(cellRef, context);
    }

    private void ifDirectionRightFillStaticCells(Context context) {
        if (getTransformer().isForwardOnly() && getDirection() == Direction.RIGHT) {
            final CellRef startCellRef = area.getStartCellRef();
            final CellRef lastCellRef = area.getAreaRef().getLastCellRef();
            final int startCol = startCellRef.getCol();
            if (startCol > 0) {
                final CellRef fixedAreaStart = new CellRef(startCellRef.getSheetName(), startCellRef.getRow(), 0);
                final CellRef fixedAreaLast = new CellRef(startCellRef.getSheetName(), lastCellRef.getRow(), startCellRef.getCol() - 1);
                final AreaRef fixedArea = new AreaRef(fixedAreaStart, fixedAreaLast);
                new XlsArea(fixedArea, getTransformer()).applyAt(fixedAreaStart, context);
            }
        }
    }

    private static class StreamingState {
        int index = 0;
        int currentIndex = 0;
        CellRef currentCell;
        int newWidth = 0;
        int newHeight = 0;

        StreamingState(final CellRef initialCell) {
            this.currentCell = initialCell;
        }

        int getAndIncrementIndex() {
            return this.index++;
        }

        void incrementCurrentIndex() {
            this.currentIndex++;
        }

        void updateMaxSize(final Size size) {
            this.newWidth = Math.max(this.newWidth, size.getWidth());
            this.newHeight = Math.max(this.newHeight, size.getHeight());
        }

        void advanceCellAndUpdateSize(final Size size, final Direction direction) {
            if (direction == Direction.DOWN) {
                this.currentCell = new CellRef(this.currentCell.getSheetName(), this.currentCell.getRow() + size.getHeight(), this.currentCell.getCol());
                this.newWidth = Math.max(this.newWidth, size.getWidth());
                this.newHeight += size.getHeight();
            } else { // RIGHT
                this.currentCell = new CellRef(this.currentCell.getSheetName(), this.currentCell.getRow(), this.currentCell.getCol() + size.getWidth());
                this.newWidth += size.getWidth();
                this.newHeight = Math.max(this.newHeight, size.getHeight());
            }
        }
    }

    private class JxlsDataSetConverter implements IDataSetConverter {
        private final StreamingState state;
        private final Context context;
        private final ExpressionEvaluator selectEvaluator;
        private final String targetTableName;
        private final Area area;
        private final CellRefGenerator cellRefGenerator;
        private RunVar runVar;
        private ITableMetaData currentMetaData;

        private JxlsDataSetConverter(final CellRef cellRef, final Context context, final String tableName, Area area, CellRefGenerator cellRefGenerator) {
            this.state = new StreamingState(cellRef);
            this.context = context;
            this.selectEvaluator = this.getExpressionEvaluator(context, StreamingEachCommand.this.getSelect());
            this.targetTableName = tableName;
            this.area = area;
            this.cellRefGenerator = cellRefGenerator;
        }

        private JxlsDataSetConverter(final JxlsDataSetConverter copyFrom) {
            this.state = copyFrom.state;
            this.context = copyFrom.context;
            this.selectEvaluator = copyFrom.selectEvaluator;
            this.targetTableName = copyFrom.targetTableName;
            this.runVar = copyFrom.runVar;
            this.currentMetaData = copyFrom.currentMetaData;
            this.area = copyFrom.area;
            this.cellRefGenerator = copyFrom.cellRefGenerator;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.currentMetaData = metaData;
            this.runVar = new RunVar(StreamingEachCommand.this.getVar(), StreamingEachCommand.this.getVarIndex(), this.context);
        }

        @Override
        public void endTable() {
            if (this.runVar != null) {
                this.runVar.close();
                this.runVar = null;
            }
            this.currentMetaData = null;
        }

        @Override
        public void row(final Object[] values) {
            if (!this.currentMetaData.getTableName().equals(this.targetTableName)) {
                return;
            }
            this.runVar.put(this.createRowMap(values), this.state.currentIndex);
            if (this.selectEvaluator != null && !this.selectEvaluator.isConditionTrue(this.context)) {
                this.state.incrementCurrentIndex();
                return;
            }
            if (this.cellRefGenerator != null) {
                this.state.currentCell = this.cellRefGenerator.generateCellRef(this.state.getAndIncrementIndex(), this.context, StreamingEachCommand.this.getLogger());
            }
            if (this.state.currentCell == null) {
                return;
            }
            this.transformRow(this.state.currentCell);
        }

        private Map<String, Object> createRowMap(Object[] values) {
            final Map<String, Object> rowMap = new LinkedHashMap<>();
            try {
                for (int i = 0; i < values.length && i < this.currentMetaData.getColumns().length; i++) {
                    rowMap.put(this.currentMetaData.getColumns()[i].getColumnName(), values[i]);
                }
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
            return rowMap;
        }

        private void transformRow(CellRef currentCell) {
            final Size size;
            try {
                size = this.area.applyAt(currentCell, this.context);
            } catch (final NegativeArraySizeException e) {
                throw new JxlsException("Check jx:each/lastCell parameter in template! Illegal area: " + area.getAreaRef(), e);
            }
            if (this.cellRefGenerator != null) {
                this.state.updateMaxSize(size);
            } else {
                this.state.advanceCellAndUpdateSize(size, StreamingEachCommand.this.getDirection());
            }
            this.state.incrementCurrentIndex();
        }

        @Override
        public boolean isExportEmptyTable() {
            return false;
        }

        @Override
        public void startDataSet() {
            // nothing
        }

        @Override
        public void endDataSet() {
            // nothing
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            // nothing
        }

        @Override
        public IDataSetConverter split() {
            return new JxlsDataSetConverter(this);
        }

        public Size getSize() {
            return new Size(this.state.newWidth, this.state.newHeight);
        }

        private ExpressionEvaluator getExpressionEvaluator(final Context context, final String selectExpression) {
            if (selectExpression != null) {
                return context.getExpressionEvaluator(selectExpression);
            }
            return null;
        }

    }
}