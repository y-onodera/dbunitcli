import {
	XlsxSchema,
	type XlsxSchemaBuilder,
} from "../../../../model/XlsxSchema";
import { createModelGenerateButton } from "./GenerateDialog";
import { XlsxSchemaEditDialog } from "./XlsxSchemaDialog";

export const XlsxSchemaGenerateButton = createModelGenerateButton<
	XlsxSchemaBuilder,
	XlsxSchema
>({
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
});
