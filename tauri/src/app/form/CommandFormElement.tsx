import { Fragment, useEffect, useRef, useState } from "react";
import {
	BlueSettingButton,
	ExpandButton,
} from "../../components/element/ButtonIcon";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
	SelectBox,
} from "../../components/element/Input";
import { useJdbcConnectionState } from "../../context/JdbcConnectionProvider";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { CommandParams } from "../../model/CommandParam";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../model/QueryDatasource";
import DatasetSettingEditButton, {
	RemoveDatasetSettingButton,
} from "../settings/DatasetSettingEditButton";
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
import { DirectoryChooser, FileChooser } from "./Chooser";
import type { FileProp, Prop, SelectProp, SrcInfo } from "./FormElementProp";
import JdbcFormSection, { JDBC_FIELD_NAMES } from "./JdbcFormSection";

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
	const srcElement = prop.elements.find((element) => element.name === "src");
	const regTableIncludeElement = prop.elements.find(
		(element) => element.name === "regTableInclude",
	);
	const regTableExcludeElement = prop.elements.find(
		(element) => element.name === "regTableExclude",
	);
	const recursiveElement = prop.elements.find(
		(element) => element.name === "recursive",
	);
	const regIncludeElement = prop.elements.find(
		(element) => element.name === "regInclude",
	);
	const regExcludeElement = prop.elements.find(
		(element) => element.name === "regExclude",
	);
	const extensionElement = prop.elements.find(
		(element) => element.name === "extension",
	);
	const srcInfo: SrcInfo = {
		srcPath: srcElement?.value ?? "",
		regTableInclude: regTableIncludeElement?.value ?? "",
		regTableExclude: regTableExcludeElement?.value ?? "",
		recursive: recursiveElement?.value ?? "",
		regInclude: regIncludeElement?.value ?? "",
		regExclude: regExcludeElement?.value ?? "",
		extension: extensionElement?.value ?? "",
	};
	const toggleOptional = () => setShowOptional(!showOptional);

	const jdbcElements = prop.elements.filter((e) =>
		JDBC_FIELD_NAMES.includes(e.name as (typeof JDBC_FIELD_NAMES)[number]),
	);

	return (
		<>
			{prop.elements.map((element) => {
				const isJdbcField = JDBC_FIELD_NAMES.includes(
					element.name as (typeof JDBC_FIELD_NAMES)[number],
				);
				if (isJdbcField) {
					return null;
				}
				if (element.attribute.type === "FLG") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{prop.optionCaption?.display(element.name) && (
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
							{prop.optionCaption?.display(element.name) && (
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
						{prop.optionCaption?.display(element.name) && (
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
							srcInfo={element.name === "xlsxSchema" ? srcInfo : undefined}
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
	const { element, srcType, srcInfo } = prop;
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
	return (
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
						handleChange={(ev) => setPath(ev.target.value)}
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
						<DropDownMenu
							prefix={prop.prefix}
							element={prop.element}
							path={path}
							setPath={setPath}
							hidden={prop.hidden}
							srcType={srcType}
							srcInfo={srcInfo}
							isValueInDatalist={isValueInDatalist}
						/>
					)}
				</div>
			</div>
		</div>
	);
}

function DropDownMenu({
	prefix,
	element,
	path,
	setPath,
	hidden,
	srcType,
	srcInfo,
	isValueInDatalist,
}: FileProp & {
	srcType?: string;
	datasources?: string[];
	isValueInDatalist?: boolean;
}) {
	const [showMenu, setShowMenu] = useState(false);
	const { connectionOk } = useJdbcConnectionState();
	const buttonRef = useRef<HTMLDivElement>(null);
	const menuRef = useRef<HTMLDivElement>(null);
	const [menuPosition, setMenuPosition] = useState<"right" | "left">("right");
	useEffect(() => {
		if (showMenu && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const viewportWidth = window.innerWidth;
			const menuWidth = 96;

			if (rect.right + menuWidth > viewportWidth) {
				setMenuPosition("left");
			} else {
				setMenuPosition("right");
			}
		}
		function handleClickOutside(event: MouseEvent) {
			if (
				menuRef.current &&
				!menuRef.current.contains(event.target as Node) &&
				buttonRef.current &&
				!buttonRef.current.contains(event.target as Node)
			) {
				setShowMenu(false);
			}
		}
		if (showMenu) {
			document.addEventListener("mousedown", handleClickOutside);
			return () => {
				document.removeEventListener("mousedown", handleClickOutside);
			};
		}
	}, [showMenu]);

	return (
		<div className="relative" ref={buttonRef}>
			<BlueSettingButton handleClick={() => setShowMenu(!showMenu)} />
			{showMenu && (
				<div
					ref={menuRef}
					className="absolute z-50 p-4 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
					style={{
						...(menuPosition === "right"
							? { left: "100%", top: 0 }
							: { right: "100%", top: 0 }),
					}}
				>
					<ul className="space-y-4">
						{element.name === "setting" && !hidden && (
							<li>
								<DatasetSettingEditButton path={path} setPath={setPath} />
							</li>
						)}
						{element.name === "xlsxSchema" && !hidden && (
							<li>
								<XlsxSchemaEditButton
									path={path}
									setPath={setPath}
									srcInfo={srcInfo}
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
									onSelect={() => setShowMenu(false)}
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
									onSelect={() => setShowMenu(false)}
								/>
							</li>
						)}
					</ul>
				</div>
			)}
		</div>
	);
}
function Check(prop: Prop) {
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
			/>
		</div>
	);
}
function Select(prop: SelectProp) {
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
				handleOnChange={prop.handleTypeSelect}
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
