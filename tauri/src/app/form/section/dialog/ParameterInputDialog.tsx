import { useState } from "react";
import { Fieldset, KeyValues, SettingDialog } from "../../../../components/dialog";

export function ParameterInputDialog(props: {
	params: { [key: string]: string };
	handleDialogClose: () => void;
	handleCommit: (params: { [key: string]: string }) => void;
}) {
	const [params, setParams] = useState(props.params);

	const removeEntry = (
		index: number,
		current: { [key: string]: string },
	) => {
		const entries = Object.entries(current);
		entries.splice(index, 1);
		return Object.fromEntries(entries);
	};

	const handleChange = (
		index: number,
		newValue: { [prop: string]: string },
	) => {
		const newKey = Object.keys(newValue)[0];
		if (!newKey) {
			setParams((cur) => removeEntry(index, cur));
		} else {
			setParams((cur) => {
				const entries = Object.entries(cur);
				entries[index] = [newKey, Object.values(newValue)[0] ?? ""];
				return Object.fromEntries(entries);
			});
		}
	};

	const handleRemove = (index: number) => {
		setParams((cur) => removeEntry(index, cur));
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
