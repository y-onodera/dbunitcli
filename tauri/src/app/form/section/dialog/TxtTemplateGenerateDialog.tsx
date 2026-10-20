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

// templateは常に生成・反映する。unitSetting・srcは選択した場合のみダイアログに入力欄を表示して生成し、
// scaffold実行結果もgenerateフォームへ反映する（どのセクションにも属さないキーは反映しない）
const generateSections = [
	{
		id: "template",
		label: "template",
		optional: false,
		match: (key: string) => key === "-template" || key.startsWith("-template."),
	},
	{
		id: "unitSetting",
		label: "unitSetting",
		optional: true,
		match: (key: string) => key.startsWith("-unitSetting"),
	},
	{
		id: "src",
		label: "src",
		optional: true,
		match: (key: string) => key.startsWith("-src."),
	},
] as const;

type OptionalSectionId = "unitSetting" | "src";

const optionalSections = generateSections.filter((section) => section.optional);

export function filterReflectParams(
	params: Record<string, string>,
	selected: ReadonlySet<OptionalSectionId>,
): Record<string, string> {
	return Object.fromEntries(
		Object.entries(params).filter(([key]) =>
			generateSections.some(
				(section) =>
					(!section.optional ||
						selected.has(section.id as OptionalSectionId)) &&
					section.match(key),
			),
		),
	);
}

function ReflectSectionSelect({
	selected,
	onChange,
}: {
	selected: ReadonlySet<OptionalSectionId>;
	onChange: (next: Set<OptionalSectionId>) => void;
}) {
	return (
		<fieldset className="flex gap-4">
			<legend>generate section</legend>
			{optionalSections.map((section) => (
				<label key={section.id} className="flex items-center gap-1">
					<input
						type="checkbox"
						checked={selected.has(section.id as OptionalSectionId)}
						onChange={(e) => {
							const next = new Set(selected);
							if (e.target.checked) {
								next.add(section.id as OptionalSectionId);
							} else {
								next.delete(section.id as OptionalSectionId);
							}
							onChange(next);
						}}
					/>
					{section.label}
				</label>
			))}
		</fieldset>
	);
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
	const [selected, setSelected] = useState<Set<OptionalSectionId>>(
		new Set(optionalSections.map((section) => section.id as OptionalSectionId)),
	);
	const withSrc = selected.has("src");
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
				dialog.job(),
			);
			await Promise.all([
				handleReflect({
					...filterReflectParams(params, selected),
					"-unit": "table",
				}),
				reloadResources(),
			]);
			if (!dialog.isAborted()) {
				handleDialogClose();
			}
		});

	return (
		<RefreshableDialogShell
			command="dialogs/txt-template-generate"
			label="Scaffold"
			title="Scaffold"
			formId={formId}
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			logs={dialog.logs}
			onExecute={handleExecute}
			onClose={handleDialogClose}
		>
			{(options) => (
				<>
					<ReflectSectionSelect selected={selected} onChange={setSelected} />
					<Select
						handleTypeSelect={dialog.refresh}
						prefix=""
						element={withoutParameterTarget(options.target)}
					/>
					<PlainText prefix="" element={requiredOption(options.template)} />
					{selected.has("unitSetting") && (
						<PlainText prefix="" element={options.unitSetting} />
					)}
					{withSrc && (
						<>
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
				title="Scaffold"
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
