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

export function FileChooser(prop: FileProp) {
	const context = useWorkspaceContext();
	const resolveAbsolutePath = useResolveAbsolutePath();
	const handleFileChooserClick = () => {
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		resolveAbsolutePath(prop.path, prop.element.attribute).then((defaultPath) =>
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
	const resolveAbsolutePath = useResolveAbsolutePath();
	const handleDirectoryChooserClick = () => {
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		resolveAbsolutePath(prop.path, prop.element.attribute).then((defaultPath) =>
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
