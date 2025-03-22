import ResourceFileDialog from "../../components/dialog/ResourceFileDialog";
import SettingTable from "../../components/dialog/SettingTable";
import { useEnviroment } from "../../context/EnviromentProvider";
import { saveXlsxSchema, useSetXlsxSchema, useXlsxSchema } from "../../context/XlsxSchemaProvider";
import { type CellSetting, type RowSetting, XlsxSchema, createCellSetting, createRowSetting } from "../../model/XlsxSchema";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
    fileName: string;
    setFileName: (fileName: string) => void;
    handleDialogClose: () => void;
    handleSave: (path: string) => void;
}) {
    const environment = useEnviroment();
    const xlsxSchema = useXlsxSchema();
    const setXlsxSchema = useSetXlsxSchema();

    return (
        <ResourceFileDialog
            handleDialogClose={props.handleDialogClose}
            fileName={props.fileName}
            setFileName={props.setFileName}
            handleSave={(fileName) => {
                saveXlsxSchema(environment.apiUrl, fileName, xlsxSchema);
                props.handleSave(fileName);
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
