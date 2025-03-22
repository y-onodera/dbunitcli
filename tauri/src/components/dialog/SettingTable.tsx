import { useState } from "react";
import { AddButton, CopyButton, DeleteButton, EditButton } from "../element/ButtonIcon";

interface SttingTableProps<T> {
    caption: string;
    settings: T[];
    setSettings: (convertFunction: (currentSettings: T[]) => T[]) => void;
    addSettings: (current: T[], newSettings: T) => T[];
    updateSettings: (current: T[], beforeSettings: T, newSettings: T) => T[];
    deleteSettings: (current: T[], targetSettings: T) => T[];
    renderSetting: (setting: T) => React.ReactNode;
    SettingDialogComponent: React.ComponentType<{
        setting: T;
        handleDialogClose: () => void;
        handleCommit: (newSettings: T) => void;
    }>;
    newSetting: () => T;
    getKey: (setting: T) => string | number;
}

export default function SettingTable<T>({
    caption,
    settings,
    setSettings,
    addSettings,
    updateSettings,
    deleteSettings,
    renderSetting,
    SettingDialogComponent,
    newSetting,
    getKey,
}: SttingTableProps<T>) {
    const defaultState = { setting: newSetting(), action: "" };
    const [selectSettings, setSelectSettings] = useState(defaultState);

    return (
        <>
            {selectSettings.action && (
                <SettingDialogComponent
                    setting={selectSettings.setting}
                    handleDialogClose={() => setSelectSettings(defaultState)}
                    handleCommit={(newSettings: T) => {
                        setSettings((cur) => {
                            if (selectSettings.action === "add") {
                                return addSettings(cur, newSettings);
                            }
                            return updateSettings(cur, selectSettings.setting, newSettings);
                        });
                        setSelectSettings(defaultState);
                    }}
                />
            )}
            <table className="table-fixed">
                <caption className="caption-top">{caption}</caption>
                <thead className="text-xs text-gray-700 uppercase bg-gray-50">
                    <tr>
                        <th scope="col" className="px-6 py-3 border-4">
                            Target
                        </th>
                        <th scope="col" className="px-6 py-3 border-4">
                            Action
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {settings?.map((setting) => (
                        <tr key={getKey(setting)}>
                            <th scope="row" className="px-6 min-w-80 max-w-80 text-left text-sm text-gray-900 border-4">
                                {renderSetting(setting)}
                            </th>
                            <td className="px-6 border-4">
                                <div className="flex">
                                    <EditButton handleClick={() => setSelectSettings({ setting, action: "update" })} />
                                    <DeleteButton handleClick={() => setSettings((current) => deleteSettings(current, setting))} />
                                    <CopyButton handleClick={() => setSelectSettings({ setting, action: "add" })} />
                                </div>
                            </td>
                        </tr>
                    ))}
                    <tr>
                        <th scope="row" className="px-6 py-4">
                            <AddButton handleClick={() => setSelectSettings({ setting: newSetting(), action: "add" })} />
                        </th>
                        <td className="px-6 py-4" />
                    </tr>
                </tbody>
            </table>
        </>
    );
}