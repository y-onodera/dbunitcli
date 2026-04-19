import { ButtonIcon } from "../element/ButtonIcon";
import { HelpIcon } from "../element/Icon";
import { openHelpWindow } from "../../utils/helpWindow";

export function DialogHelpButton(props: { command: string; label: string }) {
	return (
		<div className="flex justify-end px-4 pt-2">
			<ButtonIcon
				title="Help"
				handleClick={() => {
					openHelpWindow(props.command, props.label);
				}}
			>
				<HelpIcon />
			</ButtonIcon>
		</div>
	);
}
