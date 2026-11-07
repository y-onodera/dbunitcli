import { createContext, useContext, useState } from "react";
import { ButtonIcon } from "../element/ButtonIcon";
import { CloseFullscreenIcon, OpenInFullIcon } from "../element/Icon";
import { TextArea } from "../element/Input";

type DialogMaximize = {
	maximized: boolean;
	// maximizable な枠の中でだけ定義される
	toggle?: () => void;
};

const DialogMaximizeContext = createContext<DialogMaximize>({
	maximized: false,
});

export function useDialogMaximize() {
	return useContext(DialogMaximizeContext);
}

export const DialogMaximizeProvider = DialogMaximizeContext;

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

// 最大化中は残りの高さを埋める。親を DialogBody か DialogField grow にする
export function DialogTextArea(props: Parameters<typeof TextArea>[0]) {
	const { maximized } = useDialogMaximize();
	return <TextArea {...props} fill={maximized} />;
}
