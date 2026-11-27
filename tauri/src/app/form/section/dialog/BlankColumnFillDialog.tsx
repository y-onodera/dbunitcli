import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
} from "../../../../components/element/Input";
import {
	blankColumnPositions,
	fillBlankColumns,
} from "../../../../model/HeaderName";
import ResourceEditButton from "./ResourceEditButton";

const DEFAULT_NAME_ID = "blankColumnFill_defaultName";

export function BlankColumnFillButton(props: {
	fieldName: string;
	value: string;
	onApply: (filled: string) => void;
}) {
	if (blankColumnPositions(props.value).length === 0) {
		return null;
	}
	return (
		<ResourceEditButton
			renderTrigger={(openDialog) => (
				<BlueEditButton
					title={`fill blank columns of ${props.fieldName}`}
					handleClick={openDialog}
				/>
			)}
			renderDialog={(open, closeDialog) =>
				open ? (
					<BlankColumnFillDialog
						{...props}
						onApply={(filled) => {
							props.onApply(filled);
							closeDialog();
						}}
						onClose={closeDialog}
					/>
				) : null
			}
		/>
	);
}

function BlankColumnFillDialog(props: {
	fieldName: string;
	value: string;
	onApply: (filled: string) => void;
	onClose: () => void;
}) {
	const [defaultName, setDefaultName] = useState("col");
	const filled = fillBlankColumns(props.value, defaultName);
	return (
		<ModalOverlay onClose={props.onClose}>
			<DialogTitle>Fill Blank Columns</DialogTitle>
			<DialogField>
				<InputLabel text="default name" id={DEFAULT_NAME_ID} required={true} />
				<ControllTextBox
					name={DEFAULT_NAME_ID}
					id={DEFAULT_NAME_ID}
					required={true}
					value={defaultName}
					handleChange={(ev) => setDefaultName(ev.target.value)}
				/>
			</DialogField>
			<DialogField last>
				<PreviewField
					id="blankColumnFill_preview"
					label={props.fieldName}
					value={filled}
				/>
			</DialogField>
			<DialogActions>
				<BlueButton
					title="Apply"
					disabled={defaultName.trim() === ""}
					handleClick={() => props.onApply(filled)}
				/>
				<WhiteButton title="Cancel" handleClick={props.onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
