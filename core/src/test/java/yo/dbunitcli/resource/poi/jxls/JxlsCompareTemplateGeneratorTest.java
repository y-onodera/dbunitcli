package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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
                Assertions.assertEquals("$[INDEX(SECOND_B,ROW())&\"|\"&INDEX(SECOND_C,ROW())]", rowStrings(workbook.getSheet("new"), 1).getFirst());
                Assertions.assertEquals("'old'!$D:$D", workbook.getName("FIRST_D").getRefersToFormula());
                Assertions.assertEquals("'new'!$A:$A", workbook.getName("SECOND_A").getRefersToFormula());
                Assertions.assertEquals("'compare'!$I:$I", workbook.getName("COMPARE_I").getRefersToFormula());
            }
        }

        @Test
        void compareSheetLooksUpSecondByCompositeKey(@TempDir final File dir) throws IOException {
            final File file = new File(dir, "template.xlsx");
            twoDatasets().write(file);
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                final Sheet compare = workbook.getSheet("compare");
                Assertions.assertEquals(List.of("new only", "$[COUNTA(SECOND_A)-COUNTA(FIRST_A)+COUNTIF(COMPARE_C,\"missing\")]")
                        , rowStrings(compare, 0));
                Assertions.assertEquals(List.of("KEY", "new row", "STATUS", "NAME(old)", "NAME2(new)", "NAME diff", "AMT(old)", "AMT(new)", "AMT diff")
                        , rowStrings(compare, 1));
                Assertions.assertEquals(List.of(
                                "$[INDEX(FIRST_A,ROW()-1)]"
                                , "$[IFERROR(MATCH(INDEX(COMPARE_A,ROW()),SECOND_A,0),\"\")]"
                                , "$[IF(INDEX(COMPARE_B,ROW())=\"\",\"missing\",IF(OR(INDEX(COMPARE_F,ROW())<>\"\",INDEX(COMPARE_I,ROW())<>\"\"),\"diff\",\"\"))]"
                                , "$[INDEX(FIRST_D,ROW()-1)&\"\"]"
                                , "$[IF(INDEX(COMPARE_B,ROW())=\"\",\"\",INDEX(SECOND_D,INDEX(COMPARE_B,ROW()))&\"\")]"
                                , "$[IF(EXACT(INDEX(COMPARE_D,ROW()),INDEX(COMPARE_E,ROW())),\"\",\"×\")]"
                                , "$[INDEX(FIRST_E,ROW()-1)&\"\"]"
                                , "$[IF(INDEX(COMPARE_B,ROW())=\"\",\"\",INDEX(SECOND_E,INDEX(COMPARE_B,ROW()))&\"\")]"
                                , "$[IF(EXACT(INDEX(COMPARE_G,ROW()),INDEX(COMPARE_H,ROW())),\"\",\"×\")]")
                        , rowStrings(compare, 2));
                Assertions.assertEquals("jx:each(items=\"param.dataSet.get('old').rows\" var=\"row\" varIndex=\"index\" lastCell=\"I3\")"
                        , compare.getRow(2).getCell(0).getCellComment().getString().getString());
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
                Assertions.assertEquals(List.of("KEY", "after row", "STATUS"), rowStrings(workbook.getSheet("compare"), 1));
                Assertions.assertEquals("$[IF(INDEX(COMPARE_B,ROW())=\"\",\"missing\",\"\")]", rowStrings(workbook.getSheet("compare"), 2).get(2));
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
                Assertions.assertEquals(List.of("5|c", "5", "c", "v", "50"), rowValues(workbook.getSheet("new"), 4, evaluator));
                final Sheet compare = workbook.getSheet("compare");
                Assertions.assertEquals(List.of("new only", "2"), rowValues(compare, 0, evaluator));
                Assertions.assertEquals(List.of("1|a", "3", "", "x", "x", "", "10", "10", ""), rowValues(compare, 2, evaluator));
                Assertions.assertEquals(List.of("2|a", "2", "diff", "y", "Y", "×", "20", "20", ""), rowValues(compare, 3, evaluator));
                Assertions.assertEquals(List.of("3|b", "", "missing", "z", "", "×", "30", "", "×"), rowValues(compare, 4, evaluator));
                Assertions.assertEquals(4, compare.getLastRowNum());
            }
        }
    }
}
