import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useResourceEncoding } from "../context/ResourceEncodingProvider";
import {
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../model/FixedColumnDef";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
} from "../utils/fetchUtils";
import { resourceLoadParams, useSaveResource } from "./useResourceFiles";

export const useSaveFixedColumnDef = () => {
	return useSaveResource<FixedColumnDef>("fixedColumnDef");
};

export const useFixedColumnDefData = (
	fileName: string,
): { def: FixedColumnDef; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const encoding = useResourceEncoding("fixedColumnDef");
	const [def, setDef] = useState(FixedColumnDef.create());
	const [loading, setLoading] = useState(fileName !== "");

	useEffect(() => {
		if (!fileName) {
			setDef(FixedColumnDef.create());
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		loadFixedColumnDef(apiUrl, fileName, encoding).then((result) => {
			if (!controller.signal.aborted) {
				setDef(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [fileName, apiUrl, encoding]);

	return { def, loading };
};

async function loadFixedColumnDef(
	apiUrl: string,
	name: string,
	encoding: string,
): Promise<FixedColumnDef> {
	if (name === "") {
		return FixedColumnDef.create();
	}
	const fetchParams = resourceLoadParams(
		apiUrl,
		"fixedColumnDef",
		name,
		encoding,
	);
	return fetchData(fetchParams)
		.then((response) => response.json())
		.then((schema: FixedColumnDefBuilder) => FixedColumnDef.build(schema))
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return FixedColumnDef.create();
		});
}
