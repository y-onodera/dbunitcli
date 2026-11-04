import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import type { CommandOption } from "../../../../model/CommandOption";
import { RemoveResourceButton } from "../dialog/ResourceEditButton";
import Text, { TextDropDownMenu } from "./Text";

export default function JdbcPropertiesTextField({
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
	const settings = useResourcesSettings();
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.jdbcFiles}
			showDefaulePath={true}
			handleValueChange={(value) => onValueChange(element.name, value)}
		>
			{({ path, setPath, isValueInDatalist }) => {
				return (
					<TextDropDownMenu
						prefix={prefix}
						element={element}
						path={path}
						setPath={setPath}
						isValueInDatalist={isValueInDatalist}
						removeButton={(closeMenu) => (
							<RemoveResourceButton
								kind="jdbc"
								path={path}
								setPath={setPath}
								onRemoved={closeMenu}
							/>
						)}
						className="mr-24"
					/>
				);
			}}
		</Text>
	);
}
