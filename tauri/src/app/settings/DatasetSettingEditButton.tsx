import { useDeleteDatasetSettings } from '../../context/DatasetSettingsProvider';
import DatasetSettingsDialog from './DatasetSettingsDialog';
import ResourceEditButton, { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';

/**
 * データセット設定の編集ダイアログを表示するためのボタンコンポーネント
 */
export default function DatasetSettingEditButton({
    path,
    setPath,
}: ResourceEditButtonProp) {

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
                handleDialogClose={closeDialog}
                handleSave={(newPath: string) => {
                    setPath(newPath);
                    closeDialog();
                }}
            />
        );
    };

    return (
        <ResourceEditButton renderDialog={renderDialog} />
    );
}
export function RemoveDatasetSettingButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    const deleteSettings = useDeleteDatasetSettings();
    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={deleteSettings}
        />
    );
}
