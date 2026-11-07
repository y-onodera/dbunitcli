// 撮影ではヘルプウィンドウを開かない
export class WebviewWindow {
	static async getByLabel(_label: string): Promise<WebviewWindow | null> {
		return null;
	}
	once(_event: string, _handler: (e: unknown) => void) {}
}
