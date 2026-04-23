import { ButtonIcon } from "../element/ButtonIcon";
import { HelpIcon } from "../element/Icon";
import { openHelpWindow } from "../../utils/helpWindow";

export function SectionHelpButton(props: { command: string; label: string }) {
	return (
		<ButtonIcon
			title="Help"
			handleClick={() => {
				openHelpWindow(props.command, props.label);
			}}
		>
			<HelpIcon />
		</ButtonIcon>
	);
}

export function DialogHelpButton(props: { command: string; label: string }) {
	return (
		<div className="flex justify-end px-4 pt-2">
			<SectionHelpButton command={props.command} label={props.label} />
		</div>
	);
}
