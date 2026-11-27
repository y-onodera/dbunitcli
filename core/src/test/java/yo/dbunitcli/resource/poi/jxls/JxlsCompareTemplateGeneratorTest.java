package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.poi.jxls.JxlsCompareTemplateGenerator.ColumnPair;
import yo.dbunitcli.resource.poi.jxls.JxlsCompareTemplateGenerator.DataSheet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JxlsCompareTemplateGeneratorTest {

    private static final DataSheet FIRST = new DataSheet("old", List.of("ID", "SUB", "NAME", "AMT"));
    private static final DataSheet SECOND = new DataSheet("new", List.of("CODE", "SUB", "NAME2", "AMT"));

    private static JxlsCompareTemplateGenerator twoDatasets() {
        return new JxlsCompareTemplateGenerator(FIRST, SECOND
                , List.of(new ColumnPair("ID", "CODE"), new ColumnPair("SUB", "SUB"))
                , List.of(new ColumnPair("NAME", "NAME2"), new ColumnPair("AMT", "AMT"))
                , "compare");
    }

    private static List<String> rowValues(final Sheet sheet, final int rowIndex, final FormulaEvaluator evaluator) {
        final DataFormatter formatter = new DataFormatter();
        final List<String> result = new ArrayList<>();
        for (final Cell cell : sheet.getRow(rowIndex)) {
            result.add(formatter.formatCellValue(cell, evaluator));
        }
        return result;
    }

    private static List<String> rowStrings(final Sheet sheet, final int rowIndex) {
        final List<String> result = new ArrayList<>();
        for (final Cell cell : sheet.getRow(rowIndex)) {
            result.add(cell.getStringCellValue());
        }
        return result;
    }

    private static Map<String, Object> table(final List<String> columns, final String[]... rows) {
        final List<Map<String, Object>> result = new ArrayList<>();
        for (final String[] row : rows) {
            final Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                map.put(columns.get(i), row[i]);
            }
            result.add(map);
        }
        return Map.of("rows", result);
    }

    @Nested
    class Template {

        @Test
        void dataSheetHasCompositeKeyFormulaAndColumns(@TempDir final File dir) throws IOException {
            final File file = new File(dir, "template.xlsx");
            twoDatasets().write(file);
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                Assertions.assertEquals(List.of("old", "new", "compare")
                        , List.of(workbook.getSheetName(0), workbook.getSheetName(1), workbook.getSheetName(2)));
                final Sheet old = workbook.getSheet("old");
                Assertions.assertEquals(List.of("KEY", "ID", "SUB", "NAME", "AMT"), rowStrings(old, 0));
                Assertions.assertEquals(List.of("$[INDEX(FIRST_B,ROW())&\"|\"&INDEX(FIRST_C,ROW())]", "${row.get('ID')}", "${row.get('SUB')}", "${row.get('NAME')}", "${row.get('AMT')}")
                        , rowStrings(old, 1));
                Assertions.assertEquals("jx:area(lastCell=\"E2\")", old.getRow(0).getCell(0).getCellComment().getString().getString());
                Assertions.assertEquals("jx:each(items=\"param.dataSet.get('old').rows\" var=\"row\" varIndex=\"index\" lastCell=\"E2\")"
                        , old.getRow(1).getCell(0).getCellComment().getString().getString());
                final Sheet newSheet = workbook.getSheet("new");
                Assertions.assertEquals(List.of("KEY", "old match", "CODE", "SUB", "NAME2", "AMT"), rowStrings(newSheet, 0));
                Assertions.assertEquals(List.of("$[INDEX(SECOND_C,ROW())&\"|\"&INDEX(SECOND_D,ROW())]"
                                , "$[IF(ISNA(MATCH(INDEX(SECOND_A,ROW()),FIRST_A,0)),\"×\",\"○\")]")
                        , rowStrings(newSheet, 1).subList(0, 2));
                Assertions.assertEquals("'old'!$D:$D", workbook.getName("FIRST_D").getRefersToFormula());
                Assertions.assertEquals("'new'!$A:$A", workbook.getName("SECOND_A").getRefersToFormula());
                Assertions.assertEquals("'compare'!$I:$I", workbook.getName("COMPARE_I").getRefersToFormula());
                Assertions.assertEquals("'compare'!$F$4:$F$1048576", workbook.getName("COMPARE_DATA_F").getRefersToFormula());
            }
        }

        @Test
        void compareSheetLooksUpSecondByCompositeKey(@TempDir final File dir) throws IOException {
            final File file = new File(dir, "template.xlsx");
            twoDatasets().write(file);
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                final Sheet compare = workbook.getSheet("compare");
                Assertions.assertEquals(List.of("new only", "$[COUNTIF(SECOND_B,\"×\")]")
                        , rowStrings(compare, 0));
                Assertions.assertEquals(List.of("", "", "$[COUNTIF(COMPARE_DATA_C,\">0\")]", "", "", "$[COUNTIF(COMPARE_DATA_F,\"×\")]"
                                , "", "", "$[COUNTIF(COMPARE_DATA_I,\"×\")]")
                        , rowStrings(compare, 1));
                Assertions.assertEquals(List.of("KEY", "new row", "STATUS", "NAME(old)", "NAME2(new)", "NAME diff", "AMT(old)", "AMT(new)", "AMT diff")
                        , rowStrings(compare, 2));
                Assertions.assertEquals(List.of(
                                "$[INDEX(FIRST_A,ROW()-2)]"
                                , "$[IFERROR(MATCH(INDEX(COMPARE_A,ROW()),SECOND_A,0),\"\")]"
                                , "$[0+(INDEX(COMPARE_F,ROW())=\"×\")+(INDEX(COMPARE_I,ROW())=\"×\")]"
                                , "$[INDEX(FIRST_D,ROW()-2)&\"\"]"
                                , "$[IF(INDEX(COMPARE_B,ROW())=\"\",\"\",INDEX(SECOND_E,INDEX(COMPARE_B,ROW()))&\"\")]"
                                , "$[IF(EXACT(INDEX(COMPARE_D,ROW()),INDEX(COMPARE_E,ROW())),\"○\",\"×\")]"
                                , "$[INDEX(FIRST_E,ROW()-2)&\"\"]"
                                , "$[IF(INDEX(COMPARE_B,ROW())=\"\",\"\",INDEX(SECOND_F,INDEX(COMPARE_B,ROW()))&\"\")]"
                                , "$[IF(EXACT(INDEX(COMPARE_G,ROW()),INDEX(COMPARE_H,ROW())),\"○\",\"×\")]")
                        , rowStrings(compare, 3));
                Assertions.assertEquals("jx:each(items=\"param.dataSet.get('old').rows\" var=\"row\" varIndex=\"index\" lastCell=\"I4\")"
                        , compare.getRow(3).getCell(0).getCellComment().getString().getString());
                for (final int row : List.of(1, 3)) {
                    for (int column = 0; column < 9; column++) {
                        Assertions.assertEquals(column == 5 || column == 8 ? HorizontalAlignment.CENTER : HorizontalAlignment.GENERAL
                                , compare.getRow(row).getCell(column).getCellStyle().getAlignment(), "row " + row + " column " + column);
                    }
                }
            }
        }

        @Test
        void singleKeyIsConvertedToText(@TempDir final File dir) throws IOException {
            final File file = new File(dir, "template.xlsx");
            final DataSheet first = new DataSheet("before", List.of("ID", "NAME"));
            final DataSheet second = new DataSheet("after", List.of("ID", "NAME"));
            new JxlsCompareTemplateGenerator(first, second, List.of(new ColumnPair("ID", "ID")), List.of(), "compare").write(file);
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                Assertions.assertEquals("$[INDEX(FIRST_B,ROW())&\"\"]", rowStrings(workbook.getSheet("before"), 1).getFirst());
                Assertions.assertEquals(List.of("KEY", "after row", "STATUS"), rowStrings(workbook.getSheet("compare"), 2));
                Assertions.assertEquals("$[0]", rowStrings(workbook.getSheet("compare"), 3).get(2));
            }
        }
    }

    @Nested
    class Validation {

        @Test
        void keysAreRequired() {
            Assertions.assertThrows(IllegalArgumentException.class
                    , () -> new JxlsCompareTemplateGenerator(FIRST, SECOND, List.of(), List.of(), "compare"));
        }

        @Test
        void unknownColumnIsRejected() {
            Assertions.assertThrows(IllegalArgumentException.class
                    , () -> new JxlsCompareTemplateGenerator(FIRST, SECOND, List.of(new ColumnPair("ID", "ID")), List.of(), "compare"));
        }

        @Test
        void duplicateSheetNameIsRejected() {
            Assertions.assertThrows(IllegalArgumentException.class
                    , () -> new JxlsCompareTemplateGenerator(FIRST, new DataSheet("OLD", SECOND.columns())
                            , List.of(new ColumnPair("ID", "CODE")), List.of(), "compare"));
        }

        @Test
        void apostropheInSheetNameIsRejected() {
            Assertions.assertThrows(IllegalArgumentException.class
                    , () -> new JxlsCompareTemplateGenerator(FIRST, SECOND, List.of(new ColumnPair("ID", "CODE")), List.of(), "it's"));
        }

        @Test
        void invalidSheetNameIsRejected() {
            Assertions.assertThrows(IllegalArgumentException.class
                    , () -> new JxlsCompareTemplateGenerator(FIRST, SECOND, List.of(new ColumnPair("ID", "CODE")), List.of(), "a/b"));
        }
    }

    @Nested
    class Render {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void compareSheetShowsDifferences(final boolean formulaProcess, @TempDir final File dir) throws IOException {
            final File template = new File(dir, "template.xlsx");
            twoDatasets().write(template);
            final File result = new File(dir, "result.xlsx");
            final Map<String, Object> dataSet = Map.of(
                    "old", table(FIRST.columns()
                            , new String[]{"1", "a", "x", "10"}
                            , new String[]{"2", "a", "y", "20"}
                            , new String[]{"3", "b", "z", "30"})
                    , "new", table(SECOND.columns()
                            , new String[]{"2", "a", "Y", "20"}
                            , new String[]{"1", "a", "x", "10"}
                            , new String[]{"4", "c", "w", "40"}
                            , new String[]{"5", "c", "v", "50"}));
            JxlsTemplateRender.builder()
                    .setTemplateParameterAttribute("param")
                    .setFormulaProcess(formulaProcess)
                    .build()
                    .render(Files.readAllBytes(template.toPath()), result, new Parameter(Map.of("dataSet", dataSet)), false);
            try (final Workbook workbook = WorkbookFactory.create(result)) {
                final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                final Sheet old = workbook.getSheet("old");
                Assertions.assertEquals(List.of("2|a", "2", "a", "y", "20"), rowValues(old, 2, evaluator));
                Assertions.assertEquals(List.of("1|a", "○", "1", "a", "x", "10"), rowValues(workbook.getSheet("new"), 2, evaluator));
                Assertions.assertEquals(List.of("5|c", "×", "5", "c", "v", "50"), rowValues(workbook.getSheet("new"), 4, evaluator));
                final Sheet compare = workbook.getSheet("compare");
                Assertions.assertEquals(List.of("new only", "2"), rowValues(compare, 0, evaluator));
                Assertions.assertEquals(List.of("", "", "2", "", "", "2", "", "", "1"), rowValues(compare, 1, evaluator));
                Assertions.assertEquals(List.of("1|a", "3", "0", "x", "x", "○", "10", "10", "○"), rowValues(compare, 3, evaluator));
                Assertions.assertEquals(List.of("2|a", "2", "1", "y", "Y", "×", "20", "20", "○"), rowValues(compare, 4, evaluator));
                Assertions.assertEquals(List.of("3|b", "", "2", "z", "", "×", "30", "", "×"), rowValues(compare, 5, evaluator));
                Assertions.assertEquals(HorizontalAlignment.CENTER, compare.getRow(5).getCell(5).getCellStyle().getAlignment());
                Assertions.assertEquals(5, compare.getLastRowNum());
            }
        }
    }
}
