import {
	XlsxSchema,
	type XlsxSchemaBuilder,
} from "../../../../model/XlsxSchema";
import {
	type GenerateButtonProps,
	ModelGenerateButton,
	type ModelGenerateConfig,
} from "./GenerateDialog";
import { XlsxSchemaEditDialog } from "./XlsxSchemaDialog";

const xlsxSchemaGenerateConfig: ModelGenerateConfig<
	XlsxSchemaBuilder,
	XlsxSchema
> = {
	generateType: "xlsxSchema",
	formId: "xlsxSchemaGenerateForm",
	name: "xlsxSchemaGenerate",
	command: "dialogs/xlsx-schema-generate",
	label: "Xlsx Schema Generate",
	title: "Generate Xlsx Schema",
	endpoint: "xlsx-schema/generate",
	build: XlsxSchema.build,
	// 生成後は既存の編集ダイアログで内容確認・命名保存する
	renderEditDialog: (schema, props) => (
		<XlsxSchemaEditDialog schema={schema} {...props} />
	),
};

export function XlsxSchemaGenerateButton({ setPath }: GenerateButtonProps) {
	return (
		<ModelGenerateButton config={xlsxSchemaGenerateConfig} setPath={setPath} />
	);
}
