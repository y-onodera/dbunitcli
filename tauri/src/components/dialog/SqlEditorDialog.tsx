import { useState } from 'react';
import { saveDataSource } from '../../context/DataSourceProvider';
import { useEnviroment } from '../../context/EnviromentProvider';
import ResourceFileDialog from './ResourceFileDialog';

type SqlEditorSetting = {
    value: string;
};

type SqlEditorDialogProps = {
    type: 'sql' | 'table';
    fileName: string;
    setFileName: (fileName: string) => void;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
    value: string;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
    const [setting, setSetting] = useState<SqlEditorSetting>({ value: props.value });
    const environment = useEnviroment();

    const handleCommit = async (path: string) => {
        const result = await saveDataSource(environment.apiUrl, {
            type: props.type as 'sql' | 'table',
            fileName: path,
            contents: setting.value
        });
        if (result !== 'failed') {
            props.handleSave(path);
        }
    };

    return (
        <ResourceFileDialog
            handleDialogClose={props.handleDialogClose}
            fileName={props.fileName}
            setFileName={props.setFileName}
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