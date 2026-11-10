import {
	DialogBody,
	DialogField,
	DialogTitle,
	SettingDialog,
} from "../../../../components/dialog";
import { PreviewField } from "../../../../components/element/Input";
import { CONTENT_PARSER } from "../../../../hooks/useResourceFiles";
import type { OperationResult } from "../../../../utils/fetchUtils";
import { ResourceLoader } from "./ResourceLoader";

type JdbcSavePropertiesDialogProps = {
	jdbcValues: Record<string, string>;
	handleDialogClose: () => void;
	save: (name: string, overwrite: boolean) => Promise<OperationResult>;
	handleSave: (name: string) => void;
};

// jdbcPropertiesのファイルを開いたファイルとして、上書きか規定フォルダへの保存かを選ばせる
export default function JdbcSavePropertiesDialog({
	jdbcValues,
	handleDialogClose,
	save,
	handleSave,
}: JdbcSavePropertiesDialogProps) {
	const fileName = jdbcValues.jdbcProperties ?? "";
	return (
		<ResourceLoader kind="jdbc" fileName={fileName} parser={CONTENT_PARSER}>
			{(_, location) => (
				<SettingDialog
					location={location}
					fileName={fileName}
					handleDialogClose={handleDialogClose}
					save={save}
					handleSave={handleSave}
				>
					<DialogBody size="sm">
						<DialogTitle>Save JDBC Properties</DialogTitle>

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
			)}
		</ResourceLoader>
	);
}
