import { useState } from "react";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
} from "../../components/element/Input";

type JdbcSavePropertiesDialogProps = {
	jdbcValues: Record<string, string>;
	handleDialogClose: () => void;
	handleSave: (name: string) => void;
};

export default function JdbcSavePropertiesDialog({
	jdbcValues,
	handleDialogClose,
	handleSave,
}: JdbcSavePropertiesDialogProps) {
	const [name, setName] = useState("");

	return (
		<SettingDialog
			setting={name}
			handleDialogClose={handleDialogClose}
			handleCommit={(fileName) => {
				if (fileName) handleSave(fileName);
			}}
			commitLabel="Save"
		>
			<div className="w-[480px]">
				<h2 className="text-lg font-bold mb-4">Save JDBC Properties</h2>

				<div className="mb-3">
					<InputLabel
						text="File Name"
						id="jdbcSaveProperties_name"
						required={true}
					/>
					<ControllTextBox
						name="jdbcSaveProperties_name"
						id="jdbcSaveProperties_name"
						required={true}
						value={name}
						handleChange={(ev) => setName(ev.target.value)}
					/>
				</div>

				<div className="mb-3">
					<PreviewField
						id="jdbcSaveProperties_url"
						label="URL"
						value={jdbcValues.jdbcUrl ?? ""}
						placeholder="(not set)"
					/>
				</div>

				<div className="mb-3">
					<PreviewField
						id="jdbcSaveProperties_user"
						label="User"
						value={jdbcValues.jdbcUser ?? ""}
						placeholder="(not set)"
					/>
				</div>

				<div className="mb-4">
					<PreviewField
						id="jdbcSaveProperties_pass"
						label="Pass"
						value={jdbcValues.jdbcPass ? "********" : ""}
						placeholder="(not set)"
					/>
				</div>
			</div>
		</SettingDialog>
	);
}
