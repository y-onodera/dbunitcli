import { core } from "@tauri-apps/api";
import { sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import {
	DirectoryButton,
	FileButton,
	OpenButton,
} from "../../../../components/element/ButtonIcon";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import { useResolveAbsolutePath } from "../../../../hooks/useWorkspaceResources";
import type { FileProp } from "./FormElementProp";

function useChooserHandler(prop: FileProp, directory?: boolean) {
	const context = useWorkspaceContext();
	const resolveAbsolutePath = useResolveAbsolutePath();
	return () => {
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		const workspacePath = context.workspace;
		resolveAbsolutePath(prop.path, prop.element.attribute).then((defaultPath) =>
			open({ defaultPath, directory }).then((files) => {
				if (files) {
					const fullPath = files as string;
					const primaryPrefix = basePath + sep();
					const secondaryPrefix = workspacePath + sep();
					let relative: string;
					if (fullPath.startsWith(primaryPrefix)) {
						relative = fullPath.slice(primaryPrefix.length);
					} else if (
						workspacePath !== "" &&
						workspacePath !== basePath &&
						fullPath.startsWith(secondaryPrefix)
					) {
						relative = fullPath.slice(secondaryPrefix.length);
					} else {
						relative = fullPath;
					}
					prop.setPath(relative);
					prop.onSelect?.();
				}
			}),
		);
	};
}

export function FileChooser(prop: FileProp) {
	return <FileButton handleClick={useChooserHandler(prop)} />;
}

export function DirectoryChooser(prop: FileProp) {
	return <DirectoryButton handleClick={useChooserHandler(prop, true)} />;
}

export function OpenInOS(prop: FileProp) {
	const resolveAbsolutePath = useResolveAbsolutePath();
	const handleOpen = async () => {
		if (!prop.path) {
			return;
		}
		const absolutePath = await resolveAbsolutePath(
			prop.path,
			prop.element.attribute,
		);
		await core.invoke("open_directory", { path: absolutePath });
	};
	return <OpenButton handleClick={handleOpen} />;
}
