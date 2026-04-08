import type { TextProp } from "./FormElementProp";
import PlainText from "./PlainText";
import SqlSrcDropDownMenu from "./SqlSrcDropDownMenu";

export default function SqlSrcText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange,
}: TextProp) {
	return (
		<PlainText
			prefix={prefix}
			element={element}
			hidden={hidden}
			handleValueChange={handleValueChange}
		>
			{({ value, setValue }) => (
				<SqlSrcDropDownMenu
					path={value}
					setPath={setValue}
					prefix={prefix}
					element={element}
					srcType={srcType}
				/>
			)}
		</PlainText>
	);
}
