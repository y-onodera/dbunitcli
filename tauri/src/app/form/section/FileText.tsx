import FileDropDownMenu from "./FileDropDownMenu";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";

export default function FileText({ prefix, element, hidden, srcType }: Prop) {
	return (
		<ResourceText
			prefix={prefix}
			element={element}
			hidden={hidden}
			srcType={srcType}
			resourceFiles={[]}
		>
			{({ path, setPath }) => (
				<FileDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
				/>
			)}
		</ResourceText>
	);
}
