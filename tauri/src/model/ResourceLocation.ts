// 編集ダイアログで開いたリソースファイルの場所（sidecarの`${base}/locate`の応答）。
// folder: 開いたファイルのフォルダ、defaultFolder: 新規保存先のフォルダ、fileName: ファイル名、
// inDefaultFolder: 開いたファイルがdefaultFolder直下か、defaultFolderHasFile: defaultFolder直下に同名のファイルがあるか
export type ResourceLocation = {
	folder: string;
	defaultFolder: string;
	fileName: string;
	inDefaultFolder: boolean;
	defaultFolderHasFile: boolean;
};
