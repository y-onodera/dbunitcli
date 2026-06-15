import { useState } from "react";
import { Fieldset, Select, SettingDialog, Text } from "../../../../components/dialog";
import type { ColumnDef } from "../../../../model/FixedColumnDef";

export default function ColumnDefSettingDialog(props: {
	setting: ColumnDef;
	handleDialogClose: () => void;
	handleCommit: (newSetting: ColumnDef) => void;
}) {
	const [target, setTarget] = useState(props.setting);

	return (
		<SettingDialog
			setting={target}
			handleDialogClose={props.handleDialogClose}
			handleCommit={props.handleCommit}
		>
			<Fieldset>
				<Text
					name="name"
					value={target.name}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ name: ev.target.value }))
					}
				/>
				<Text
					name="length"
					value={String(target.length)}
					handleChange={(ev) =>
						setTarget((cur) =>
							cur.with({ length: Number(ev.target.value) || 0 }),
						)
					}
				/>
				<Select
					name="align"
					defaultValue={target.align}
					handleOnChange={async (selected) =>
						setTarget((cur) => cur.with({ align: selected }))
					}
				>
					<option value="left">left</option>
					<option value="right">right</option>
				</Select>
				<Text
					name="pad"
					value={target.pad}
					handleChange={(ev) =>
						setTarget((cur) => cur.with({ pad: ev.target.value.slice(0, 1) || " " }))
					}
				/>
			</Fieldset>
		</SettingDialog>
	);
}
