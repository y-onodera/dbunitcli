import type { TextProp } from "./FormElementProp";
import Text from "./Text";

const ENCODING_CANDIDATES = ["UTF-8", "MS932"];

export default function PlainText({
	prefix,
	element,
	hidden,
	handleValueChange: onValueChange,
	children,
}: TextProp) {
	// encoding・settingEncoding・outputEncoding等の文字コード入力欄には候補を出す
	const isEncoding = element.name.toLowerCase().endsWith("encoding");
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			resourceFiles={isEncoding ? ENCODING_CANDIDATES : undefined}
			handleValueChange={onValueChange}
		>
			{children}
		</Text>
	);
}
