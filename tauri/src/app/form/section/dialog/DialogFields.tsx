import { Text } from "../../../../components/dialog";
import { ResourceDatalist } from "../../../../components/element/Input";

// 候補(datalist)付きのText。候補が無い間はdatalistを描画しない
export function TextWithDatalist({
	name,
	value,
	options,
	handleChange,
}: {
	name: string;
	value: string;
	options: string[];
	handleChange: (ev: React.ChangeEvent<HTMLInputElement>) => void;
}) {
	const hasOptions = options.length > 0;
	return (
		<>
			<Text
				name={name}
				value={value}
				list={hasOptions ? `${name}_list` : undefined}
				handleChange={handleChange}
			/>
			{hasOptions && <ResourceDatalist id={name} resources={options} />}
		</>
	);
}

// カンマ区切りで入力した文字列を配列として編集するText
export function CommaSeparatedText({
	name,
	values,
	handleChange,
}: {
	name: string;
	values: string[];
	handleChange: (values: string[]) => void;
}) {
	return (
		<Text
			name={name}
			value={values.join(",")}
			handleChange={(ev) => handleChange(ev.target.value.split(","))}
		/>
	);
}
