import { SettingDialog } from "../../components/dialog";
import { PreviewField } from "../../components/element/Input";

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
		<SettingDialog fileName="" handleDialogClose={handleDialogClose} handleSave={handleSave}>
			<div className="w-[480px] p-4">
				<h2 className="text-lg font-bold mb-4">Save JDBC Properties</h2>

				{jdbcValues.jdbcProperties && (
					<div className="mb-3">
						<PreviewField
							id="jdbcSaveProperties_baseFile"
							label="Base Properties File"
							value={jdbcValues.jdbcProperties}
							placeholder="(not set)"
						/>
					</div>
				)}

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
