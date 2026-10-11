import type { ReactNode } from "react";
import { ExpandButton } from "../element/ButtonIcon";
import { SectionHelpButton } from "./DialogHelpButton";

export function SectionFieldset({ children }: { children: ReactNode }) {
	return (
		<fieldset className="border border-border-subtle p-3">{children}</fieldset>
	);
}

export function SectionLegend(props: {
	title: ReactNode;
	command?: string;
	label?: string;
}) {
	return (
		<legend className="flex items-center gap-1">
			{props.title}
			{props.command && props.label && (
				<SectionHelpButton command={props.command} label={props.label} />
			)}
		</legend>
	);
}

// 「xxx option」展開ボタンとヘルプボタンを横並びにしたセクション先頭行
export function SectionOptionHeader(props: {
	showOptional: boolean;
	toggleOptional: () => void;
	caption: string;
	command: string;
	label: string;
}) {
	return (
		<div className="flex items-center gap-2">
			<ExpandButton
				toggleOptional={props.toggleOptional}
				showOptional={props.showOptional}
				caption={props.caption}
			/>
			<SectionHelpButton command={props.command} label={props.label} />
		</div>
	);
}
