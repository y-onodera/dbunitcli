import { openHelpWindow } from "../../utils/helpWindow";
import { ButtonIcon } from "../element/ButtonIcon";
import { HelpIcon } from "../element/Icon";

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
