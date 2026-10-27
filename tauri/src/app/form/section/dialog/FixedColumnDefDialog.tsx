import { useState } from "react";
import {
	SettingDialog,
	SettingTable,
	StatusMessage,
} from "../../../../components/dialog";
import {
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

export default function FixedColumnDefDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const { def, loading } = useFixedColumnDefData(props.fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
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
				SettingDialogComponent={ColumnDefSettingDialog}
				newSetting={createColumnDef}
			/>
		</SettingDialog>
	);
}
