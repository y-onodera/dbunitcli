import { Suspense, use, useState } from "react";
import { SettingDialog, SettingTable } from "../../../../components/dialog";
import {
	useDeleteXlsxSchema,
	useLoadXlsxSchema,
	useSaveXlsxSchema,
} from "../../../../hooks/useXlsxSchema";
import type { CellSetting, RowSetting } from "../../../../model/XlsxSchema";
import {
	createCellSetting,
	createRowSetting,
	XlsxSchema,
} from "../../../../model/XlsxSchema";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const loadXlsxSchema = useLoadXlsxSchema();
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={loadXlsxSchema(props.fileName)}
				fileName={props.fileName}
				handleDialogClose={props.handleDialogClose}
				handleSave={props.handleSave}
			/>
		</Suspense>
	);
}
function Dialog(props: {
	promise: Promise<XlsxSchema>;
	fileName: string;
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
					/>
				)}
				newSetting={createCellSetting}
				getKey={(setting) => setting.displayName()}
			/>
		</SettingDialog>
	);
}
export function XlsxSchemaEditButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<XlsxSchemaDialog
				fileName={path}
				handleDialogClose={closeDialog}
				handleSave={(newPath: string) => {
					setPath(newPath);
					closeDialog();
				}}
			/>
		);
	};

	return <ResourceEditButton renderDialog={renderDialog} />;
}
export function RemoveXlsxSchemaButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteSchema = useDeleteXlsxSchema();

	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteSchema}
		/>
	);
}
