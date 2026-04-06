import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import type { CommandOption } from "../../../../model/CommandParam";
import JdbcUrlBuilderDialog from "../../../settings/JdbcUrlBuilderDialog";
import PlainText from "./PlainText";

export default function JdbcUrlTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandOption;
	onValueChange: (name: string, value: string) => void;
}) {
	return (
		<PlainText
			prefix={prefix}
			element={element}
			handleValueChange={(value: string) => onValueChange(element.name, value)}
		>
			{({ value, setValue }) => {
				const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
					const newPath = typeof action === "function" ? action(value) : action;
					setValue(newPath);
					onValueChange(element.name, newPath);
				};
				return <JdbcUrlBuilderButton value={value} setValue={wrappedSetPath} />;
			}}
		</PlainText>
	);
}

function JdbcUrlBuilderButton({
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
