import { useState } from "react";
import { SettingDialog, SettingTable } from "../../../../components/dialog";
import {
	useDeleteFixedColumnDef,
	useFixedColumnDefData,
	useSaveFixedColumnDef,
} from "../../../../hooks/useFixedColumnDef";
import {
	type ColumnDef,
	createColumnDef,
	FixedColumnDef,
} from "../../../../model/FixedColumnDef";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import ColumnDefSettingDialog from "./ColumnDefSettingDialog";
import ResourceEditButton, {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";

export default function FixedColumnDefDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { def, loading } = useFixedColumnDefData(props.fileName);
	if (loading) {
		return <div>Loading...</div>;
	}
	return (
		<FixedColumnDefEditDialog
			def={def}
			fileName={props.fileName}
			handleDialogClose={props.handleDialogClose}
			handleSave={props.handleSave}
		/>
	);
}

export function FixedColumnDefEditDialog(props: {
	def: FixedColumnDef;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const saveDef = useSaveFixedColumnDef();
	const [fixedColumnDef, setFixedColumnDef] = useState(props.def);

	return (
		<SettingDialog
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={(fileName) =>
				saveOnSuccess(
					() => saveDef(fileName, fixedColumnDef),
					() => props.handleSave(fileName),
				)
			}
		>
			<SettingTable<ColumnDef>
				caption="Column Definitions"
				settings={fixedColumnDef.columns}
				setSettings={(convert) =>
					setFixedColumnDef((cur) => new FixedColumnDef(convert(cur.columns)))
				}
				renderSetting={(setting) => setting.displayName()}
				SettingDialogComponent={({
					setting,
					handleDialogClose,
					handleCommit,
				}) => (
					<ColumnDefSettingDialog
						setting={setting}
						handleDialogClose={handleDialogClose}
						handleCommit={handleCommit}
					/>
				)}
				newSetting={createColumnDef}
				getKey={(setting) => setting.displayName()}
			/>
		</SettingDialog>
	);
}

export function FixedColumnDefEditButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<FixedColumnDefDialog
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

export function RemoveFixedColumnDefButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const deleteDef = useDeleteFixedColumnDef();

	return (
		<RemoveResource path={path} setPath={setPath} deleteResource={deleteDef} />
	);
}
