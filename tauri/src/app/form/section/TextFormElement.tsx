import { useState } from "react";
import DropDownMenu from "../../../components/element/DropDownMenu";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../../context/DatasetSrcInfoProvider";
import { useJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../../../model/CommandParam";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../../model/QueryDatasource";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../../settings/DatasetSettingEditButton";
import DatasetTableNamesPreviewButton from "../../settings/DatasetTableNamesPreviewButton";
import JdbcTableSelectorButton from "../../settings/JdbcTableSelectorButton";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../../settings/SqlEditorButton";
import TemplateEditButton, {
	RemoveTemplateButton,
} from "../../settings/TemplateEditButton";
import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../../settings/XlsxSchemaEditButton";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp, Prop } from "./FormElementProp";
import { getId, getName } from "./FormElementProp";

export default function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	const { element, srcType } = prop;
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();
	const isSqlSrc =
		element.name === "src" && isSqlRelatedType(srcType ?? "");
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
	const showDropDownMenu =
		element.attribute.type.includes("FILE") ||
		element.attribute.type.includes("DIR") ||
		showDatalist;
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
						className={`flex-1${!showDropDownMenu && !isValueInDatalist ? " mr-36" : ""}`}
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
					{showDropDownMenu && !prop.hidden && (
						<TextDropDownMenu
							prefix={prop.prefix}
							element={prop.element}
							path={path}
							setPath={setPath}
							hidden={prop.hidden}
							srcType={srcType}
							isValueInDatalist={isValueInDatalist}
							hideDatasetSettingEdit={prop.hideDatasetSettingEdit}
						/>
					)}
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

function TextDropDownMenu({
	prefix,
	element,
	path,
	setPath,
	hidden,
	srcType,
	isValueInDatalist,
	hideDatasetSettingEdit,
}: FileProp & {
	srcType?: string;
	isValueInDatalist?: boolean;
	hideDatasetSettingEdit?: boolean;
}) {
	const { connectionOk } = useJdbcConnectionState();

	return (
		<DropDownMenu>
			{(closeMenu) => (
				<>
					{element.name === "setting" && !hidden && !hideDatasetSettingEdit && (
						<li>
							<DatasetSettingEditButton path={path} setPath={setPath} />
						</li>
					)}
					{element.name === "xlsxSchema" && !hidden && (
						<li>
							<XlsxSchemaEditButton path={path} setPath={setPath} />
						</li>
					)}
					{element.name === "src" &&
						!hidden &&
						isSqlRelatedType(srcType ?? "") && (
							<li>
								<SqlEditorButton
									type={srcType as QueryDatasourceType}
									path={path}
									setPath={setPath}
								/>
							</li>
						)}
					{element.name === "src" &&
						!hidden &&
						srcType === "table" &&
						connectionOk && (
							<li>
								<JdbcTableSelectorButton path={path} setPath={setPath} />
							</li>
						)}
					{element.name === "templateGroup" && !hidden && (
						<li>
							<TemplateEditButton path={path} setPath={setPath} />
						</li>
					)}
					{(element.attribute.type.includes("FILE") ||
						element.attribute.type.includes("DIR")) &&
						path &&
						!hidden && (
							<li>
								<OpenInOS
									prefix={prefix}
									element={element}
									srcType={srcType}
									path={path}
									setPath={setPath}
								/>
							</li>
						)}
					{element.name === "templateGroup" && !hidden && isValueInDatalist && (
						<li>
							<RemoveTemplateButton path={path} setPath={setPath} />
						</li>
					)}
					{element.name === "setting" && !hidden && isValueInDatalist && (
						<li>
							<RemoveDatasetSettingButton path={path} setPath={setPath} />
						</li>
					)}
					{element.name === "xlsxSchema" && !hidden && isValueInDatalist && (
						<li>
							<RemoveXlsxSchemaButton path={path} setPath={setPath} />
						</li>
					)}
					{(srcType === "sql" || srcType === "table") &&
						!hidden &&
						isValueInDatalist && (
							<li>
								<RemoveSqlEditorButton
									path={path}
									setPath={setPath}
									type={srcType as QueryDatasourceType}
								/>
							</li>
						)}
					{element.attribute.type.includes("FILE") && (
						<li>
							<FileChooser
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
								onSelect={closeMenu}
							/>
						</li>
					)}
					{element.attribute.type.includes("DIR") && (
						<li>
							<DirectoryChooser
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
								onSelect={closeMenu}
							/>
						</li>
					)}
				</>
			)}
		</DropDownMenu>
	);
}
