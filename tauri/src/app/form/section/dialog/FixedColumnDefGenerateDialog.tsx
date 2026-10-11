import {
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../../../../model/FixedColumnDef";
import { FixedColumnDefEditDialog } from "./FixedColumnDefDialog";
import { createModelGenerateButton } from "./GenerateDialog";

export const FixedColumnDefGenerateButton = createModelGenerateButton<
	FixedColumnDefBuilder,
	FixedColumnDef
>({
	generateType: "fixedColumnDef",
	formId: "fixedColumnDefGenerateForm",
	name: "fixedColumnDefGenerate",
	command: "dialogs/fixed-column-def-generate",
	label: "Fixed Column Def Generate",
	title: "Generate Fixed Column Def",
	endpoint: "fixed-column-def/generate",
	build: FixedColumnDef.build,
	// 生成後は既存の編集ダイアログで内容確認・命名保存する
	renderEditDialog: (def, props) => (
		<FixedColumnDefEditDialog def={def} {...props} />
	),
});
