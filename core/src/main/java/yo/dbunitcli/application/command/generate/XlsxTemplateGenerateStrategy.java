package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.poi.jxls.JxlsCompareTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsMetaDataTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * xlsxTemplate: データセットのメタデータからjxlsテンプレート（Excel）をコードで組み立てる型（テンプレート不要）。
 * -templateType=compareは、srcのテーブル（-setting適用後）から-oldTable・-newTableを選んでテーブル名そのままのデータシートにし、
 * -keysで突合する比較シートを加える。-templateType=metadataはsrcに依らない固定レイアウトのテーブル定義書を出す
 */
public final class XlsxTemplateGenerateStrategy implements GenerateStrategy {

    public static final String DEFAULT_COMPARE_SHEET_NAME = "compare";

    private static final String PAIR_SEPARATOR = ":";

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public ParameterUnit fixedUnit() {
        return ParameterUnit.dataset;
    }

    @Override
    public boolean loadData() {
        return false;
    }

    @Override
    public List<Option.Violation> validateOption(final GenerateContext option) {
        return option.templateType() == XlsxTemplateType.compare
                ? Option.concat(option.validateRequired("-oldTable", option.oldTable())
                , option.validateRequired("-newTable", option.newTable())
                , option.validateRequired("-keys", option.keys()))
                : List.of();
    }

    @Override
    public void putOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        builder.put("-templateType", option.templateType(), XlsxTemplateType.class, true);
        if (option.templateType() == XlsxTemplateType.compare) {
            builder.put("-oldTable", option.oldTable(), true)
                   .put("-newTable", option.newTable(), true)
                   .put("-keys", option.keys(), true)
                   .put("-compares", option.compares())
                   .putWithDefault("-compareSheetName", option.compareSheetName(), DEFAULT_COMPARE_SHEET_NAME);
        }
    }

    @Override
    public GenerateWriter writer(final GenerateContext option) {
        if (option.templateType() == XlsxTemplateType.metadata) {
            return (resultFile, _) -> JxlsMetaDataTemplateGenerator.write(resultFile);
        }
        if (option.templateType() != XlsxTemplateType.compare) {
            return JxlsTemplateGenerator::createTemplate;
        }
        final String compareSheetName = Strings.orDefault(option.compareSheetName(), DEFAULT_COMPARE_SHEET_NAME);
        return (resultFile, param) -> compareGenerator(param, option.oldTable(), option.newTable()
                , option.keys(), option.compares(), compareSheetName)
                .write(resultFile);
    }

    @SuppressWarnings("unchecked")
    static JxlsCompareTemplateGenerator compareGenerator(final Parameter param, final String oldTable, final String newTable
            , final String keys, final String compares, final String compareSheetName) {
        final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
        final JxlsCompareTemplateGenerator.DataSheet first = dataSheet(dataSet, "-oldTable", oldTable);
        final JxlsCompareTemplateGenerator.DataSheet second = dataSheet(dataSet, "-newTable", newTable);
        final List<JxlsCompareTemplateGenerator.ColumnPair> keyPairs = toPairs(keys);
        final List<JxlsCompareTemplateGenerator.ColumnPair> comparePairs = Strings.isEmpty(compares)
                ? sameNamePairs(first, second, keyPairs)
                : toPairs(compares);
        return new JxlsCompareTemplateGenerator(first, second, keyPairs, comparePairs, compareSheetName);
    }

    // データシート名はテーブル名と同じにする（generate時にシート名のテーブルの行を流し込むため）
    private static JxlsCompareTemplateGenerator.DataSheet dataSheet(final Map<String, Map<String, Object>> dataSet
            , final String key, final String tableName) {
        final Map<String, Object> table = dataSet.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException(key + " table not found in src:" + tableName + " tables:" + dataSet.keySet());
        }
        return new JxlsCompareTemplateGenerator.DataSheet(tableName, Arrays.stream((Column[]) table.get("columns"))
                .map(Column::getColumnName)
                .toList());
    }

    /** "oldColumn:newColumn" または同名の"column"をカンマ区切りで並べた値 */
    private static List<JxlsCompareTemplateGenerator.ColumnPair> toPairs(final String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(Strings::isNotEmpty)
                .map(it -> {
                    final int index = it.indexOf(PAIR_SEPARATOR);
                    return index < 0
                            ? new JxlsCompareTemplateGenerator.ColumnPair(it, it)
                            : new JxlsCompareTemplateGenerator.ColumnPair(it.substring(0, index).trim(), it.substring(index + 1).trim());
                })
                .toList();
    }

    /** firstのキー以外の列のうち、secondに同名の列があるもの */
    private static List<JxlsCompareTemplateGenerator.ColumnPair> sameNamePairs(final JxlsCompareTemplateGenerator.DataSheet first
            , final JxlsCompareTemplateGenerator.DataSheet second, final List<JxlsCompareTemplateGenerator.ColumnPair> keys) {
        final List<String> keyColumns = keys.stream().map(JxlsCompareTemplateGenerator.ColumnPair::first).toList();
        return first.columns().stream()
                .filter(it -> !keyColumns.contains(it) && second.columns().contains(it))
                .map(it -> new JxlsCompareTemplateGenerator.ColumnPair(it, it))
                .toList();
    }
}
