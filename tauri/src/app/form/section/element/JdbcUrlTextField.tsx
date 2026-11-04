import type { CommandOption } from "../../../../model/CommandOption";
import { JdbcUrlBuilderButton } from "../dialog/JdbcUrlBuilderDialog";
import PlainText from "./PlainText";

export default function JdbcUrlTextField({
	prefix,
	element,
	hidden,
	onValueChange,
}: {
	prefix: string;
	element: CommandOption;
	hidden?: boolean;
	onValueChange: (name: string, value: string) => void;
}) {
	return (
		<PlainText
			prefix={prefix}
			element={element}
			handleValueChange={(value: string) => onValueChange(element.name, value)}
			hidden={hidden}
		>
			{({ path, setPath }) => (
				<JdbcUrlBuilderButton value={path} setValue={setPath} />
			)}
		</PlainText>
	);
}
