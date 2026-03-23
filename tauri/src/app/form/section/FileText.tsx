import ResourceDropDownMenu from "./ResourceDropDownMenu";
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
				<ResourceDropDownMenu
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
