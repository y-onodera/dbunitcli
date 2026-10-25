import type { TextProp } from "./FormElementProp";
import Text from "./Text";

const ENCODING_CANDIDATES = ["UTF-8", "MS932"];

export default function EncodingText({
	prefix,
	element,
	hidden,
	handleValueChange,
}: TextProp) {
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={ENCODING_CANDIDATES}
			handleValueChange={handleValueChange}
		/>
	);
}
