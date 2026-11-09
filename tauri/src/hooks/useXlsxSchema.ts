import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import { fetchData, getErrorMessage, logFetchError } from "../utils/fetchUtils";
import {
	type ResourceParser,
	useResourceLoad,
	useSaveResource,
} from "./useResourceFiles";

export const useSaveXlsxSchema = () => {
	return useSaveResource<XlsxSchema>("xlsxSchema");
};

const XLSX_SCHEMA_PARSER: ResourceParser<XlsxSchema> = {
	empty: () => XlsxSchema.create(),
	parse: (json) => XlsxSchema.build(json as XlsxSchemaBuilder),
};

export const useXlsxSchemaData = (
	fileName: string,
): { schema: XlsxSchema; loading: boolean } => {
	const { data, loading } = useResourceLoad(
		"xlsxSchema",
		fileName,
		XLSX_SCHEMA_PARSER,
	);
	return { schema: data, loading };
};

type SheetSrc = {
	src: string;
	regTableInclude: string;
	regTableExclude: string;
	recursive: string;
	regInclude: string;
	regExclude: string;
	extension: string;
};

// 入力候補なので、取得できなければ候補なしとする（原因はコンソールに残す）
async function fetchSheets(
	apiUrl: string,
	sheetSrc: SheetSrc,
	signal: AbortSignal,
): Promise<string[]> {
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
		signal,
	};
	try {
		const response = await fetchData(fetchParams);
		return (await response.json()) as string[];
	} catch (ex) {
		if (!signal.aborted) {
			logFetchError(getErrorMessage(ex), fetchParams);
		}
		return [];
	}
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
			setSheetNames([]);
			return;
		}
		const controller = new AbortController();
		fetchSheets(
			apiUrl,
			{
				src,
				regTableInclude,
				regTableExclude,
				recursive,
				regInclude,
				regExclude,
				extension,
			},
			controller.signal,
		).then((names) => {
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
