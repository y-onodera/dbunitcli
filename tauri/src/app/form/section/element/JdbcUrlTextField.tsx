import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import type { CommandParam } from "../../../../model/CommandParam";
import JdbcUrlBuilderDialog from "../../../settings/JdbcUrlBuilderDialog";
import ResourceText from "./ResourceText";

export default function JdbcUrlTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandParam;
	onValueChange: (name: string, value: string) => void;
}) {
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			resourceFiles={[]}
			handleValueChange={(value) => onValueChange(element.name, value)}
		>
			{({ path, setPath }) => {
				const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
					const newPath = typeof action === "function" ? action(path) : action;
					setPath(newPath);
					onValueChange(element.name, newPath);
				};
				return <JdbcUrlBuilderButton path={path} setPath={wrappedSetPath} />;
			}}
		</ResourceText>
	);
}

function JdbcUrlBuilderButton({
	path,
	setPath,
}: {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<JdbcUrlBuilderDialog
					currentUrl={path}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(url) => {
						setPath(url);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
