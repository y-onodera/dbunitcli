import type { ReactNode } from "react";
import { DialogField } from "../../../../components/dialog";
import { OptionSelect, OptionText, type SelectOption } from "./SqlInsertParts";

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
		<DialogField>
			<OptionText
				id={fileNameField}
				label={fileNameField}
				value={value}
				required={true}
				onChange={onChange}
			/>
		</DialogField>
	);
}

// 値はgenerateTypeがxlsxTemplateのときのtemplateTypeに合わせる
const templateTypes = ["table", "compare", "metadata"] as const;
export type XlsxTemplateType = (typeof templateTypes)[number];

const templateTypeOptions: SelectOption<XlsxTemplateType>[] = templateTypes.map(
	(value) => ({ value, label: value }),
);

export function TemplateTypeField({
	value,
	onChange,
}: {
	value: XlsxTemplateType;
	onChange: (value: XlsxTemplateType) => void;
}) {
	return (
		<DialogField last>
			<OptionSelect
				id="xlsxTemplateType"
				label="templateType"
				options={templateTypeOptions}
				value={value}
				onChange={onChange}
			/>
		</DialogField>
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
