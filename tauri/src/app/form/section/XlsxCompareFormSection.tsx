import { useState } from "react";
import { SectionFieldset } from "../../../components/dialog";
import { SectionLegend } from "../../../components/dialog/SectionFieldset";
import { BlueRefreshButton } from "../../../components/element/ButtonIcon";
import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import { useDatasetTableNames } from "../../../hooks/useDatasetSettings";
import type {
	CommandOption,
	DatasetSrcInfo,
} from "../../../model/CommandOption";
import type { GenerateOptions } from "../../../model/SelectParameter";
import { useSyncedState } from "../../../utils/useResettableState";
import { CompareColumnsButton } from "./dialog/CompareColumnsDialog";
import PlainText from "./element/PlainText";
import Text from "./element/Text";

// srcが未指定なので読まない
const NOT_LOADED: DatasetSrcInfo = { values: {} };

// generateType=xlsxTemplate・templateType=compareで、比べる2テーブルと突合の列を指定する
export default function XlsxCompareFormSection({
	generate,
}: {
	generate: GenerateOptions;
}) {
	const srcInfo = useDatasetSrcInfo();
	// 実行時に照合するのはsrcの読込とsettingの適用が終わった後のテーブル名なので、
	// srcの入力値（settingTargetOnlyを含む）のまま読んだテーブル名を候補にする。
	// 読むたびにsidecarがデータセットを読み直すため、reloadボタンを押したときだけ読む
	const [tableNamesSrcInfo, setTableNamesSrcInfo] = useState(NOT_LOADED);
	const { tableNames } = useDatasetTableNames(tableNamesSrcInfo);
	// keys / comparesの列の選択はoldTableの入力中の値から読む
	const [oldTable, setOldTable] = useSyncedState(
		generate.oldTable?.value ?? "",
	);
	const tables = [generate.oldTable, generate.newTable].filter(
		(field): field is CommandOption => field != null,
	);
	const columns = [generate.keys, generate.compares].filter(
		(field): field is CommandOption => field != null,
	);
	if (
		tables.length === 0 &&
		columns.length === 0 &&
		!generate.compareSheetName
	) {
		return null;
	}
	return (
		<SectionFieldset>
			<SectionLegend
				title="compare"
				command="sections/xlsx-compare"
				label="Compare"
			/>
			{tables.map((field) => (
				<Text
					key={field.name}
					prefix=""
					element={field}
					resourceFiles={tableNames}
					handleValueChange={
						field === generate.oldTable ? setOldTable : undefined
					}
				>
					{() => (
						// 同じsrcInfoでも読み直すよう、毎回別のオブジェクトにする
						<BlueRefreshButton
							title="reload table names"
							handleClick={() => setTableNamesSrcInfo({ ...srcInfo })}
						/>
					)}
				</Text>
			))}
			{columns.map((field) => (
				<Text key={field.name} prefix="" element={field}>
					{({ path, setPath }) => (
						<CompareColumnsButton
							caption={field.name}
							oldTable={oldTable}
							value={path}
							setValue={setPath}
						/>
					)}
				</Text>
			))}
			{generate.compareSheetName && (
				<PlainText prefix="" element={generate.compareSheetName} />
			)}
		</SectionFieldset>
	);
}
