import { useState } from "react";
import { Fieldset, KeyValues, SettingDialog } from "../../../../components/dialog";

export function ParameterInputDialog(props: {
	params: { [key: string]: string };
	handleDialogClose: () => void;
	handleCommit: (params: { [key: string]: string }) => void;
}) {
	const [params, setParams] = useState(props.params);

	const handleChange = (
		index: number,
		newValue: { [prop: string]: string },
	) => {
		const entries = Object.entries(params);
		const newKey = Object.keys(newValue)[0];
		if (!newKey) {
			entries.splice(index, 1);
		} else {
			entries[index] = [newKey, Object.values(newValue)[0] ?? ""];
		}
		setParams(Object.fromEntries(entries));
	};

	const handleRemove = (index: number) => {
		const entries = Object.entries(params);
		entries.splice(index, 1);
		setParams(Object.fromEntries(entries));
	};

	return (
		<SettingDialog
			setting={params}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
			commitLabel="Apply"
		>
			<Fieldset legend="Parameters (-P)">
				<KeyValues
					name="parameter"
					values={params}
					handleChange={handleChange}
					handleRemove={handleRemove}
				/>
			</Fieldset>
		</SettingDialog>
	);
}
