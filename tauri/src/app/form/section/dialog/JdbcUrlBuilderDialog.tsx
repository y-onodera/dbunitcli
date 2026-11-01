import { type Dispatch, type SetStateAction, useState } from "react";
import {
	DialogBody,
	DialogField,
	DialogTitle,
	SettingDialog,
} from "../../../../components/dialog";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
	SelectBox,
} from "../../../../components/element/Input";
import type {
	JdbcUrlBuilderState,
	RdbType,
} from "../../../../model/JdbcUrlBuilder";
import {
	buildJdbcUrl,
	DEFAULT_PORTS,
	parseJdbcUrl,
	SERVICE_NAME_LABEL,
} from "../../../../model/JdbcUrlBuilder";

type JdbcUrlBuilderDialogProps = {
	currentUrl: string;
	handleDialogClose: () => void;
	handleSave: (url: string) => void;
};

export default function JdbcUrlBuilderDialog({
	currentUrl,
	handleDialogClose,
	handleSave,
}: JdbcUrlBuilderDialogProps) {
	const parsed = parseJdbcUrl(currentUrl);
	const initialRdbType: RdbType = parsed.rdbType ?? "oracle";
	const [state, setState] = useState<JdbcUrlBuilderState>({
		rdbType: initialRdbType,
		host: parsed.host ?? "localhost",
		port: parsed.port ?? DEFAULT_PORTS[initialRdbType],
		serviceName: parsed.serviceName ?? "",
	});

	const handleRdbTypeChange = async (selected: string) => {
		const rdbType = selected as RdbType;
		setState((prev) => ({
			...prev,
			rdbType,
			port: DEFAULT_PORTS[rdbType],
		}));
	};

	const builtUrl = buildJdbcUrl(state);

	return (
		<SettingDialog
			setting={builtUrl}
			handleDialogClose={handleDialogClose}
			handleCommit={(url) => {
				if (url) {
					handleSave(url);
				}
			}}
			commitLabel="Apply"
		>
			<DialogBody size="sm">
				<DialogTitle>JDBC URL Builder</DialogTitle>

				<DialogField>
					<InputLabel
						text="Database"
						id="jdbcUrlBuilder_rdbType"
						required={true}
					/>
					<SelectBox
						name="jdbcUrlBuilder_rdbType"
						id="jdbcUrlBuilder_rdbType"
						required={true}
						defaultValue={state.rdbType}
						handleOnChange={handleRdbTypeChange}
					>
						<option value="oracle">Oracle</option>
						<option value="postgres">PostgreSQL</option>
						<option value="h2">H2</option>
					</SelectBox>
				</DialogField>

				<DialogField>
					<InputLabel text="Host" id="jdbcUrlBuilder_host" required={true} />
					<ControllTextBox
						name="jdbcUrlBuilder_host"
						id="jdbcUrlBuilder_host"
						required={true}
						value={state.host}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, host: ev.target.value }))
						}
					/>
				</DialogField>

				<DialogField>
					<InputLabel text="Port" id="jdbcUrlBuilder_port" required={true} />
					<ControllTextBox
						name="jdbcUrlBuilder_port"
						id="jdbcUrlBuilder_port"
						required={true}
						value={state.port}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, port: ev.target.value }))
						}
					/>
				</DialogField>

				<DialogField>
					<InputLabel
						text={SERVICE_NAME_LABEL[state.rdbType]}
						id="jdbcUrlBuilder_serviceName"
						required={true}
					/>
					<ControllTextBox
						name="jdbcUrlBuilder_serviceName"
						id="jdbcUrlBuilder_serviceName"
						required={true}
						value={state.serviceName}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, serviceName: ev.target.value }))
						}
					/>
				</DialogField>

				<DialogField last>
					<PreviewField
						id="jdbcUrlBuilder_preview"
						label="Preview"
						value={builtUrl}
						placeholder="(Enter host and service name)"
					/>
				</DialogField>
			</DialogBody>
		</SettingDialog>
	);
}
export function JdbcUrlBuilderButton({
	value,
	setValue,
}: {
	value: string;
	setValue: Dispatch<SetStateAction<string>>;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<JdbcUrlBuilderDialog
					currentUrl={value}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(url) => {
						setValue(url);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
