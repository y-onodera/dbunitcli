import { core } from "@tauri-apps/api";
import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	DirectoryButton,
	FileButton,
	OpenButton,
} from "../../components/element/ButtonIcon";
import { useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import type { Attribute } from "../../model/CommandParam";
import { isSqlRelatedType } from "../../model/QueryDatasource";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import type { FileProp } from "./FormElementProp";

async function resolveAbsolutePath(
	path: string,
	context: WorkspaceContext,
	attribute: Attribute,
	srcType: string | undefined,
): Promise<string> {
	if (await isAbsolute(path)) {
		return path;
	}
	const basePath = getPath(context, attribute, srcType);
	if (path) {
		return basePath + sep() + path;
	}
	return basePath;
}

export function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleFileChooserClick = () => {
		const basePath = getPath(context, prop.element.attribute, prop.srcType);
		resolveAbsolutePath(prop.path, context, prop.element.attribute, prop.srcType).then(
			(defaultPath) =>
				open({ defaultPath }).then((files) => {
					if (files) {
						prop.setPath(
							(files as string).replace(basePath + sep(), ""),
						);
						prop.onSelect?.();
					}
				}),
		);
	};
	return <FileButton handleClick={handleFileChooserClick} />;
}
export function DirectoryChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleDirectoryChooserClick = () => {
		const basePath = getPath(context, prop.element.attribute, prop.srcType);
		resolveAbsolutePath(prop.path, context, prop.element.attribute, prop.srcType).then(
			(defaultPath) =>
				open({ defaultPath, directory: true }).then((files) => {
					if (files) {
						prop.setPath(
							(files as string).replace(basePath + sep(), ""),
						);
						prop.onSelect?.();
					}
				}),
		);
	};
	return <DirectoryButton handleClick={handleDirectoryChooserClick} />;
}

export function OpenInOS(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleOpen = async () => {
		if (!prop.path) {
			return;
		}
		const absolutePath = await resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
			prop.srcType,
		);
		await core.invoke("open_directory", { path: absolutePath });
	};
	return <OpenButton handleClick={handleOpen} title="Open in Explorer" />;
}

function getPath(
	context: WorkspaceContext,
	attribute: Attribute,
	srcType: string | undefined,
): string {
	if (attribute.defaultPath === "DATASET") {
		if (isSqlRelatedType(srcType ?? "")) {
			return context.datasetBase + sep() + srcType;
		}
		return context.datasetBase;
	}
	if (attribute.defaultPath === "RESULT") {
		return context.resultBase;
	}
	if (attribute.defaultPath === "SETTING") {
		return context.settingBase;
	}
	if (attribute.defaultPath === "TEMPLATE") {
		return context.templateBase;
	}
	if (attribute.defaultPath === "PARAMETERIZE_TEMPLATE") {
		return context.parameterizeTemplateBase;
	}
	if (attribute.defaultPath === "JDBC") {
		return context.jdbcBase;
	}
	if (attribute.defaultPath === "XLSX_SCHEMA") {
		return context.xlsxSchemaBase;
	}
	return context.workspace;
}
