import { useState } from 'react';
import ResourceFileDialog from '../../components/dialog/ResourceFileDialog';
import { useSaveDataSource } from '../../context/QueryDatasourceProvider';
import type { QueryDatasourceType } from '../../model/QueryDatasource';

type SqlEditorSetting = {
    value: string;
};

type SqlEditorDialogProps = {
    type: QueryDatasourceType;
    fileName: string;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
    value: string;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
    const [setting, setSetting] = useState<SqlEditorSetting>({ value: props.value });
    const saveDataSource = useSaveDataSource();

    const handleCommit = async (path: string) => {
        const result = await saveDataSource({
            type: props.type,
            name: path,
            contents: setting.value
        });
        if (result === 'success') {
            props.handleSave(path);
        }
    };

    return (
        <ResourceFileDialog
            handleDialogClose={props.handleDialogClose}
            fileName={props.fileName}
            handleSave={handleCommit}
        >
            <div className="w-[640px] p-4">
                <h2 className="text-xl font-bold mb-4">
                    {props.type === 'sql' ? 'Edit SQL' : 'Edit Table Definition'}
                </h2>
                <div className="relative">
                    <textarea id="contents"
                        className="w-full h-96 p-4 border border-gray-200 rounded-lg shadow-sm
                         font-mono text-base bg-white
                         focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={setting.value}
                        onChange={(e) => setSetting({ value: e.target.value })}
                        placeholder={props.type === 'sql' ? 'Enter SQL query...' : 'Enter table definition...'}
                        spellCheck={false}
                    />
                </div>
            </div>
        </ResourceFileDialog>
    );
}