import type { ReactNode } from "react";
import {
	ControllTextBox,
	InputLabel,
} from "../../../../components/element/Input";
import { OptionSelect, type SelectOption } from "./SqlInsertParts";

export const fileNameField = "-fileName";
const xlsxExtension = ".xlsx";

// sidecarはxlsxTemplateを常に.xlsxで保存し、拡張子が無ければ補う。template欄へも同名を反映する
export function toSavedFileName(name: string): string {
	return name.toLowerCase().endsWith(xlsxExtension)
		? name
		: name + xlsxExtension;
}

export function FileNameField({
	value,
	onChange,
}: {
	value: string;
	onChange: (value: string) => void;
}) {
	return (
		<div>
			<InputLabel
				text={fileNameField}
				id="xlsxTemplateFileName"
				required={true}
			/>
			<ControllTextBox
				name={fileNameField}
				id="xlsxTemplateFileName"
				required={true}
				value={value}
				handleChange={(ev) => onChange(ev.target.value)}
			/>
		</div>
	);
}

// 値はgenerateTypeがxlsxTemplateのときのtemplateTypeに合わせる
export type XlsxTemplateType = "table" | "compare" | "metadata";

const templateTypeOptions: SelectOption<XlsxTemplateType>[] = [
	{ value: "table", label: "table" },
	{ value: "compare", label: "compare" },
	{ value: "metadata", label: "metadata" },
];

// 生成の送信値に混ざらないよう、formの外（RefreshableDialogShellのheader）に置く
export function TemplateTypeField({
	value,
	onChange,
}: {
	value: XlsxTemplateType;
	onChange: (value: XlsxTemplateType) => void;
}) {
	return (
		<div className="mb-6">
			<OptionSelect
				id="xlsxTemplateType"
				label="templateType"
				options={templateTypeOptions}
				value={value}
				onChange={onChange}
			/>
		</div>
	);
}

// templateTypeごとのダイアログに共通の受け渡し（fileNameはtemplateTypeを切り替えても引き継ぐ）
export type XlsxTemplateTypeDialogProps = {
	header: ReactNode;
	fileName: string;
	onFileNameChange: (value: string) => void;
	onSaved: (fileName: string) => void;
	onClose: () => void;
};

export const xlsxTemplateDialogTitle = "Generate Xlsx Template";
// templateTypeを切り替えても幅が変わらないよう、最も広いcompareに合わせて固定する
export const xlsxTemplateDialogSize = "lg";
