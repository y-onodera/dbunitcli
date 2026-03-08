import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	DirectoryButton,
	FileButton,
} from "../../components/element/ButtonIcon";
import { useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import type { Attribute } from "../../model/CommandParam";
import { isSqlRelatedType } from "../../model/QueryDatasource";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import type { FileProp } from "./FormElementProp";
export function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const handleFileChooserClick = () => {
		const getDefaultPath = async (): Promise<string> => {
			return (await isAbsolute(prop.path))
				? prop.path
				: prop.path
					? getPath(context, prop.element.attribute, prop.srcType) +
						sep() +
						prop.path
					: getPath(context, prop.element.attribute, prop.srcType);
		};
		getDefaultPath().then((defaultPath) =>
			open({ defaultPath }).then((files) => {
				if (files) {
					prop.setPath(
						(files as string).replace(
							getPath(context, prop.element.attribute, prop.srcType) + sep(),
							"",
						),
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
		const getDefaultPath = async (): Promise<string> => {
			return (await isAbsolute(prop.path))
				? prop.path
				: prop.path
					? getPath(context, prop.element.attribute, prop.srcType) +
						sep() +
						prop.path
					: getPath(context, prop.element.attribute, prop.srcType);
		};
		getDefaultPath().then((defaultPath) =>
			open({ defaultPath, directory: true }).then((files) => {
				if (files) {
					prop.setPath(
						(files as string).replace(
							getPath(context, prop.element.attribute, prop.srcType) + sep(),
							"",
						),
					);
					prop.onSelect?.();
				}
			}),
		);
	};
	return <DirectoryButton handleClick={handleDirectoryChooserClick} />;
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
