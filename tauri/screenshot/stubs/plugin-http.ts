// sidecar への通信をブラウザの fetch に置き換える。オリジンを外し、vite の proxy 経由で送る（sidecar の CORS は tauri のオリジンだけを許可しているため）
export const fetch = (input: string, init?: RequestInit) =>
	window.fetch(input.replace(/^https?:\/\/[^/]+/, ""), init);
