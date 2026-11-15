import { useState } from "react";
import { SectionFieldset } from "../../../components/dialog";
import { SectionLegend } from "../../../components/dialog/SectionFieldset";
import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../hooks/useDatasetSettings";
import type {
	CommandOption,
	DatasetSrcInfo,
} from "../../../model/CommandOption";
import type { GenerateOptions } from "../../../model/SelectParameter";
import PlainText from "./element/PlainText";
import Text from "./element/Text";

// generateType=xlsxTemplate・templateType=compareで、比べる2テーブルと突合の列を指定する
export default function XlsxCompareFormSection({
	generate,
}: {
	generate: GenerateOptions;
}) {
	const tables = [generate.oldTable, generate.newTable].filter(
		(field): field is CommandOption => field != null,
	);
	const columns = [
		generate.keys,
		generate.compares,
		generate.compareSheetName,
	].filter((field): field is CommandOption => field != null);
	if (tables.length === 0 && columns.length === 0) {
		return null;
	}
	return (
		<SectionFieldset>
			<SectionLegend
				title="compare"
				command="sections/xlsx-compare"
				label="Compare"
			/>
			{tables.length > 0 && <CompareTableTexts tables={tables} />}
			{columns.map((field) => (
				<PlainText key={field.name} prefix="" element={field} />
			))}
		</SectionFieldset>
	);
}

// srcが未指定なので読まない
const NOT_LOADED: DatasetSrcInfo = { values: {} };

// 実行時に照合するのはsrcの読込とsettingの適用が終わった後のテーブル名なので、
// srcの入力値（settingTargetOnlyを含む）のまま読んだテーブル名を候補にする。
// srcの入力値は1文字ごとに変わり、そのたびにsidecarがデータセットを読み直すため、欄にフォーカスした時点の値で読む
function CompareTableTexts({ tables }: { tables: CommandOption[] }) {
	const srcInfo = useDatasetSrcInfo();
	const [loadedSrcInfo, setLoadedSrcInfo] = useState(NOT_LOADED);
	const { tableNames } = useDatasetTableNames(loadedSrcInfo);
	return (
		<fieldset onFocus={() => setLoadedSrcInfo(srcInfo)}>
			{tables.map((field) => (
				<Text
					key={field.name}
					prefix=""
					element={field}
					resourceFiles={tableNames}
				/>
			))}
		</fieldset>
	);
}
