import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import {
	FixedColumnDef,
	type FixedColumnDefBuilder,
} from "../model/FixedColumnDef";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
} from "../utils/fetchUtils";
import { useUpdateResourceFiles } from "./useResourceFiles";

export const useSaveFixedColumnDef = () => {
	const updateResourceFiles = useUpdateResourceFiles("fixedColumnDef");
	return (name: string, input: FixedColumnDef) =>
		updateResourceFiles("save", JSON.stringify({ name, input }));
};

export const useFixedColumnDefData = (
	fileName: string,
): { def: FixedColumnDef; loading: boolean } => {
	const { apiUrl } = useEnvironment();
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
		loadFixedColumnDef(apiUrl, fileName).then((result) => {
			if (!controller.signal.aborted) {
				setDef(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [fileName, apiUrl]);

	return { def, loading };
};

async function loadFixedColumnDef(
	apiUrl: string,
	name: string,
): Promise<FixedColumnDef> {
	if (name === "") {
		return FixedColumnDef.create();
	}
	const fetchParams = {
		endpoint: `${apiUrl}fixed-column-def/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	return fetchData(fetchParams)
		.then((response) => response.json())
		.then((schema: FixedColumnDefBuilder) => FixedColumnDef.build(schema))
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return FixedColumnDef.create();
		});
}
