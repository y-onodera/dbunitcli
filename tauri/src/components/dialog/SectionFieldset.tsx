import type { ReactNode } from "react";
import { SectionHelpButton } from "./DialogHelpButton";

export function SectionFieldset({ children }: { children: ReactNode }) {
	return (
		<fieldset className="border border-border-subtle p-3">{children}</fieldset>
	);
}

export function SectionLegend(props: {
	title: ReactNode;
	command: string;
	label: string;
}) {
	return (
		<legend className="flex items-center gap-1">
			{props.title}
			<SectionHelpButton command={props.command} label={props.label} />
		</legend>
	);
}
