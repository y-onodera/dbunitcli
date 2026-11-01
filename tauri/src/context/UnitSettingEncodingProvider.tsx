import { createContext, useContext } from "react";

// generateの-unitSettingEncodingの入力中の値。unitSettingの編集ダイアログ（Save and Preview）と
// プレビューボタンが、unitSettingを読むときの文字コードに使う
const UnitSettingEncodingContext = createContext("");

export function UnitSettingEncodingProvider({
	children,
	encoding,
}: {
	children: React.ReactNode;
	encoding: string;
}) {
	return (
		<UnitSettingEncodingContext.Provider value={encoding}>
			{children}
		</UnitSettingEncodingContext.Provider>
	);
}

export function useUnitSettingEncoding(): string {
	return useContext(UnitSettingEncodingContext);
}
