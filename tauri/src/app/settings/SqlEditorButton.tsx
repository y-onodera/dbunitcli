import { useState } from 'react';
import SqlEditorDialog from '../../components/dialog/SqlEditorDialog';
import { EditButton } from '../../components/element/ButtonIcon';
import { deleteDataSource, loadDataSource } from '../../context/DataSourceProvider';
import { useEnviroment } from '../../context/EnviromentProvider';
import type { QueryDatasourceType } from '../../model/QueryDatasource';
import { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';

type SqlEditorButtonProps = ResourceEditButtonProp & {
    type: QueryDatasourceType;
};

/**
 * SQLエディタまたはテーブル定義エディタを表示するためのボタンコンポーネント
 */
export default function SqlEditorButton({
    path,
    setPath,
    type,
}: SqlEditorButtonProps) {
    const [showDialog, setShowDialog] = useState(false);
    const [content, setContent] = useState('');
    const environment = useEnviroment();

    const handleOpen = async () => {
        try {
            if (path) {
                const result = await loadDataSource(environment.apiUrl, type, path);
                setContent(result);
            }
            setShowDialog(true);
        } catch (ex) {
            alert(ex);
        }
    };

    const handleClose = () => {
        setShowDialog(false);
        setContent('');
    };

    const handleSave = (path: string) => {
        setPath(path);
        setShowDialog(false);
        setContent('');
    };

    return (
        <>
            <EditButton handleClick={handleOpen} />
            {showDialog && (
                <SqlEditorDialog
                    type={type}
                    fileName={path}
                    setFileName={setPath}
                    value={content}
                    handleDialogClose={handleClose}
                    handleSave={handleSave}
                />
            )}
        </>
    );
}

export function RemoveSqlEditorButton({
    path,
    setPath,
    type,
}: SqlEditorButtonProps) {
    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={async (url, path) => {
                return await deleteDataSource(url, type, path);
            }}
        />
    );
}