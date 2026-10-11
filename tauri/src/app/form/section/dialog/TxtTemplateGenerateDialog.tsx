import { useState } from "react";
import { AddButton } from "../../../../components/element/ButtonIcon";
import { useWorkspaceResourcesReload } from "../../../../hooks/useWorkspaceResources";
import type { CommandOption } from "../../../../model/CommandOption";
import type { ScaffoldOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import PlainText from "../element/PlainText";
import Select from "../element/Select";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import { postDialogForm } from "./refreshableDialogForm";
import { useRefreshableDialog } from "./useRefreshableDialog";

const formId = "txtTemplateGenerateForm";

export type TxtTemplateGenerateFormValues = FormValues;

// scaffold実行結果はgenerateフォームへのtxt駆動反映が前提のため、parameter targetは選ばせない
function withoutParameterTarget(target: CommandOption): CommandOption {
	return {
		...target,
		attribute: {
			...target.attribute,
			selectOption: target.attribute.selectOption.filter(
				(it) => it !== "parameter",
			),
		},
	};
}

// 反映には-templateが必須（sidecarのexecも-template必須で検証する）
function requiredOption(element: CommandOption): CommandOption {
	return { ...element, attribute: { ...element.attribute, required: true } };
}

function TxtTemplateGenerateDialog({
	prefill,
	handleReflect,
	handleDialogClose,
}: {
	prefill: TxtTemplateGenerateFormValues;
	handleReflect: (params: Record<string, string>) => Promise<void>;
	handleDialogClose: () => void;
}) {
	const reloadResources = useWorkspaceResourcesReload();
	const dialog = useRefreshableDialog<ScaffoldOptions>({
		refreshEndpoint: "scaffold/refresh",
		formId,
		initialValues: prefill,
	});

	const handleExecute = () =>
		dialog.execute(async (values) => {
			const params = await postDialogForm<Record<string, string>>(
				dialog.apiUrl,
				"scaffold/exec",
				values,
			);
			await Promise.all([handleReflect(params), reloadResources()]);
			if (!dialog.isAborted()) {
				handleDialogClose();
			}
		});

	return (
		<RefreshableDialogShell
			command="dialogs/txt-template-generate"
			label="Txt Template Generate"
			title="Generate Txt Template"
			formId={formId}
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			onExecute={handleExecute}
			onClose={handleDialogClose}
		>
			{(options) => (
				<>
					<Select
						handleTypeSelect={dialog.refresh}
						prefix=""
						element={withoutParameterTarget(options.target)}
					/>
					<PlainText prefix="" element={requiredOption(options.template)} />
					<PlainText prefix="" element={options.unitSetting} />
					<Select
						handleTypeSelect={dialog.refresh}
						prefix=""
						element={options.datasetType}
					/>
					<PlainText prefix="" element={options.datasetEncoding} />
					{[options.fixedLength, options.defaultLength, options.align]
						.filter(
							(field): field is NonNullable<typeof field> => field != null,
						)
						.map((field) => (
							<PlainText key={field.name} prefix="" element={field} />
						))}
					<DatasetLoadForm
						handleTypeSelect={dialog.refresh}
						name="scaffold"
						srcData={options.dataset}
					/>
				</>
			)}
		</RefreshableDialogShell>
	);
}

export function TxtTemplateGenerateButton({
	scaffoldPrefill,
	handleReflect,
}: {
	scaffoldPrefill: () => TxtTemplateGenerateFormValues;
	handleReflect: (params: Record<string, string>) => Promise<void>;
}) {
	const [prefill, setPrefill] = useState<TxtTemplateGenerateFormValues | null>(
		null,
	);
	return (
		<div>
			<AddButton
				title="generate"
				handleClick={() => setPrefill(scaffoldPrefill())}
			/>
			{prefill && (
				<TxtTemplateGenerateDialog
					prefill={prefill}
					handleReflect={handleReflect}
					handleDialogClose={() => setPrefill(null)}
				/>
			)}
		</div>
	);
}
