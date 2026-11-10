import { createContext, use, useState } from "react";
import { ButtonIcon } from "../element/ButtonIcon";
import { CloseFullscreenIcon, OpenInFullIcon } from "../element/Icon";

type DialogMaximize = {
	maximized: boolean;
	// maximizable な枠の中でだけ定義される
	toggle?: () => void;
};

export const DialogMaximizeContext = createContext<DialogMaximize>({
	maximized: false,
});

export function useDialogMaximize() {
	return use(DialogMaximizeContext);
}

// 枠（FullDialog / ModalOverlay）ごとに状態を持つ。入れ子の枠が親の最大化を引き継がないよう、maximizable でなくても Provider で値を上書きする
export function useDialogMaximizeState(maximizable: boolean): DialogMaximize {
	const [maximized, setMaximized] = useState(false);
	return {
		maximized,
		toggle: maximizable ? () => setMaximized((m) => !m) : undefined,
	};
}

// DialogTitle の右端に置く
export function MaximizeToggleButton() {
	const { maximized, toggle } = useDialogMaximize();
	if (!toggle) {
		return null;
	}
	const title = maximized ? "Restore" : "Maximize";
	return (
		<span className="ml-auto">
			<ButtonIcon handleClick={toggle}>
				{maximized ? (
					<CloseFullscreenIcon title={title} />
				) : (
					<OpenInFullIcon title={title} />
				)}
			</ButtonIcon>
		</span>
	);
}
