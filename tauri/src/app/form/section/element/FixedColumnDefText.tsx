import { useResourcesSettings } from "../../../../context/WorkspaceResourcesProvider";
import {
	FixedColumnDefEditButton,
	RemoveFixedColumnDefButton,
} from "../dialog/FixedColumnDefDialog";
import type { TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

export default function FixedColumnDefText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
	const settings = useResourcesSettings();

	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={settings.fixedColumnDefs}
			showDefaulePath={true}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath, isValueInDatalist }) => (
				<TextDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					isValueInDatalist={isValueInDatalist}
					editButtons={[
						<FixedColumnDefEditButton key="edit" path={path} setPath={setPath} />,
					]}
					removeButton={() => (
						<RemoveFixedColumnDefButton path={path} setPath={setPath} />
					)}
				/>
			)}
		</Text>
	);
}
