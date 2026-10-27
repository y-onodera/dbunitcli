import {
	DialogBody,
	DialogField,
	DialogTitle,
	SettingDialog,
} from "../../../../components/dialog";
import { PreviewField } from "../../../../components/element/Input";

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
	return (
		<SettingDialog
			fileName=""
			handleDialogClose={handleDialogClose}
			handleSave={handleSave}
		>
			<DialogBody size="sm">
				<DialogTitle>Save JDBC Properties</DialogTitle>

				{jdbcValues.jdbcProperties && (
					<DialogField>
						<PreviewField
							id="jdbcSaveProperties_baseFile"
							label="Base Properties File"
							value={jdbcValues.jdbcProperties}
							placeholder="(not set)"
						/>
					</DialogField>
				)}

				<DialogField>
					<PreviewField
						id="jdbcSaveProperties_url"
						label="URL"
						value={jdbcValues.jdbcUrl ?? ""}
						placeholder="(not set)"
					/>
				</DialogField>

				<DialogField>
					<PreviewField
						id="jdbcSaveProperties_user"
						label="User"
						value={jdbcValues.jdbcUser ?? ""}
						placeholder="(not set)"
					/>
				</DialogField>

				<DialogField last>
					<PreviewField
						id="jdbcSaveProperties_pass"
						label="Pass"
						value={jdbcValues.jdbcPass ? "********" : ""}
						placeholder="(not set)"
					/>
				</DialogField>
			</DialogBody>
		</SettingDialog>
	);
}
