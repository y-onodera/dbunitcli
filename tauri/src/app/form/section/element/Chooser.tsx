import { core } from "@tauri-apps/api";
import { isAbsolute, sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	DirectoryButton,
	FileButton,
	OpenButton,
} from "../../../../components/element/ButtonIcon";
import { useEnviroment } from "../../../../context/EnviromentProvider";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import type { Attribute } from "../../../../model/CommandOption";
import type { WorkspaceContext } from "../../../../model/WorkspaceResources";
import { fetchData } from "../../../../utils/fetchUtils";
import type { FileProp } from "./FormElementProp";

async function resolvePathViaSidecar(
	path: string,
	defaultPath: string,
	apiUrl: string,
): Promise<string> {
	try {
		const fetchParams = {
			endpoint: `${apiUrl}workspace/resolve-path`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ path, defaultPath }),
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
	apiUrl: string,
): Promise<string> {
	const sidecarResult = await resolvePathViaSidecar(
		path,
		attribute.defaultPath,
		apiUrl,
	);
	if (sidecarResult) {
		return sidecarResult;
	}
	if (await isAbsolute(path)) {
		return path;
	}
	const basePath = context.getPath(attribute.defaultPath);
	if (path) {
		return basePath + sep() + path;
	}
	return basePath;
}

export function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const environment = useEnviroment();
	const handleFileChooserClick = () => {
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
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
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		resolveAbsolutePath(
			prop.path,
			context,
			prop.element.attribute,
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
			environment.apiUrl,
		);
		await core.invoke("open_directory", { path: absolutePath });
	};
	return <OpenButton handleClick={handleOpen} />;
}
