import { deleteXlsxSchema, loadXlsxSchema, useSetXlsxSchema, } from '../../context/XlsxSchemaProvider';
import type { XlsxSchema } from '../../model/XlsxSchema';
import ResourceEditButton, { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';
import XlsxSchemaDialog from './XlsxSchemaDialog';

/**
 * XLSXスキーマの編集ダイアログを表示するためのボタンコンポーネント
 */
export default function XlsxSchemaEditButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    const setXlsxSchema = useSetXlsxSchema();

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
        <ResourceEditButton<XlsxSchema>
            path={path}
            loadResource={loadXlsxSchema}
            handleSetResource={setXlsxSchema}
            renderDialog={renderDialog}
        />
    );
}
export function RemoveXlsxSchemaButton({
    path,
    setPath,
}: ResourceEditButtonProp) {
    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={deleteXlsxSchema}
        />
    );
}
