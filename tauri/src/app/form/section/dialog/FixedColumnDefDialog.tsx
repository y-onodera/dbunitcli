import { useState } from "react";
import { SettingDialog, SettingTable } from "../../../../components/dialog";
import {
	FIXED_COLUMN_DEF_PARSER,
	useSaveFixedColumnDef,
} from "../../../../hooks/useFixedColumnDef";
import {
	type ColumnDef,
	createColumnDef,
	FixedColumnDef,
} from "../../../../model/FixedColumnDef";
import ColumnDefSettingDialog from "./ColumnDefSettingDialog";
import type { LoadedResourceDialogProps } from "./ResourceEditButton";
import { ResourceLoader } from "./ResourceLoader";

export default function FixedColumnDefDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	return (
		<ResourceLoader
			kind="fixedColumnDef"
			fileName={props.fileName}
			parser={FIXED_COLUMN_DEF_PARSER}
		>
			{(def, location) => (
				<FixedColumnDefEditDialog
					def={def}
					location={location}
					fileName={props.fileName}
					handleDialogClose={props.handleDialogClose}
					handleSave={props.handleSave}
				/>
			)}
		</ResourceLoader>
	);
}

export function FixedColumnDefEditDialog(
	props: LoadedResourceDialogProps & {
		def: FixedColumnDef;
	},
) {
	const saveDef = useSaveFixedColumnDef();
	const [fixedColumnDef, setFixedColumnDef] = useState(props.def);

	return (
		<SettingDialog
			location={props.location}
			title="Fixed Column Def"
			helpCommand="dialogs/fixed-column-def"
			helpLabel="Fixed Column Def"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={(fileName, overwrite) =>
				saveDef(fileName, fixedColumnDef, overwrite)
			}
			handleSave={props.handleSave}
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
