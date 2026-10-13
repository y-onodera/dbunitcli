import { useEffect, useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { WhiteButton } from "../../../../components/element/Button";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { TextArea } from "../../../../components/element/Input";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import type { Command } from "../../../../model/SelectParameter";
import { COMMANDS } from "../../../../model/SelectParameter";
import { fetchData, getErrorMessage } from "../../../../utils/fetchUtils";

type LoadState =
	| { status: "loading" }
	| { status: "error"; message: string }
	| { status: "loaded"; content: string };

async function loadParameterizeTemplateContent(
	apiUrl: string,
	name: string,
): Promise<string> {
	const fetchParams = {
		endpoint: `${apiUrl}parameterize/template/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	const response = await fetchData(fetchParams);
	const data = (await response.json()) as { content?: string };
	return data.content ?? "";
}

export function resolveCommand(cmdValue: string): Command | null {
	const lower = cmdValue.trim().toLowerCase() as Command;
	return COMMANDS.includes(lower) ? lower : null;
}

function extractNameFromTemplatePath(templatePath: string): string {
	const filename =
		templatePath.replace(/\\/g, "/").split("/").pop() ?? templatePath;
	return filename.replace(/\.[^.]+$/, "");
}

function ParameterizeTemplateDialog({
	name,
	handleDialogClose,
}: {
	name: string;
	handleDialogClose: () => void;
}) {
	const environment = useEnvironment();
	const [loadState, setLoadState] = useState<LoadState>({ status: "loading" });

	useEffect(() => {
		if (!name) {
			setLoadState({ status: "error", message: "No template selected" });
			return;
		}
		const controller = new AbortController();
		async function load() {
			const content = await loadParameterizeTemplateContent(
				environment.apiUrl,
				name,
			);
			if (!controller.signal.aborted) {
				setLoadState({ status: "loaded", content });
			}
		}
		load().catch((ex) => {
			if (!controller.signal.aborted) {
				setLoadState({ status: "error", message: getErrorMessage(ex) });
			}
		});
		return () => {
			controller.abort();
		};
	}, [name, environment.apiUrl]);

	return (
		<FullDialog onClose={handleDialogClose}>
			<DialogBody size="auto" scrollable>
				<DialogTitle>Template - {name}</DialogTitle>
				{loadState.status === "loading" && (
					<StatusMessage kind="loading">Loading...</StatusMessage>
				)}
				{loadState.status === "error" && (
					<StatusMessage kind="error">Error: {loadState.message}</StatusMessage>
				)}
				{loadState.status === "loaded" && (
					<TextArea value={loadState.content} readOnly />
				)}
			</DialogBody>
			<DialogFooter>
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</DialogFooter>
		</FullDialog>
	);
}

export function TemplateCommandButton({
	name,
	isValidPath,
}: {
	name: string;
	isValidPath: boolean;
}) {
	const [showDialog, setShowDialog] = useState(false);
	const paramName = isValidPath ? extractNameFromTemplatePath(name) : "";
	return (
		<>
			<EditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<ParameterizeTemplateDialog
					name={paramName}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
