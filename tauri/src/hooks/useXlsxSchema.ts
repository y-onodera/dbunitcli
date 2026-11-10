import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import {
	fetchData,
	getErrorMessage,
	logFetchError,
	postJsonParams,
} from "../utils/fetchUtils";
import { type ResourceParser, useSaveResource } from "./useResourceFiles";

export const useSaveXlsxSchema = () => {
	return useSaveResource<XlsxSchema>("xlsxSchema");
};

export const XLSX_SCHEMA_PARSER: ResourceParser<XlsxSchema> = {
	empty: () => XlsxSchema.create(),
	parse: (content) => XlsxSchema.build(content as XlsxSchemaBuilder),
};

// 空にするたびに別の配列を作って再描画しないよう共有する
const NO_SHEETS: string[] = [];

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
		...postJsonParams(`${apiUrl}xlsx-schema/sheets`, {
			...sheetSrc,
			recursive: sheetSrc.recursive === "true",
		}),
		signal,
	};
	try {
		const response = await fetchData(fetchParams);
		return (await response.json()) as string[];
	} catch (ex) {
		logFetchError(getErrorMessage(ex), fetchParams);
		return [];
	}
}

export const useSrcInfoSheets = (srcInfo: DatasetSrcInfo): string[] => {
	const [sheetNames, setSheetNames] = useState<string[]>(NO_SHEETS);
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
			setSheetNames(NO_SHEETS);
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
