import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { SrcInfo } from "../model/CommandOption";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import { fetchAndUpdate, fetchData, getErrorMessage, handleFetchError, type OperationResult } from "../utils/fetchUtils";

export const useDeleteXlsxSchema = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}xlsx-schema/delete`,
				options: { method: "POST", headers: { "Content-Type": "text/plain" }, body: name },
			},
			(schemas) => setResourcesSettings((current) => current.with({ xlsxSchemas: schemas })),
		);
};

export const useSaveXlsxSchema = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, input: XlsxSchema): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}xlsx-schema/save`,
				options: { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name, input }) },
			},
			(schemas) => setResourcesSettings((current) => current.with({ xlsxSchemas: schemas })),
		);
};

export const useXlsxSchemaData = (
	fileName: string,
): { schema: XlsxSchema; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const [schema, setSchema] = useState(XlsxSchema.create());
	const [loading, setLoading] = useState(fileName !== "");

	useEffect(() => {
		if (!fileName) {
			setSchema(XlsxSchema.create());
			setLoading(false);
			return;
		}
		let isMounted = true;
		setLoading(true);
		loadXlsxSchema(apiUrl, fileName).then((result) => {
			if (isMounted) {
				setSchema(result);
				setLoading(false);
			}
		});
		return () => {
			isMounted = false;
		};
	}, [fileName, apiUrl]);

	return { schema, loading };
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
			handleFetchError(getErrorMessage(ex), fetchParams);
			return XlsxSchema.create();
		});
}

async function fetchSheets(apiUrl: string, srcInfo: SrcInfo): Promise<string[]> {
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
}

export const useSrcInfoSheets = (srcInfo: SrcInfo): string[] => {
	const [sheetNames, setSheetNames] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
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
		let isMounted = true;
		fetchSheets(apiUrl, srcInfo).then((names) => {
			if (isMounted) {
				setSheetNames(names);
			}
		});
		return () => {
			isMounted = false;
		};
	}, [
		apiUrl,
		srcPath,
		regTableInclude,
		regTableExclude,
		recursive,
		regInclude,
		regExclude,
		extension,
	]);

	return sheetNames;
};
