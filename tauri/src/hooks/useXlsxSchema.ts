import type { Dispatch, SetStateAction } from "react";
import { useCallback, useEffect, useState } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { SrcInfo } from "../model/CommandParam";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

export const useDeleteXlsxSchema = () => {
	const environment = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string) => {
		return deleteXlsxSchema(environment.apiUrl, name, setResourcesSettings);
	};
};

export const useSaveXlsxSchema = () => {
	const environment = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, input: XlsxSchema) => {
		return saveXlsxSchema(
			environment.apiUrl,
			name,
			input,
			setResourcesSettings,
		);
	};
};

export const useLoadXlsxSchema = () => {
	const environment = useEnviroment();
	return async (name: string) => {
		return loadXlsxSchema(environment.apiUrl, name);
	};
};

async function loadXlsxSchema(
	apiUrl: string,
	name: string,
): Promise<XlsxSchema> {
	if (name === "") {
		return XlsxSchema.create();
	}
	const fetchParams = {
		endpoint: `${apiUrl}xlsx-schema/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((schema: XlsxSchemaBuilder) => XlsxSchema.build(schema))
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return XlsxSchema.create();
		});
}

async function saveXlsxSchema(
	apiUrl: string,
	name: string,
	input: XlsxSchema,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}xlsx-schema/save`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name, input }),
		},
	};

	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((schemas: string[]) => {
			setResourcesSettings((current) => current.with({ xlsxSchemas: schemas }));
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}

async function deleteXlsxSchema(
	apiUrl: string,
	name: string,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}xlsx-schema/delete`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};

	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((schemas: string[]) => {
			setResourcesSettings((current) => current.with({ xlsxSchemas: schemas }));
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}

export const useXlsxSheets = () => {
	const { apiUrl } = useEnviroment();
	return useCallback(
		async (srcInfo: SrcInfo): Promise<string[]> => {
			if (!srcInfo.srcPath) {
				return [];
			}
			const fetchParams = {
				endpoint: `${apiUrl}xlsx-schema/sheets`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({
						src: srcInfo.srcPath,
						regTableInclude: srcInfo.regTableInclude,
						regTableExclude: srcInfo.regTableExclude,
						recursive: srcInfo.recursive === "true",
						regInclude: srcInfo.regInclude,
						regExclude: srcInfo.regExclude,
						extension: srcInfo.extension,
					}),
				},
			};
			return fetchData(fetchParams)
				.then((r) => r.json())
				.catch(() => []);
		},
		[apiUrl],
	);
};

export const useSrcInfoSheets = (srcInfo: SrcInfo): string[] => {
	const [sheetNames, setSheetNames] = useState<string[]>([]);
	const loadSheets = useXlsxSheets();
	const srcPath = srcInfo.srcPath;
	const regTableInclude = srcInfo.regTableInclude;
	const regTableExclude = srcInfo.regTableExclude;
	const recursive = srcInfo.recursive;
	const regInclude = srcInfo.regInclude;
	const regExclude = srcInfo.regExclude;
	const extension = srcInfo.extension;

	useEffect(() => {
		if (!srcPath) {
			return;
		}
		loadSheets({
			srcPath,
			regTableInclude,
			regTableExclude,
			recursive,
			regInclude,
			regExclude,
			extension,
		}).then(setSheetNames);
	}, [
		srcPath,
		regTableInclude,
		regTableExclude,
		recursive,
		regInclude,
		regExclude,
		extension,
		loadSheets,
	]);

	return sheetNames;
};
