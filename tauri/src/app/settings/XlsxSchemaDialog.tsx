import { useEffect, useState } from "react";
import ResourceFileDialog from "../../components/dialog/ResourceFileDialog";
import SettingTable from "../../components/dialog/SettingTable";
import { useLoadXlsxSchema, useSaveXlsxSchema } from "../../context/XlsxSchemaProvider";
import { type CellSetting, type RowSetting, XlsxSchema, createCellSetting, createRowSetting } from "../../model/XlsxSchema";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
    fileName: string;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
}) {
    const loadXlsxSchema = useLoadXlsxSchema();
    const [xlsxSchema, setXlsxSchema] = useState(XlsxSchema.create());
    const saveSchema = useSaveXlsxSchema();
    // biome-ignore lint/correctness/useExhaustiveDependencies: <explanation>
    useEffect(() => {
        loadXlsxSchema(props.fileName)
            .then((res) => {
                setXlsxSchema(res);
            })
            .catch((ex) => {
                alert(ex);
            });
    }, [props.fileName]);
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
                SettingDialogComponent={XlsxRowSettingDialog}
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
                SettingDialogComponent={XlsxCellSettingDialog}
                newSetting={createCellSetting}
                getKey={(setting) => setting.displayName()}
            />
        </ResourceFileDialog>
    );
}
