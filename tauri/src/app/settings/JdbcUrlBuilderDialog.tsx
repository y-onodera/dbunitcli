import { useState } from "react";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
	SelectBox,
} from "../../components/element/Input";
import {
	DEFAULT_PORTS,
	SERVICE_NAME_LABEL,
	buildJdbcUrl,
	parseJdbcUrl,
} from "../../model/JdbcUrlBuilder";
import type { JdbcUrlBuilderState, RdbType } from "../../model/JdbcUrlBuilder";

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
	const initialRdbType: RdbType = parsed.rdbType ?? "postgres";
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
			handleCommit={(url) => { if (url) { handleSave(url);  }}}
			commitLabel="Apply"
		>
			<div className="w-[480px]">
				<h2 className="text-lg font-bold mb-4">JDBC URL Builder</h2>

				<div className="mb-3">
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
				</div>

				<div className="mb-3">
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
				</div>

				<div className="mb-3">
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
				</div>

				<div className="mb-3">
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
				</div>

				<div className="mb-4">
					<PreviewField
						id="jdbcUrlBuilder_preview"
						label="Preview"
						value={builtUrl}
						placeholder="(Enter host and service name)"
					/>
				</div>
			</div>
		</SettingDialog>
	);
}
