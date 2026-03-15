import { Suspense, use, useState } from "react";
import { SettingDialog, SettingTable } from "../../components/dialog";
import {
	useLoadXlsxSchema,
	useSaveXlsxSchema,
} from "../../hooks/useXlsxSchema";
import {
	type CellSetting,
	createCellSetting,
	createRowSetting,
	type RowSetting,
	XlsxSchema,
} from "../../model/XlsxSchema";
import { saveOnSuccess } from "../../utils/fetchUtils";
import type { SrcInfo } from "../../model/CommandParam";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
	fileName: string;
	srcInfo?: SrcInfo;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const loadXlsxSchema = useLoadXlsxSchema();
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={loadXlsxSchema(props.fileName)}
				fileName={props.fileName}
				srcInfo={props.srcInfo}
				handleDialogClose={props.handleDialogClose}
				handleSave={props.handleSave}
			/>
		</Suspense>
	);
}
function Dialog(props: {
	promise: Promise<XlsxSchema>;
	fileName: string;
	srcInfo?: SrcInfo;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const saveSchema = useSaveXlsxSchema();
	const xlsxSchemaData = use(props.promise);
	const [xlsxSchema, setXlsxSchema] = useState(xlsxSchemaData);

	return (
		<SettingDialog
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
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={({
					setting,
					handleDialogClose,
					handleCommit,
				}) => (
					<XlsxRowSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
						srcInfo={props.srcInfo}
					/>
				)}
				newSetting={createRowSetting}
				getKey={(setting) => setting.displayName()}
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
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={({
					setting,
					handleDialogClose,
					handleCommit,
				}) => (
					<XlsxCellSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
						srcInfo={props.srcInfo}
					/>
				)}
				newSetting={createCellSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</SettingDialog>
	);
}
