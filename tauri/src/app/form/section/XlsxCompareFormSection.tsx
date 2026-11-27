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

// reloadを押したときのsrcの入力値と、押した回数（同じ値でも読み直す）
type TableNamesLoad = { srcInfo: DatasetSrcInfo; count: number };

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
	const [tableNamesLoad, setTableNamesLoad] = useState<TableNamesLoad | null>(
		null,
	);
	const { tableNames } = useDatasetTableNames(
		tableNamesLoad?.srcInfo ?? srcInfo,
		false,
		{ enabled: tableNamesLoad !== null, reloadKey: tableNamesLoad?.count },
	);
	// keys / comparesの列の選択はoldTable・newTableの入力中の値から読む
	const [oldTable, setOldTable] = useSyncedState(
		generate.oldTable?.value ?? "",
	);
	const [newTable, setNewTable] = useSyncedState(
		generate.newTable?.value ?? "",
	);
	const { keys, compares, compareSheetName } = generate;
	if (
		!generate.oldTable &&
		!generate.newTable &&
		!keys &&
		!compares &&
		!compareSheetName
	) {
		return null;
	}

	const renderTableText = (
		field: CommandOption,
		handleValueChange?: (value: string) => void,
	) => (
		<Text
			prefix=""
			element={field}
			resourceFiles={tableNames}
			handleValueChange={handleValueChange}
		>
			{() => (
				<BlueRefreshButton
					title="reload table names"
					handleClick={() =>
						setTableNamesLoad({
							srcInfo,
							count: (tableNamesLoad?.count ?? 0) + 1,
						})
					}
				/>
			)}
		</Text>
	);
	const renderColumnsText = (field: CommandOption) => (
		<Text prefix="" element={field}>
			{({ path, setPath }) => (
				<CompareColumnsButton
					caption={field.name}
					oldTable={oldTable}
					newTable={newTable}
					value={path}
					setValue={setPath}
				/>
			)}
		</Text>
	);

	return (
		<SectionFieldset>
			<SectionLegend
				title="compare"
				command="sections/xlsx-compare"
				label="Compare"
			/>
			{generate.oldTable && renderTableText(generate.oldTable, setOldTable)}
			{generate.newTable && renderTableText(generate.newTable, setNewTable)}
			{keys && renderColumnsText(keys)}
			{compares && renderColumnsText(compares)}
			{compareSheetName && <PlainText prefix="" element={compareSheetName} />}
		</SectionFieldset>
	);
}
