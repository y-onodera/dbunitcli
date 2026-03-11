import type { Dispatch, SetStateAction } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
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
	const environment = useEnviroment();
	return async (src: string, regTableInclude: string, regTableExclude: string, recursive: string, regInclude: string, regExclude: string, extension: string): Promise<string[]> => {
		if (!src) {
			return [];
		}
		const fetchParams = {
			endpoint: `${environment.apiUrl}xlsx-schema/sheets`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ src, regTableInclude, regTableExclude, recursive: recursive === "true", regInclude, regExclude, extension }),
			},
		};
		return fetchData(fetchParams)
			.then((r) => r.json())
			.catch(() => []);
	};
};
