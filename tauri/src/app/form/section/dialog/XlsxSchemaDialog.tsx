import { useState } from "react";
import { SettingDialog, SettingTable } from "../../../../components/dialog";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import {
	useSaveXlsxSchema,
	XLSX_SCHEMA_PARSER,
} from "../../../../hooks/useXlsxSchema";
import { srcTypeOf, withValues } from "../../../../model/CommandOption";
import type { CellSetting, RowSetting } from "../../../../model/XlsxSchema";
import {
	createCellSetting,
	createRowSetting,
	XlsxSchema,
} from "../../../../model/XlsxSchema";
import { SaveAndPreviewButton } from "./DatasetTableNamesPreviewButton";
import type { LoadedResourceDialogProps } from "./ResourceEditButton";
import { ResourceLoader } from "./ResourceLoader";
import XlsxCellSettingDialog from "./XlsxCellSettingDialog";
import XlsxRowSettingDialog from "./XlsxRowSettingDialog";

export default function XlsxSchemaDialog(props: {
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	return (
		<ResourceLoader
			kind="xlsxSchema"
			fileName={props.fileName}
			parser={XLSX_SCHEMA_PARSER}
		>
			{(schema, location) => (
				<XlsxSchemaEditDialog
					schema={schema}
					location={location}
					fileName={props.fileName}
					handleDialogClose={props.handleDialogClose}
					handleSave={props.handleSave}
				/>
			)}
		</ResourceLoader>
	);
}
export function XlsxSchemaEditDialog(
	props: LoadedResourceDialogProps & {
		schema: XlsxSchema;
	},
) {
	const saveSchema = useSaveXlsxSchema();
	const [xlsxSchema, setXlsxSchema] = useState(props.schema);
	const datasetSrcInfo = useDatasetSrcInfo();

	return (
		<SettingDialog
			location={props.location}
			title="Xlsx Schema"
			helpCommand="dialogs/xlsx-schema"
			helpLabel="Xlsx Schema"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={(fileName, overwrite) =>
				saveSchema(fileName, xlsxSchema, overwrite)
			}
			handleSave={props.handleSave}
			footerActions={(saveThen) =>
				// 保存したファイルをxlsxSchemaに適用してプレビューする（それ以外のsrc要素はフォームのまま）
				srcTypeOf(datasetSrcInfo) && (
					<SaveAndPreviewButton
						saveThen={saveThen}
						toSrcInfo={(path) =>
							withValues(datasetSrcInfo, { xlsxSchema: path })
						}
					/>
				)
			}
		>
			<SettingTable<RowSetting>
				caption="Rows Mapping Tables"
				settings={xlsxSchema.rows}
				setSettings={(convertRows) =>
					setXlsxSchema((cur) => {
						const updatedRows = convertRows(cur.rows);
						return new XlsxSchema(updatedRows, cur.cells);
					})
				}
				SettingDialogComponent={XlsxRowSettingDialog}
				newSetting={createRowSetting}
			/>
			<SettingTable<CellSetting>
				caption="Random Cell Mapping Tables"
				settings={xlsxSchema.cells}
				setSettings={(convertCells) =>
					setXlsxSchema((cur) => {
						const updatedCells = convertCells(cur.cells);
						return new XlsxSchema(cur.rows, updatedCells);
					})
				}
				SettingDialogComponent={XlsxCellSettingDialog}
				newSetting={createCellSetting}
			/>
		</SettingDialog>
	);
}
