import { type ReactNode, useState } from "react";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import DatasetTableNamesPreviewButton from "../../settings/DatasetTableNamesPreviewButton";
import DatasetSettingDropDownMenu from "./DatasetSettingDropDownMenu";
import FileDropDownMenu from "./FileDropDownMenu";
import type { Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";
import SqlSrcDropDownMenu from "./SqlSrcDropDownMenu";
import TemplateDropDownMenu from "./TemplateDropDownMenu";
import XlsxSchemaDropDownMenu from "./XlsxSchemaDropDownMenu";

export default function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	const { element, srcType } = prop;
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();
	const isSqlSrc = element.name === "src" && isSqlRelatedType(srcType ?? "");
	let resourceFiles: string[] = [];
	if (isSqlSrc) {
		resourceFiles = settings.querys(srcType);
	} else if (element.name === "setting") {
		resourceFiles = settings.datasetSettings;
	} else if (element.name === "xlsxSchema") {
		resourceFiles = settings.xlsxSchemas;
	} else if (element.name === "templateGroup") {
		resourceFiles = settings.templateFiles;
	}
	const showDatalist =
		element.name === "setting" ||
		element.name === "xlsxSchema" ||
		isSqlSrc ||
		element.name === "templateGroup";
	const isFileOrDir =
		element.attribute.type.includes("FILE") ||
		element.attribute.type.includes("DIR");
	const showDropDownMenu = isFileOrDir || showDatalist;
	const isValueInDatalist = resourceFiles.includes(path);

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		if (datasetSrcInfo) {
			const fieldName = element.name === "src" ? "srcPath" : element.name;
			if (fieldName in datasetSrcInfo) {
				setDatasetSrcInfo({
					...datasetSrcInfo,
					[fieldName]: newValue,
				} as DatasetSrcInfo);
			}
		}
	};

	let dropDownMenu: ReactNode = null;
	if (element.name === "setting") {
		dropDownMenu = (
			<DatasetSettingDropDownMenu
				path={path}
				setPath={setPath}
				prefix={prop.prefix}
				element={element}
				srcType={srcType}
				isValueInDatalist={isValueInDatalist}
				hideDatasetSettingEdit={prop.hideDatasetSettingEdit}
			/>
		);
	} else if (element.name === "xlsxSchema") {
		dropDownMenu = (
			<XlsxSchemaDropDownMenu
				path={path}
				setPath={setPath}
				prefix={prop.prefix}
				element={element}
				srcType={srcType}
				isValueInDatalist={isValueInDatalist}
			/>
		);
	} else if (element.name === "src") {
		dropDownMenu = (
			<SqlSrcDropDownMenu
				path={path}
				setPath={setPath}
				prefix={prop.prefix}
				element={element}
				srcType={srcType}
				isValueInDatalist={isValueInDatalist}
			/>
		);
	} else if (element.name === "templateGroup") {
		dropDownMenu = (
			<TemplateDropDownMenu
				path={path}
				setPath={setPath}
				prefix={prop.prefix}
				element={element}
				srcType={srcType}
				isValueInDatalist={isValueInDatalist}
			/>
		);
	} else if (isFileOrDir) {
		dropDownMenu = (
			<FileDropDownMenu
				path={path}
				setPath={setPath}
				prefix={prop.prefix}
				element={element}
				srcType={srcType}
			/>
		);
	}

	return (
		<>
			<div>
				<InputLabel
					text={getName(prop.prefix, prop.element.name)}
					id={getId(prop.prefix, prop.element.name)}
					required={prop.element.attribute.required}
					hidden={prop.hidden}
				/>
				<div className="flex">
					<div
						className={`flex-1${!showDropDownMenu ? " mr-36" : ""}`}
					>
						<ControllTextBox
							name={getName(prop.prefix, prop.element.name)}
							id={getId(prop.prefix, prop.element.name)}
							list={
								showDatalist
									? `${getId(prop.prefix, prop.element.name)}_list`
									: undefined
							}
							hidden={prop.hidden}
							required={prop.element.attribute.required}
							value={path}
							handleChange={handleChange}
						/>
						{showDatalist && !prop.hidden && (
							<ResourceDatalist
								id={getId(prop.prefix, prop.element.name)}
								resources={resourceFiles}
							/>
						)}
					</div>
					{!prop.hidden && dropDownMenu}
				</div>
			</div>
			{element.name === "setting" &&
				datasetSrcInfo?.srcType &&
				!prop.hideDatasetSettingEdit && (
					<div className="mt-2 flex items-center gap-3">
						<DatasetTableNamesPreviewButton title="Preview Before Settings" />
						{path && (
							<DatasetTableNamesPreviewButton
								title="Preview Aply Settings"
								setting={path}
							/>
						)}
					</div>
				)}
		</>
	);
}
