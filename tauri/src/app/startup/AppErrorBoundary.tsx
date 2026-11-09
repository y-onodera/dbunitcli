import { Component, type ErrorInfo, type ReactNode } from "react";
import { StatusMessage } from "../../components/dialog/DialogLayout";
import { getErrorMessage } from "../../utils/fetchUtils";

type State = { failed: false } | { failed: true; error: unknown };

// 描画中の例外で画面が真っ白になるのを防ぎ、原因を表示する。関数コンポーネントでは書けないためクラスにする
export default class AppErrorBoundary extends Component<
	{ children: ReactNode },
	State
> {
	state: State = { failed: false };

	static getDerivedStateFromError(error: unknown): State {
		return { failed: true, error };
	}

	componentDidCatch(error: unknown, info: ErrorInfo) {
		console.error("Render Error:", error, info.componentStack);
	}

	render() {
		if (!this.state.failed) {
			return this.props.children;
		}
		return (
			<div className="mx-auto max-w-4xl p-4">
				<StatusMessage kind="error">
					Error: {getErrorMessage(this.state.error)}
				</StatusMessage>
			</div>
		);
	}
}
