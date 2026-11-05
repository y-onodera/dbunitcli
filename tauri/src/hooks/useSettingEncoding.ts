import { useDatasetSrcInfo } from "../context/DatasetSrcInfoProvider";
import { useSettingEncodingContext } from "../context/SettingEncodingProvider";

// 設定ファイル（-setting・-unitSetting・-xlsxSchema）をコマンド実行時と同じ文字コードで読み書きするための値。
// SettingEncodingProviderの範囲外はdataset読込フォームの-settingEncodingを使う。空ならsidecarがシステムの既定を使う
export function useSettingEncoding(): string {
	const encoding = useSettingEncodingContext();
	const datasetSrcInfo = useDatasetSrcInfo();
	return encoding ?? datasetSrcInfo.values.settingEncoding ?? "";
}
