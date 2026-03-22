import type { SelectProp } from "./FormElementProp";
import PlainSelect from "./PlainSelect";
import SrcTypeSelect from "./SrcTypeSelect";

export default function Select(prop: SelectProp) {
	if (prop.element.name === "srcType") {
		return <SrcTypeSelect {...prop} />;
	}
	return <PlainSelect {...prop} />;
}
