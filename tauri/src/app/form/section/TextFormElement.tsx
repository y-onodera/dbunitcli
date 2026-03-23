import FileText from "./FileText";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";

export default function Text(prop: Prop) {
	if (
		prop.element.attribute.type.includes("FILE") ||
		prop.element.attribute.type.includes("DIR")
	) {
		return <FileText {...prop} />;
	}
	return <ResourceText {...prop} />;
}
