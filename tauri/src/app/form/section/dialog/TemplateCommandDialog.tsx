import { useEffect, useRef, useState } from "react";
import { DialogTitle } from "../../../../components/dialog";
import { WhiteButton } from "../../../../components/element/Button";
import { PreviewButton } from "../../../../components/element/ButtonIcon";
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
	const dialogRef = useRef<HTMLDialogElement>(null);
	const environment = useEnvironment();
	const [loadState, setLoadState] = useState<LoadState>({ status: "loading" });

	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

	useEffect(() => {
		if (!name) {
			setLoadState({ status: "error", message: "No template selected" });
			return;
		}
		let isMounted = true;
		async function load() {
			const content = await loadParameterizeTemplateContent(
				environment.apiUrl,
				name,
			);
			if (isMounted) {
				setLoadState({ status: "loaded", content });
			}
		}
		load().catch((ex) => {
			if (isMounted) {
				setLoadState({ status: "error", message: getErrorMessage(ex) });
			}
		});
		return () => {
			isMounted = false;
		};
	}, [name, environment.apiUrl]);

	return (
		<dialog
			ref={dialogRef}
			onClose={handleDialogClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-surface border border-border-subtle"
		>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				<DialogTitle>Template - {name}</DialogTitle>
				{loadState.status === "loading" && (
					<div className="text-content-muted">Loading...</div>
				)}
				{loadState.status === "error" && (
					<div className="text-error">Error: {loadState.message}</div>
				)}
				{loadState.status === "loaded" && (
					<TextArea value={loadState.content} readOnly />
				)}
			</div>
			<div className="flex justify-end p-4">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
		</dialog>
	);
}

export function TemplateCommandButton({ name }: { name: string }) {
	const [showDialog, setShowDialog] = useState(false);
	const paramName = extractNameFromTemplatePath(name);
	return (
		<>
			<PreviewButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<ParameterizeTemplateDialog
					name={paramName}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
