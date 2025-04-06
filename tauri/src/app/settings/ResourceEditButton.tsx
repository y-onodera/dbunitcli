import { type ReactElement, useState } from 'react';
import { EditButton, RemoveButton } from '../../components/element/ButtonIcon';
import { useEnviroment } from '../../context/EnviromentProvider';

/**
 * ResourcesEditButton と RemoveResource が共通で受け取るプロパティ
 */
export type ResourceEditButtonProp = {
    /**
     * 選択中のファイルパス
     */
    path: string;
    /**
     * ファイルパスを更新するための関数
     */
    setPath: (value: string) => void;
}

/**
 * RemoveResourceで受け取るプロパティ
 */
type RemoveResourceProp = ResourceEditButtonProp & {
    /**
     * リソースを読み込むための関数
     * @param apiUrl APIのエンドポイントURL
     * @param path リソースのパス
     */
    deleteResource: (apiUrl: string, path: string) => Promise<string>;
}

/**
 * ResourcesEditButtonが受け取るプロパティ
 */
type ResourcesEditButtonProps<T> = {
    /**
     * 選択中のファイルパス
     */
    path: string;
    /**
     * リソースを読み込むための関数
     * @param apiUrl APIのエンドポイントURL
     * @param path リソースのパス
     */
    loadResource: (apiUrl: string, path: string) => Promise<T>;
    /**
     * 読み込んだリソースをContextなどに設定するための関数
     */
    handleSetResource: (resource: T) => void;
    /**
     * ダイアログを描画する関数
     * @param dialogOpen ダイアログ表示状態
     * @param closeDialog ダイアログを閉じる関数
     *
     * @returns JSX.Element または null
     */
    renderDialog: (dialogOpen: boolean, closeDialog: () => void) => ReactElement | null;
}

/**
 * リソースファイルを削除するコンポーネント
 * データセット設定またはXlsxスキーマを削除します
 */
export function RemoveResource({ deleteResource, path, setPath }: RemoveResourceProp) {
    const environment = useEnviroment();

    const handleRemove = async () => {
        const confirmed = await window.confirm(`${path}を削除してもよろしいですか？`);
        if (!confirmed) return;

        try {
            const result = await deleteResource(environment.apiUrl, path);
            if (result === 'success') {
                setPath('');
            } else {
                alert('削除に失敗しました');
            }
        } catch (ex) {
            alert(ex);
        }
    };

    return <RemoveButton title="" handleClick={handleRemove} />;
}

/**
 * リソース読み込みとダイアログ表示を共通化したコンポーネント
 * @template T 読み込むリソースの型
 */
export default function ResourceEditButton<T>({
    path,
    loadResource,
    handleSetResource,
    renderDialog,
}: ResourcesEditButtonProps<T>) {
    const environment = useEnviroment();
    const [dialogOpen, setDialogOpen] = useState(false);

    /**
     * ダイアログを開いてリソースを読み込む
     */
    const openDialog = () => {
        loadResource(environment.apiUrl, path)
            .then((res) => {
                handleSetResource(res);
            })
            .catch((ex) => {
                alert(ex);
            });
        setDialogOpen(true);
    };

    /**
     * ダイアログを閉じる
     */
    const closeDialog = () => {
        setDialogOpen(false);
    };

    return (
        <>
            {renderDialog(dialogOpen, closeDialog)}
            <EditButton handleClick={openDialog} />
        </>
    );
}