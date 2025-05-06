import { useState } from 'react';
import { EditButton } from '../../components/element/ButtonIcon';
import { useDeleteDataSource, useLoadDataSource } from '../../context/QueryDatasourceProvider';
import type { QueryDatasourceType } from '../../model/QueryDatasource';
import { RemoveResource, type ResourceEditButtonProp } from './ResourceEditButton';
import SqlEditorDialog from './SqlEditorDialog';

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
    const loadDataSource = useLoadDataSource();
    const handleOpen = async () => {
        try {
            if (path) {
                const result = await loadDataSource(type, path);
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
    const deleteDataSource = useDeleteDataSource(type);

    return (
        <RemoveResource
            path={path}
            setPath={setPath}
            deleteResource={deleteDataSource}
        />
    );
}