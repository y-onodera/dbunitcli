import { useState } from "react";
import {
	SettingDialog,
	SettingTable,
	StatusMessage,
} from "../../../../components/dialog";
import {
	useDeleteXlsxSchema,
	useSaveXlsxSchema,
	useXlsxSchemaData,
} from "../../../../hooks/useXlsxSchema";
import type { CellSetting, RowSetting } from "../../../../model/XlsxSchema";
import {
	createCellSetting,
	createRowSetting,
	XlsxSchema,
} from "../../../../model/XlsxSchema";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import { createEditButton, createRemoveButton } from "./ResourceEditButton";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { schema, loading } = useXlsxSchemaData(props.fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<XlsxSchemaEditDialog
			schema={schema}
			fileName={props.fileName}
			handleDialogClose={props.handleDialogClose}
			handleSave={props.handleSave}
		/>
	);
}
export function XlsxSchemaEditDialog(props: {
	schema: XlsxSchema;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const saveSchema = useSaveXlsxSchema();
	const [xlsxSchema, setXlsxSchema] = useState(props.schema);

	return (
		<SettingDialog
			title="Xlsx Schema"
			helpCommand="dialogs/xlsx-schema"
			helpLabel="Xlsx Schema"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={(fileName) =>
				saveOnSuccess(
					() => saveSchema(fileName, xlsxSchema),
					() => props.handleSave(fileName),
				)
			}
		>
			<SettingTable<RowSetting>
				caption="Rows Mapping Tables"
				settings={xlsxSchema.rows}
				setSettings={(convertRows) =>
					setXlsxSchema((cur) => {
						const updatedRows = convertRows(cur.rows);
						return new XlsxSchema(updatedRows, cur.cells);
					})
				}
				SettingDialogComponent={XlsxRowSettingDialog}
				newSetting={createRowSetting}
			/>
			<SettingTable<CellSetting>
				caption="Random Cell Mapping Tables"
				settings={xlsxSchema.cells}
				setSettings={(convertCells) =>
					setXlsxSchema((cur) => {
						const updatedCells = convertCells(cur.cells);
						return new XlsxSchema(cur.rows, updatedCells);
					})
				}
				SettingDialogComponent={XlsxCellSettingDialog}
				newSetting={createCellSetting}
			/>
		</SettingDialog>
	);
}
export const XlsxSchemaEditButton = createEditButton(XlsxSchemaDialog);
export const RemoveXlsxSchemaButton = createRemoveButton(useDeleteXlsxSchema);
