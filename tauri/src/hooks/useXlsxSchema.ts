import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
} from "../utils/fetchUtils";
import { useUpdateResourceFiles } from "./useResourceFiles";

export const useSaveXlsxSchema = () => {
	const updateResourceFiles = useUpdateResourceFiles("xlsxSchema");
	return (name: string, input: XlsxSchema) =>
		updateResourceFiles("save", JSON.stringify({ name, input }));
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
		const controller = new AbortController();
		setLoading(true);
		loadXlsxSchema(apiUrl, fileName).then((result) => {
			if (!controller.signal.aborted) {
				setSchema(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
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

type SheetSrc = {
	src: string;
	regTableInclude: string;
	regTableExclude: string;
	recursive: string;
	regInclude: string;
	regExclude: string;
	extension: string;
};

async function fetchSheets(
	apiUrl: string,
	sheetSrc: SheetSrc,
): Promise<string[]> {
	if (!sheetSrc.src) {
		return [];
	}
	const fetchParams = {
		endpoint: `${apiUrl}xlsx-schema/sheets`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				...sheetSrc,
				recursive: sheetSrc.recursive === "true",
			}),
		},
	};
	return fetchData(fetchParams)
		.then((r) => r.json())
		.catch(() => []);
}

export const useSrcInfoSheets = (srcInfo: DatasetSrcInfo): string[] => {
	const [sheetNames, setSheetNames] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const {
		src = "",
		regTableInclude = "",
		regTableExclude = "",
		recursive = "",
		regInclude = "",
		regExclude = "",
		extension = "",
	} = srcInfo.values;

	useEffect(() => {
		if (!src) {
			return;
		}
		const controller = new AbortController();
		fetchSheets(apiUrl, {
			src,
			regTableInclude,
			regTableExclude,
			recursive,
			regInclude,
			regExclude,
			extension,
		}).then((names) => {
			if (!controller.signal.aborted) {
				setSheetNames(names);
			}
		});
		return () => {
			controller.abort();
		};
	}, [
		apiUrl,
		src,
		regTableInclude,
		regTableExclude,
		recursive,
		regInclude,
		regExclude,
		extension,
	]);

	return sheetNames;
};
