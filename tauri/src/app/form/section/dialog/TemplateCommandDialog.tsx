import { useEffect, useRef, useState } from "react";
import { WhiteButton } from "../../../../components/element/Button";
import { PreviewButton } from "../../../../components/element/ButtonIcon";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import type { Command, Options } from "../../../../model/SelectParameter";
import { COMMANDS } from "../../../../model/SelectParameter";
import { fetchData, getErrorMessage } from "../../../../utils/fetchUtils";
import { CompareForm } from "../../CompareForm";
import { ConvertForm } from "../../ConvertForm";
import { GenerateForm } from "../../GenerateForm";
import { RunForm } from "../../RunForm";

type LoadState =
	| { status: "loading" }
	| { status: "error"; message: string }
	| { status: "loaded"; options: Options };

async function loadCommandOptions(
	apiUrl: string,
	command: Command,
	name: string,
): Promise<Options> {
	const fetchParams = {
		endpoint: `${apiUrl + command}/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name }),
		},
	};
	const response = await fetchData(fetchParams);
	const options = (await response.json()) as Options;
	(options as Record<string, unknown>).command = command;
	return options;
}

export function resolveCommand(cmdValue: string): Command | null {
	const lower = cmdValue.trim().toLowerCase() as Command;
	return COMMANDS.includes(lower) ? lower : null;
}

function renderCommandForm(options: Options, name: string) {
	const noop = async () => {};
	switch (options.command) {
		case "convert":
			return (
				<ConvertForm handleTypeSelect={noop} name={name} convert={options} />
			);
		case "compare":
			return (
				<CompareForm handleTypeSelect={noop} name={name} compare={options} />
			);
		case "generate":
			return (
				<GenerateForm handleTypeSelect={noop} name={name} generate={options} />
			);
		case "run":
			return <RunForm handleTypeSelect={noop} name={name} run={options} />;
		default:
			return (
				<p className="text-content-muted">
					This command type cannot be displayed.
				</p>
			);
	}
}

function TemplateCommandDialog({
	command,
	name,
	cmdValue,
	handleDialogClose,
}: {
	command: Command | null;
	name: string;
	cmdValue: string;
	handleDialogClose: () => void;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	const environment = useEnvironment();
	const [loadState, setLoadState] = useState<LoadState>({ status: "loading" });

	useEffect(() => {
		dialogRef.current?.showModal();
	}, []);

	useEffect(() => {
		if (!command) return;
		let isMounted = true;
		async function load() {
			const options = await loadCommandOptions(
				environment.apiUrl,
				command as Command,
				name,
			);
			if (isMounted) setLoadState({ status: "loaded", options });
		}
		load().catch((ex) => {
			if (isMounted)
				setLoadState({ status: "error", message: getErrorMessage(ex) });
		});
		return () => {
			isMounted = false;
		};
	}, [command, name, environment.apiUrl]);

	return (
		<dialog
			ref={dialogRef}
			onClose={handleDialogClose}
			className="overflow-y-auto fixed top-0 right-0 left-0 z-50 bg-white border border-gray-200"
		>
			<div className="overflow-y-auto max-h-[80vh] min-w-[600px] p-4">
				{command ? (
					<>
						<h2 className="text-lg font-bold mb-4">
							{command} - {name}
						</h2>
						{loadState.status === "loading" && (
							<div className="text-content-muted">Loading...</div>
						)}
						{loadState.status === "error" && (
							<div className="text-error">Error: {loadState.message}</div>
						)}
						{loadState.status === "loaded" &&
							renderCommandForm(loadState.options, name)}
					</>
				) : (
					<>
						<h2 className="text-lg font-bold mb-2">Command resolution error</h2>
						<p className="text-error">
							Cannot resolve command from cmd value: &quot;{cmdValue}&quot;
						</p>
						<p className="text-content-muted text-sm mt-1">
							The parent folder name must be one of: convert, compare, generate,
							run, parameterize.
						</p>
					</>
				)}
			</div>
			<div className="flex justify-end p-4">
				<WhiteButton title="Close" handleClick={handleDialogClose} />
			</div>
		</dialog>
	);
}

export function TemplateCommandButton({
	command,
	name,
	cmdValue,
}: {
	command: Command | null;
	name: string;
	cmdValue: string;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<PreviewButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<TemplateCommandDialog
					command={command}
					name={name}
					cmdValue={cmdValue}
					handleDialogClose={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
