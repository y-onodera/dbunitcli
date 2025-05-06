import { useDeleteXlsxSchema } from '../../context/XlsxSchemaProvider';
import ResourceEditButton, { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';
import XlsxSchemaDialog from './XlsxSchemaDialog';

/**
 * XLSXスキーマの編集ダイアログを表示するためのボタンコンポーネント
 */
export default function XlsxSchemaEditButton({
    path,
    setPath,
}: ResourceEditButtonProp) {

    /**
     * ダイアログを描画するときに呼び出す関数
     *
     * @param open ダイアログの開閉状態
     * @param closeDialog ダイアログを閉じるための関数
     * @returns JSX.Element | null
     */
    const renderDialog = (open: boolean, closeDialog: () => void) => {
        if (!open) return null;
        return (
            <XlsxSchemaDialog
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
export function RemoveXlsxSchemaButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    const deleteSchema = useDeleteXlsxSchema();

    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={deleteSchema}
        />
    );
}
