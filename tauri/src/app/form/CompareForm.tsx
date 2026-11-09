import { useState } from "react";
import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import { ResourceEncodingProvider } from "../../context/ResourceEncodingProvider";
import { buildDatasetSrcInfo } from "../../model/CommandOption";
import type { CompareOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import CompareKeySettingsDialog, {
	CompareKeySettingsEditDialog,
} from "./section/dialog/CompareKeySettingsDialog";
import Check from "./section/element/Check";
import DatasetSettingText from "./section/element/DatasetSettingText";
import EncodingText from "./section/element/EncodingText";
import FileText from "./section/element/FileText";
import Select from "./section/element/Select";
import ImageOptionFormSection from "./section/ImageOptionFormSection";
import ResultFormSection from "./section/ResultFormSection";

export function CompareForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	compare: CompareOptions;
}) {
	const imageOption = prop.compare.imageOption;
	const newData = prop.compare.newData;
	const oldData = prop.compare.oldData;
	const expectData = prop.compare.expectData;
	const convertResult = prop.compare.convertResult;
	const targetTypeElement = prop.compare.targetType;
	const targetType = prop.compare.targetType.value;
	const settingElement = prop.compare.setting;
	const settingEncodingElement = prop.compare.settingEncoding;
	// 比較キーの設定の編集で読み書きする文字コード（入力中の値）
	const [settingEncoding, setSettingEncoding] = useState(
		settingEncodingElement.value,
	);
	const createPatchSqlElement = prop.compare.createPatchSql;

	return (
		// 比較キーの編集ではold側の入力中のデータセット・JDBC接続からテーブル名・カラム名の候補を取るため、
		// old側のsrcの入力値をフォーム全体で共有する。new/expect側は各DatasetLoadFormが自前の範囲を持つ
		<DatasetSrcInfoProvider
			resetKey={`${prop.name}:${oldData.prefix}:${oldData.srcType?.value ?? ""}`}
			initialValue={buildDatasetSrcInfo(oldData)}
		>
			<SectionFieldset>
				<SectionLegend
					title="compare"
					command="commands/compare"
					label="Compare"
				/>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={targetTypeElement}
				/>
				{targetType === "data" ? (
					<ResourceEncodingProvider
						encodings={{ compareSetting: settingEncoding }}
					>
						<DatasetSettingText
							prefix=""
							element={settingElement}
							editDialog={CompareKeySettingsDialog}
							generateEditDialog={CompareKeySettingsEditDialog}
							preview={null}
						/>
					</ResourceEncodingProvider>
				) : (
					<FileText prefix="" element={settingElement} />
				)}
				<EncodingText
					prefix=""
					element={settingEncodingElement}
					handleValueChange={setSettingEncoding}
				/>
				{targetType === "data" && (
					<Check prefix="" element={createPatchSqlElement} />
				)}
			</SectionFieldset>
			{prop.compare.imageOption && (
				<SectionFieldset>
					<SectionLegend
						title="image"
						command="sections/image"
						label="Image Option"
					/>
					<ImageOptionFormSection imageOption={imageOption} />
				</SectionFieldset>
			)}
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={newData}
				helpCommand="sections/dataset-load-form"
				helpLabel="new"
			/>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={oldData}
				helpCommand="sections/dataset-load-form"
				helpLabel="old"
				parentSrcInfo
			/>
			<SectionFieldset>
				<legend>{convertResult.prefix}</legend>
				<ResultFormSection
					resultOption={convertResult}
					handleTypeSelect={prop.handleTypeSelect}
				/>
			</SectionFieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={expectData}
				helpCommand="sections/dataset-load-form"
				helpLabel="expect"
			/>
		</DatasetSrcInfoProvider>
	);
}
