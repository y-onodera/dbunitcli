import { createContext, useContext } from "react";

// compareの-settingEncoding・generateの-unitSettingEncodingの入力中の値。
// 範囲内の設定ファイルの編集ダイアログ・プレビューが、読み書きする文字コードに使う（範囲外はundefined）
const SettingEncodingContext = createContext<string | undefined>(undefined);

export function SettingEncodingProvider({
	children,
	encoding,
}: {
	children: React.ReactNode;
	encoding: string;
}) {
	return (
		<SettingEncodingContext.Provider value={encoding}>
			{children}
		</SettingEncodingContext.Provider>
	);
}

export function useSettingEncodingContext(): string | undefined {
	return useContext(SettingEncodingContext);
}
