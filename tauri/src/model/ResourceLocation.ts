// 編集ダイアログで開いたリソースファイルの場所（sidecarの`${base}/locate`の応答）。
// folder: 開いたファイルのフォルダ、defaultFolder: 新規保存先のフォルダ（どちらも正規化した絶対パス）、
// fileName: ファイル名、defaultFolderHasFile: defaultFolder直下に同名のファイルがあるか
export type ResourceLocation = {
	folder: string;
	defaultFolder: string;
	fileName: string;
	defaultFolderHasFile: boolean;
};
