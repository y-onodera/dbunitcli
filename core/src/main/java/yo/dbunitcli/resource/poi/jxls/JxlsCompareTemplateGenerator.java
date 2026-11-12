package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator.Formula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 2つのデータシートと、複合キーで両シートを突合する比較シートからなるjxlsテンプレートを作る。
 * 各データシートはシート名と同名のテーブルの全行を流し込み、A列に選んだキー列を連結した複合キーを数式で持つ。
 * 比較シートはfirstの行ごとに展開し、複合キーでsecondの行を引いて比較ペアの値を並べる。
 * <p>
 * 数式はセルを直接参照せず、列全体に付けた名前とROW()で同じ行の値を取る。formulaProcess=falseではjxlsがセル参照を
 * 展開後の行へずらさないため。シート名も数式に書かない（jxlsがセル参照に見える名前（例: T1）を書き換えるため）。
 *
 * @param first            比較の基準になるデータシート
 * @param second           firstと突合するデータシート
 * @param keys             複合キーにする列のペア（firstの列とsecondの列）
 * @param compares         値を比較する列のペア
 * @param compareSheetName 比較シートの名前
 */
public record JxlsCompareTemplateGenerator(DataSheet first, DataSheet second, List<ColumnPair> keys,
                                           List<ColumnPair> compares, String compareSheetName) {

    private static final String KEY_HEADER = "KEY";
    private static final String KEY_SEPARATOR = "|";
    private static final String DIFF_MARK = "×";
    private static final String DIFF_STATUS = "diff";
    // secondにキーが無い行のSTATUS
    private static final String MISSING_STATUS = "missing";
    private static final String FIRST = "FIRST";
    private static final String SECOND = "SECOND";
    private static final String COMPARE = "COMPARE";
    // データシートは1行目が見出し・2行目からデータ、比較シートは1行目が集計・2行目が見出し・3行目からデータ
    private static final int DATA_SHEET_EACH_ROW = 1;
    private static final int COMPARE_SHEET_HEADER_ROW = 1;
    private static final int COMPARE_SHEET_EACH_ROW = 2;
    private static final int ROW_OFFSET = COMPARE_SHEET_EACH_ROW - DATA_SHEET_EACH_ROW;

    public JxlsCompareTemplateGenerator {
        keys = List.copyOf(keys);
        compares = List.copyOf(compares);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("keys is required");
        }
        final Set<String> sheetNames = new HashSet<>();
        for (final String sheetName : List.of(first.sheetName(), second.sheetName(), compareSheetName)) {
            validateSheetName(sheetName);
            if (!sheetNames.add(sheetName.toLowerCase())) {
                throw new IllegalArgumentException("duplicate sheet name:" + sheetName);
            }
        }
        for (final ColumnPair pair : Stream.concat(keys.stream(), compares.stream()).toList()) {
            first.indexOf(pair.first());
            second.indexOf(pair.second());
        }
    }

    // jxlsコメントの式（'…'）と数式のシート参照に埋め込むため、アポストロフィは使わせない
    private static void validateSheetName(final String sheetName) {
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException("sheet name is required");
        }
        if (sheetName.contains("'")) {
            throw new IllegalArgumentException("sheet name must not contain apostrophe:" + sheetName);
        }
        WorkbookUtil.validateSheetName(sheetName);
    }

    public void write(final File resultFile) throws IOException {
        try (final Workbook workbook = new XSSFWorkbook()) {
            this.createDataSheet(workbook, this.first, FIRST, this.keys.stream().map(ColumnPair::first).toList());
            this.createDataSheet(workbook, this.second, SECOND, this.keys.stream().map(ColumnPair::second).toList());
            this.createCompareSheet(workbook);
            JxlsTemplateGenerator.write(workbook, resultFile);
        }
    }

    private void createDataSheet(final Workbook workbook, final DataSheet dataSheet, final String role, final List<String> keyColumns) {
        final Sheet sheet = workbook.createSheet(dataSheet.sheetName());
        final List<String> header = new ArrayList<>();
        header.add(KEY_HEADER);
        header.addAll(dataSheet.columns());
        JxlsTemplateGenerator.createRow(sheet, 0, header);
        for (int i = 0; i < header.size(); i++) {
            defineColumnName(workbook, sheet, role, i);
        }
        final List<Object> values = new ArrayList<>();
        values.add(new Formula(keyColumns.stream()
                .map(column -> at(columnName(role, dataSheet.columnIndex(column)), 0))
                .collect(Collectors.joining("&\"" + KEY_SEPARATOR + "\"&", "", keyColumns.size() == 1 ? "&\"\"" : ""))));
        dataSheet.columns().stream().map(JxlsTemplateGenerator::rowValue).forEach(values::add);
        JxlsTemplateGenerator.createRow(sheet, DATA_SHEET_EACH_ROW, values);
        JxlsTemplateGenerator.addAreaComments(sheet, 0, DATA_SHEET_EACH_ROW, header.size() - 1);
    }

    /*
     * 列: A=複合キー、B=secondの行番号（MATCH）、C=STATUS、以降は比較ペアごとに値1・値2・判定。
     * firstの行とは行番号の差（ROW_OFFSET）で対応させる
     */
    private void createCompareSheet(final Workbook workbook) {
        final Sheet sheet = workbook.createSheet(this.compareSheetName);
        final String secondOnly = this.second.sheetName() + " only";
        final String secondRow = at(columnName(COMPARE, 1), 0);

        final List<String> header = new ArrayList<>(List.of(KEY_HEADER, this.second.sheetName() + " row", "STATUS"));
        final List<Object> values = new ArrayList<>();
        values.add(new Formula(at(columnName(FIRST, 0), ROW_OFFSET)));
        values.add(new Formula("IFERROR(MATCH(" + at(columnName(COMPARE, 0), 0) + "," + columnName(SECOND, 0) + ",0),\"\")"));
        values.add(null);
        final List<String> differences = new ArrayList<>();
        for (final ColumnPair pair : this.compares) {
            header.add(pair.first() + "(" + this.first.sheetName() + ")");
            header.add(pair.second() + "(" + this.second.sheetName() + ")");
            header.add(pair.first() + " diff");
            final String firstValue = at(columnName(COMPARE, values.size()), 0);
            final String secondValue = at(columnName(COMPARE, values.size() + 1), 0);
            differences.add(at(columnName(COMPARE, values.size() + 2), 0) + "<>\"\"");
            values.add(new Formula(at(columnName(FIRST, this.first.columnIndex(pair.first())), ROW_OFFSET) + "&\"\""));
            values.add(new Formula("IF(" + secondRow + "=\"\",\"\",INDEX(" + columnName(SECOND, this.second.columnIndex(pair.second())) + "," + secondRow + ")&\"\")"));
            values.add(new Formula("IF(EXACT(" + firstValue + "," + secondValue + "),\"\",\"" + DIFF_MARK + "\")"));
        }
        final String status = differences.isEmpty()
                ? "\"\""
                : "IF(OR(" + String.join(",", differences) + "),\"" + DIFF_STATUS + "\",\"\")";
        values.set(2, new Formula("IF(" + secondRow + "=\"\",\"" + MISSING_STATUS + "\"," + status + ")"));

        for (int i = 0; i < header.size(); i++) {
            defineColumnName(workbook, sheet, COMPARE, i);
        }
        // secondだけにある行数。キーが一意なら second件数 - (first件数 - secondに無い件数)
        JxlsTemplateGenerator.createRow(sheet, 0, List.of(secondOnly
                , new Formula("COUNTA(" + columnName(SECOND, 0) + ")-COUNTA(" + columnName(FIRST, 0) + ")+COUNTIF(" + columnName(COMPARE, 2) + ",\"" + MISSING_STATUS + "\")")));
        JxlsTemplateGenerator.createRow(sheet, COMPARE_SHEET_HEADER_ROW, header);
        JxlsTemplateGenerator.createRow(sheet, COMPARE_SHEET_EACH_ROW, values);
        JxlsTemplateGenerator.addAreaComments(sheet, 0, COMPARE_SHEET_EACH_ROW, header.size() - 1, this.first.sheetName());
    }

    /** 列全体を指す名前（例: FIRST_B）。シートの役割と列記号で決め、シート名・列名に依存させない */
    private static String columnName(final String role, final int columnIndex) {
        return role + "_" + CellReference.convertNumToColString(columnIndex);
    }

    private static void defineColumnName(final Workbook workbook, final Sheet sheet, final String role, final int columnIndex) {
        final Name name = workbook.createName();
        name.setNameName(columnName(role, columnIndex));
        final String column = CellReference.convertNumToColString(columnIndex);
        name.setRefersToFormula("'" + sheet.getSheetName() + "'!$" + column + ":$" + column);
    }

    /** 列名の範囲から、この行（の offset 行上）の値を取る式 */
    private static String at(final String columnName, final int offset) {
        return "INDEX(" + columnName + ",ROW()" + (offset == 0 ? "" : "-" + offset) + ")";
    }

    /**
     * @param sheetName シート名。generate時はこの名前のテーブルの行を流し込む
     * @param columns   データセットの列（シートのB列以降に並べる）
     */
    public record DataSheet(String sheetName, List<String> columns) {
        public DataSheet {
            columns = List.copyOf(columns);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("columns is required:" + sheetName);
            }
        }

        int indexOf(final String column) {
            final int index = this.columns.indexOf(column);
            if (index < 0) {
                throw new IllegalArgumentException("column not found:" + this.sheetName + "." + column);
            }
            return index;
        }

        /** シート上の列番号（A列は複合キー） */
        int columnIndex(final String column) {
            return this.indexOf(column) + 1;
        }
    }

    public record ColumnPair(String first, String second) {
    }
}
