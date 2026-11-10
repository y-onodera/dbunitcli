import { sep } from "@tauri-apps/api/path";
import { open } from "@tauri-apps/plugin-dialog";
import { useEffect, useRef } from "react";
import {
	DirectoryButton,
	FileButton,
	OpenButton,
} from "../../../../components/element/ButtonIcon";
import { useWorkspaceContext } from "../../../../context/WorkspaceResourcesProvider";
import { useResolveAbsolutePath } from "../../../../hooks/useWorkspaceResources";
import { reportError } from "../../../../utils/fetchUtils";
import { openDirectory } from "../../../../utils/openDirectory";
import type { FileProp } from "./FormElementProp";

function useChooserHandler(prop: FileProp, directory?: boolean) {
	const context = useWorkspaceContext();
	const resolveAbsolutePath = useResolveAbsolutePath();
	const abortControllerRef = useRef<AbortController | null>(null);
	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		return () => {
			controller.abort();
		};
	}, []);
	return () => {
		const basePath = context.getPath(prop.element.attribute.defaultPath);
		resolveAbsolutePath(prop.path, prop.element.attribute)
			.then((defaultPath) => open({ defaultPath, directory }))
			.then((files) => {
				if (files && !abortControllerRef.current?.signal.aborted) {
					const fullPath = files as string;
					prop.setPath(
						relativeTo(fullPath, basePath) ??
							relativeTo(fullPath, context.workspace) ??
							fullPath,
					);
					prop.onSelect?.();
				}
			})
			.catch((ex) => reportError("Failed to choose a file", ex));
	};
}

// baseの配下ならbaseからの相対パス、そうでなければundefined。baseが空なら相対化しない
function relativeTo(fullPath: string, base: string): string | undefined {
	const prefix = base + sep();
	if (base === "" || !fullPath.startsWith(prefix)) {
		return undefined;
	}
	return fullPath.slice(prefix.length);
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
		await openDirectory(absolutePath);
	};
	return <OpenButton handleClick={handleOpen} />;
}
