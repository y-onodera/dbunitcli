import { Fragment, useState } from "react";
import { ExpandButton } from "../../components/element/ButtonIcon";
import DropDownMenu from "../../components/element/DropDownMenu";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
	SelectBox,
} from "../../components/element/Input";
import {
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../context/DatasetSrcInfoProvider";
import { useJdbcConnectionState } from "../../context/JdbcConnectionProvider";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type {
	CommandParam,
	CommandParams,
	DatasetSrcInfo,
	SrcInfo,
} from "../../model/CommandParam";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../model/QueryDatasource";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../settings/DatasetSettingEditButton";
import DatasetTableNamesPreviewButton from "../settings/DatasetTableNamesPreviewButton";
import JdbcTableSelectorButton from "../settings/JdbcTableSelectorButton";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../settings/SqlEditorButton";
import TemplateEditButton, {
	RemoveTemplateButton,
} from "../settings/TemplateEditButton";
import XlsxSchemaEditButton, {
	RemoveXlsxSchemaButton,
} from "../settings/XlsxSchemaEditButton";
import { DirectoryChooser, FileChooser, OpenInOS } from "./Chooser";
import type { FileProp, Prop, SelectProp } from "./FormElementProp";
import JdbcFormSection, { JDBC_FIELD_NAMES } from "./JdbcFormSection";

export function buildSrcInfo(elements: CommandParam[]): SrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		srcPath: find("src")?.value ?? "",
		regTableInclude: find("regTableInclude")?.value ?? "",
		regTableExclude: find("regTableExclude")?.value ?? "",
		recursive: find("recursive")?.value ?? "",
		regInclude: find("regInclude")?.value ?? "",
		regExclude: find("regExclude")?.value ?? "",
		extension: find("extension")?.value ?? "",
	};
}

export function buildDatasetSrcInfo(elements: CommandParam[]): DatasetSrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		...buildSrcInfo(elements),
		srcType: find("srcType")?.value ?? "",
		xlsxSchema: find("xlsxSchema")?.value ?? "",
		fixedLength: find("fixedLength")?.value ?? "",
		regHeaderSplit: find("regHeaderSplit")?.value ?? "",
		regDataSplit: find("regDataSplit")?.value ?? "",
		encoding: find("encoding")?.value ?? "",
		delimiter: find("delimiter")?.value ?? "",
		ignoreQuoted: find("ignoreQuoted")?.value === "true",
		headerName: find("headerName")?.value ?? "",
		startRow: find("startRow")?.value ?? "",
		addFileInfo: find("addFileInfo")?.value === "true",
	};
}

export default function CommandFormElements(
	prop: {
		handleTypeSelect: (selected: string) => Promise<void>;
	} & CommandParams,
) {
	const [showOptional, setShowOptional] = useState(false);
	const srcTypeElement = prop.elements.find(
		(element) => element.name === "srcType",
	);
	const srcType = srcTypeElement ? srcTypeElement.value : "";
	const toggleOptional = () => setShowOptional(!showOptional);

	const isJdbcFieldName = (name: string) =>
		JDBC_FIELD_NAMES.includes(name as (typeof JDBC_FIELD_NAMES)[number]);

	const jdbcElements = prop.elements.filter((e) => isJdbcFieldName(e.name));

	const firstOptionalNonJdbcElementName = prop.optionCaption
		? prop.elements.find(
				(e) => !isJdbcFieldName(e.name) && prop.optional?.(e.name),
			)?.name
		: undefined;

	return (
		<>
			{prop.elements.map((element) => {
				if (isJdbcFieldName(element.name)) {
					return null;
				}
				const showExpandButton =
					element.name === firstOptionalNonJdbcElementName;
				if (element.attribute.type === "FLG") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Check
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Select
								handleTypeSelect={prop.handleTypeSelect}
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				return (
					<Fragment key={prop.name + prop.prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							</div>
						)}
						<Text
							prefix={prop.prefix}
							element={element}
							hidden={prop.optional?.(element.name) && !showOptional}
							srcType={element.name === "src" ? srcType : undefined}
						/>
					</Fragment>
				);
			})}
			{jdbcElements.length > 0 && (
				<JdbcFormSection prefix={prop.prefix} elements={jdbcElements} />
			)}
		</>
	);
}
function Text(prop: Prop) {
	const [path, setPath] = useState(prop.element.value);
	const { element, srcType } = prop;
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();
	const settings = useResourcesSettings();
	let resourceFiles: string[] = [];
	if (element.name === "src" && isSqlRelatedType(srcType ?? "")) {
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
		(element.name === "src" && isSqlRelatedType(srcType ?? "")) ||
		element.name === "templateGroup";
	const showDopDownMenu =
		element.attribute.type.includes("FILE") ||
		element.attribute.type.includes("DIR") ||
		showDatalist;
	const isValueInDatalist = resourceFiles?.includes(path) || false;

	const handleChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
		const newValue = ev.target.value;
		setPath(newValue);
		if (datasetSrcInfo) {
			const fieldName =
				element.name === "src" ? "srcPath" : element.name;
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
						className={`flex-1${!showDopDownMenu && !isValueInDatalist ? " mr-36" : ""}`}
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
					<div className="flex">
						{showDopDownMenu && !prop.hidden && (
							<TextDropDownMenu
								prefix={prop.prefix}
								element={prop.element}
								path={path}
								setPath={setPath}
								hidden={prop.hidden}
								srcType={srcType}
								isValueInDatalist={isValueInDatalist}
							/>
						)}
					</div>
				</div>
			</div>
			{element.name === "setting" && datasetSrcInfo?.srcType && (
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
}: FileProp & {
	srcType?: string;
	isValueInDatalist?: boolean;
}) {
	const { connectionOk } = useJdbcConnectionState();

	return (
		<DropDownMenu>
			{(closeMenu) => (
				<>
					{element.name === "setting" && !hidden && (
						<li>
							<DatasetSettingEditButton
								path={path}
								setPath={setPath}
							/>
						</li>
					)}
					{element.name === "xlsxSchema" && !hidden && (
						<li>
							<XlsxSchemaEditButton
								path={path}
								setPath={setPath}
							/>
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
function Check(prop: Prop) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleOnChange = (checked: boolean) => {
		if (datasetSrcInfo && prop.element.name in datasetSrcInfo) {
			setDatasetSrcInfo({
				...datasetSrcInfo,
				[prop.element.name]: checked,
			} as DatasetSrcInfo);
		}
	};

	return (
		<div>
			<InputLabel
				text={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={false}
				hidden={prop.hidden}
			/>
			<CheckBox
				name={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				hidden={prop.hidden}
				defaultValue={prop.element.value}
				handleOnChange={handleOnChange}
			/>
		</div>
	);
}
function Select(prop: SelectProp) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const setDatasetSrcInfo = useSetDatasetSrcInfo();

	const handleTypeSelect = async (selected: string) => {
		await prop.handleTypeSelect(selected);
		if (prop.element.name === "srcType" && datasetSrcInfo) {
			setDatasetSrcInfo({ ...datasetSrcInfo, srcType: selected });
		}
	};

	return (
		<div>
			<InputLabel
				text={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={prop.element.attribute.required}
				hidden={prop.hidden}
			/>
			<SelectBox
				name={
					prop.prefix
						? `-${prop.prefix}.${prop.element.name}`
						: `-${prop.element.name}`
				}
				id={`${prop.prefix}_${prop.element.name}`}
				required={true}
				hidden={prop.hidden}
				handleOnChange={handleTypeSelect}
				defaultValue={prop.element.value}
			>
				{prop.element.attribute.selectOption.map((value) => {
					return (
						<option key={prop.prefix + prop.element.name + value} value={value}>
							{value}
						</option>
					);
				})}
			</SelectBox>
		</div>
	);
}
function getId(prefix: string, name: string): string {
	return prefix ? `${prefix}_${name}` : `${name}`;
}
function getName(prefix: string, name: string): string {
	return prefix ? `-${prefix}.${name}` : `-${name}`;
}
