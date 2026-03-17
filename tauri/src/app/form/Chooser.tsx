import { core } from "@tauri-apps/api";
import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	DirectoryButton,
	FileButton,
	OpenButton,
} from "../../components/element/ButtonIcon";
import { useEnviroment } from "../../context/EnviromentProvider";
import { useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import type { Attribute } from "../../model/CommandParam";
import { isSqlRelatedType } from "../../model/QueryDatasource";
import type { WorkspaceContext } from "../../model/WorkspaceResources";
import { fetchData } from "../../utils/fetchUtils";
import type { FileProp } from "./FormElementProp";

async function resolvePathViaSidecar(
	path: string,
	defaultPath: string,
	srcType: string | undefined,
	apiUrl: string,
): Promise<string> {
	try {
		const fetchParams = {
			endpoint: `${apiUrl}workspace/resolve-path`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ path, defaultPath, srcType }),
			},
		};
		const response = await fetchData(fetchParams);
		return await response.text();
	} catch (e) {
		console.error("resolvePathViaSidecar failed, falling back to frontend:", e);
		return "";
	}
}

async function resolveAbsolutePath(
	path: string,
	context: WorkspaceContext,
	attribute: Attribute,
	srcType: string | undefined,
	apiUrl: string,
): Promise<string> {
	const sidecarResult = await resolvePathViaSidecar(
		path,
		attribute.defaultPath,
		srcType,
		apiUrl,
	);
	if (sidecarResult) {
		return sidecarResult;
	}
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
	const environment = useEnviroment();
	const handleFileChooserClick = () => {
		const basePath = getPath(context, prop.element.attribute, prop.srcType);
		resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
			prop.srcType,
			environment.apiUrl,
		).then((defaultPath) =>
			open({ defaultPath }).then((files) => {
				if (files) {
					prop.setPath((files as string).replace(basePath + sep(), ""));
					prop.onSelect?.();
				}
			}),
		);
	};
	return <FileButton handleClick={handleFileChooserClick} />;
}
export function DirectoryChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const environment = useEnviroment();
	const handleDirectoryChooserClick = () => {
		const basePath = getPath(context, prop.element.attribute, prop.srcType);
		resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
			prop.srcType,
			environment.apiUrl,
		).then((defaultPath) =>
			open({ defaultPath, directory: true }).then((files) => {
				if (files) {
					prop.setPath((files as string).replace(basePath + sep(), ""));
					prop.onSelect?.();
				}
			}),
		);
	};
	return <DirectoryButton handleClick={handleDirectoryChooserClick} />;
}

export function OpenInOS(prop: FileProp) {
	const context = useWorkspaceContext();
	const environment = useEnviroment();
	const handleOpen = async () => {
		if (!prop.path) {
			return;
		}
		const absolutePath = await resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
			prop.srcType,
			environment.apiUrl,
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
