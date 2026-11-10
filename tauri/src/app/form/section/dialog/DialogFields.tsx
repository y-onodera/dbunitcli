import { useState } from "react";
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

// 前後の空白を除き、空の要素は持たない
function splitComma(text: string): string[] {
	return text
		.split(",")
		.map((it) => it.trim())
		.filter((it) => it !== "");
}

// カンマ区切りで入力した文字列を配列として編集するText。
// 打ちかけの区切り（末尾のカンマ・空白）を消さないよう入力中の文字列を持ち、valuesが外から変わったときだけ作り直す
export function CommaSeparatedText({
	name,
	values,
	handleChange,
}: {
	name: string;
	values: string[];
	handleChange: (values: string[]) => void;
}) {
	const joined = values.join(",");
	const [text, setText] = useState(joined);
	const shown = splitComma(text).join(",") === joined ? text : joined;
	return (
		<Text
			name={name}
			value={shown}
			handleChange={(ev) => {
				setText(ev.target.value);
				handleChange(splitComma(ev.target.value));
			}}
		/>
	);
}
