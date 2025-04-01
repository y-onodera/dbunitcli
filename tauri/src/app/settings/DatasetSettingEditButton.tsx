import { deleteDatasetSettings, loadDatasetSettings, useSetDatasetSettings, } from '../../context/DatasetSettingsProvider';
import type { DatasetSettings } from '../../model/DatasetSettings';
import DatasetSettingsDialog from './DatasetSettingsDialog';
import ResourceEditButton, { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';

/**
 * データセット設定の編集ダイアログを表示するためのボタンコンポーネント
 */
export default function DatasetSettingEditButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    const setDatasetSettings = useSetDatasetSettings();

    /**
     * ダイアログを描画するときに呼び出す関数
     *
     * @param open ダイアログの開閉状態
     * @param closeDialog ダイアログを閉じるためのコールバック
     */
    const renderDialog = (open: boolean, closeDialog: () => void) => {
        if (!open) return null;
        return (
            <DatasetSettingsDialog
                fileName={path}
                setFileName={(fileName: string) => {
                    setPath(fileName);
                    closeDialog();
                }}
                handleDialogClose={closeDialog}
                handleSave={(newPath: string) => {
                    setPath(newPath);
                    closeDialog();
                }}
            />
        );
    };

    return (
        <ResourceEditButton<DatasetSettings>
            path={path}
            loadResource={loadDatasetSettings}
            handleSetResource={setDatasetSettings}
            renderDialog={renderDialog}
        />
    );
}
export function RemoveDatasetSettingButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={deleteDatasetSettings}
        />
    );
}
