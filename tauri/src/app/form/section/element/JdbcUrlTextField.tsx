import type { Dispatch, SetStateAction } from "react";
import type { CommandOption } from "../../../../model/CommandOption";
import { JdbcUrlBuilderButton } from "../dialog/JdbcUrlBuilderDialog";
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
			{({ path, setPath }) => {
				const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
					const newPath = typeof action === "function" ? action(path) : action;
					setPath(newPath);
					onValueChange(element.name, newPath);
				};
				return <JdbcUrlBuilderButton value={path} setValue={wrappedSetPath} />;
			}}
		</PlainText>
	);
}
