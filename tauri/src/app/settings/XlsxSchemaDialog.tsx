import { Suspense, use, useState } from "react";
import { ResourceFileDialog, SettingTable } from "../../components/dialog";
import { useLoadXlsxSchema, useSaveXlsxSchema } from "../../hooks/useXlsxSchema";
import { type CellSetting, type RowSetting, XlsxSchema, createCellSetting, createRowSetting } from "../../model/XlsxSchema";
import type { SrcInfo } from "../form/FormElementProp";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
    fileName: string;
    srcInfo?: SrcInfo;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
}) {
    const loadXlsxSchema = useLoadXlsxSchema();
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <Dialog
                promise={loadXlsxSchema(props.fileName)}
                fileName={props.fileName}
                srcInfo={props.srcInfo}
                handleDialogClose={props.handleDialogClose}
                handleSave={props.handleSave}
            />
        </Suspense>
    );
}
function Dialog(props: {
    promise: Promise<XlsxSchema>;
    fileName: string;
    srcInfo?: SrcInfo;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
}) {
    const saveSchema = useSaveXlsxSchema();
    const xlsxSchemaData = use(props.promise);
    const [xlsxSchema, setXlsxSchema] = useState(xlsxSchemaData);

    return (
        <ResourceFileDialog
            handleDialogClose={props.handleDialogClose}
            fileName={props.fileName}
            handleSave={async (fileName) => {
                const result = await saveSchema(fileName, xlsxSchema);
                if (result === 'success') {
                    props.handleSave(fileName);
                }
            }}
        >
            <SettingTable<RowSetting>
                caption="Rows Mapping Tables"
                settings={xlsxSchema.rows}
                setSettings={(convertRows) => setXlsxSchema((cur) => {
                    const updatedRows = convertRows(cur.rows);
                    return new XlsxSchema(updatedRows, cur.cells);
                })}
                addSettings={(current, settings) => [...current, settings]}
                updateSettings={(current, before, after) => current.map((row) => (row === before ? after : row))}
                deleteSettings={(current, settings) => current.filter((row) => row !== settings)}
                renderSetting={(setting) => setting.displayName()}
                SettingDialogComponent={({ setting, handleDialogClose, handleCommit }) => (
                    <XlsxRowSettingDialog
                        setting={setting}
                        handleDialogClose={handleDialogClose}
                        handleCommit={handleCommit}
                        srcInfo={props.srcInfo}
                    />
                )}
                newSetting={createRowSetting}
                getKey={(setting) => setting.displayName()}
            />
            <SettingTable<CellSetting>
                caption="Random Cell Mapping Tables"
                settings={xlsxSchema.cells}
                setSettings={(convertCells) => setXlsxSchema((cur) => {
                    const updatedCells = convertCells(cur.cells);
                    return new XlsxSchema(cur.rows, updatedCells);
                })}
                addSettings={(current, settings) => [...current, settings]}
                updateSettings={(current, before, after) => current.map((cell) => (cell === before ? after : cell))}
                deleteSettings={(current, settings) => current.filter((cell) => cell !== settings)}
                renderSetting={(setting) => setting.displayName()}
                SettingDialogComponent={({ setting, handleDialogClose, handleCommit }) => (
                    <XlsxCellSettingDialog
                        setting={setting}
                        handleDialogClose={handleDialogClose}
                        handleCommit={handleCommit}
                        srcInfo={props.srcInfo}
                    />
                )}
                newSetting={createCellSetting}
                getKey={(setting) => setting.displayName()}
            />
        </ResourceFileDialog>
    );
}
