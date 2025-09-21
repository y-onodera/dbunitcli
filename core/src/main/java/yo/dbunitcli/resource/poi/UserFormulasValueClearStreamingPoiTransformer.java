package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.usermodel.*;
import org.jxls.area.XlsArea;
import org.jxls.common.AreaRef;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiCellData;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.transform.poi.SelectSheetsForStreamingPoiTransformer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserFormulasValueClearStreamingPoiTransformer extends SelectSheetsForStreamingPoiTransformer {
    private static final String USER_FORMULA_PREFIX = "$[";
    private static final String USER_FORMULA_SUFFIX = "]";

    private static boolean isUserFormula(final String str) {
        return str.startsWith(USER_FORMULA_PREFIX) && str.endsWith(USER_FORMULA_SUFFIX);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook) {
        super(workbook);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final boolean allSheets, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, allSheets, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    public UserFormulasValueClearStreamingPoiTransformer(final Workbook workbook, final Set<String> sheetNames, final int rowAccessWindowSize, final boolean compressTmpFiles, final boolean useSharedStringsTable) {
        super(workbook, sheetNames, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    @Override
    protected void transformCell(final CellRef srcCellRef, final CellRef targetCellRef, final Context context, final boolean updateRowHeightFlag, final CellData cellData, final Sheet destSheet, final Row destRow) {
        if (!this.isStreaming() || !isUserFormula(cellData.getCellValue().toString())) {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, cellData, destSheet, destRow);
        } else {
            super.transformCell(srcCellRef, targetCellRef, context, updateRowHeightFlag, new WriteCellValueClearCellData((PoiCellData) cellData), destSheet, destRow);
        }
    }

    /**
     * {@link #writeToCell(Cell, Context, PoiTransformer)}の実行後にcellValueをClearすることで、Excelを開いたときにFormulasを計算させる
     */
    class WriteCellValueClearCellData extends PoiCellData {
        private final PoiCellData delegate;

        public WriteCellValueClearCellData(final PoiCellData delegate) {
            super(delegate.getCellRef());
            this.delegate = delegate;
        }

        @Override
        public void readCell(final Cell cell) {
            this.delegate.readCell(cell);
        }

        @Override
        public CellStyle getCellStyle() {
            return this.delegate.getCellStyle();
        }

        @Override
        public void setCellStyle(final CellStyle cellStyle) {
            this.delegate.setCellStyle(cellStyle);
        }

        @Override
        public void writeToCell(final Cell cell, final Context context, final PoiTransformer transformer) {
            this.delegate.writeToCell(cell, context, transformer);
            cell.setCellValue("");
        }

        @Override
        public Transformer getTransformer() {
            return this.delegate.getTransformer();
        }

        @Override
        public void setTransformer(final Transformer transformer) {
            this.delegate.setTransformer(transformer);
        }

        @Override
        public XlsArea getArea() {
            return this.delegate.getArea();
        }

        @Override
        public void setArea(final XlsArea area) {
            this.delegate.setArea(area);
        }

        @Override
        public Map<String, String> getAttrMap() {
            return this.delegate.getAttrMap();
        }

        @Override
        public void setAttrMap(final Map<String, String> attrMap) {
            this.delegate.setAttrMap(attrMap);
        }

        @Override
        public void setEvaluationResult(final Object evaluationResult) {
            this.delegate.setEvaluationResult(evaluationResult);
        }

        @Override
        public FormulaStrategy getFormulaStrategy() {
            return this.delegate.getFormulaStrategy();
        }

        @Override
        public void setFormulaStrategy(final FormulaStrategy formulaStrategy) {
            this.delegate.setFormulaStrategy(formulaStrategy);
        }

        @Override
        public String getDefaultValue() {
            return this.delegate.getDefaultValue();
        }

        @Override
        public void setDefaultValue(final String defaultValue) {
            this.delegate.setDefaultValue(defaultValue);
        }

        @Override
        public String getCellComment() {
            return this.delegate.getCellComment();
        }

        @Override
        public void setCellComment(final String cellComment) {
            this.delegate.setCellComment(cellComment);
        }

        @Override
        public String getSheetName() {
            return this.delegate.getSheetName();
        }

        @Override
        public CellRef getCellRef() {
            return this.delegate.getCellRef();
        }

        @Override
        public CellType getCellType() {
            return this.delegate.getCellType();
        }

        @Override
        public void setCellType(final CellType cellType) {
            this.delegate.setCellType(cellType);
        }

        @Override
        public Object getCellValue() {
            return this.delegate.getCellValue();
        }

        @Override
        public int getRow() {
            return this.delegate.getRow();
        }

        @Override
        public int getCol() {
            return this.delegate.getCol();
        }

        @Override
        public String getFormula() {
            return this.delegate.getFormula();
        }

        @Override
        public void setFormula(final String formula) {
            this.delegate.setFormula(formula);
        }

        @Override
        public List<String> getEvaluatedFormulas() {
            return this.delegate.getEvaluatedFormulas();
        }

        @Override
        public boolean isFormulaCell() {
            return this.delegate.isFormulaCell();
        }

        @Override
        public boolean isParameterizedFormulaCell() {
            return this.delegate.isParameterizedFormulaCell();
        }

        @Override
        public boolean isJointedFormulaCell() {
            return this.delegate.isJointedFormulaCell();
        }

        @Override
        public boolean addTargetPos(final CellRef cellRef) {
            return this.delegate.addTargetPos(cellRef);
        }

        @Override
        public void addTargetParentAreaRef(final AreaRef areaRef) {
            this.delegate.addTargetParentAreaRef(areaRef);
        }

        @Override
        public List<AreaRef> getTargetParentAreaRef() {
            return this.delegate.getTargetParentAreaRef();
        }

        @Override
        public void setEvaluatedFormulas(final List<String> evaluatedFormulas) {
            this.delegate.setEvaluatedFormulas(evaluatedFormulas);
        }

        @Override
        public List<CellRef> getTargetPos() {
            return this.delegate.getTargetPos();
        }

        @Override
        public void resetTargetPos() {
            this.delegate.resetTargetPos();
        }

        @Override
        public Object evaluate(final Context context) {
            return this.delegate.evaluate(context);
        }

        @Override
        public String toString() {
            return this.delegate.toString();
        }

        @Override
        public boolean equals(final Object o) {
            return this.delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return this.delegate.hashCode();
        }
    }
}
