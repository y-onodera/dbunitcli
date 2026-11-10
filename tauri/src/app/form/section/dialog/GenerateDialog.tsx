import { type ReactElement, type ReactNode, useState } from "react";
import { AddButton } from "../../../../components/element/ButtonIcon";
import type { JobContext } from "../../../../hooks/useJob";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import ResourceEditButton, {
	type LoadedResourceDialogProps,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import { postDialogForm } from "./refreshableDialogForm";
import { useRefreshableDialog } from "./useRefreshableDialog";

type GenerateDialogConfig = {
	generateType: string;
	formId: string;
	// DatasetLoadFormのname（フィールドid接頭辞）
	name: string;
	command: string;
	label: string;
	title: string;
};

export type GenerateExecuteContext = {
	apiUrl: string;
	// generate/refreshと同じgenerateType付きで値を送る
	withGenerateType: (values: FormValues) => FormValues;
	// 生成の実行に渡すfetchオプション（ログ表示・ダイアログを閉じたときのsidecar側の中断）
	fetchOptions: JobContext;
};

// onExecuteで生成し、成功したら（中断されていなければ）結果をonDoneへ渡す
function GenerateFormDialog<T>({
	config,
	leading,
	onExecute,
	onDone,
	onClose,
}: {
	config: GenerateDialogConfig;
	leading?: ReactNode;
	onExecute: (
		values: FormValues,
		context: GenerateExecuteContext,
	) => Promise<T>;
	onDone: (value: T) => void;
	onClose: () => void;
}) {
	const dialog = useRefreshableDialog<GenerateOptions>({
		refreshEndpoint: "generate/refresh",
		formId: config.formId,
		generateType: config.generateType,
	});
	return (
		<RefreshableDialogShell
			command={config.command}
			label={config.label}
			title={config.title}
			formId={config.formId}
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			logs={dialog.logs}
			onExecute={async () => {
				const result = await dialog.execute((values, context) =>
					onExecute(values, {
						apiUrl: dialog.apiUrl,
						withGenerateType: dialog.withGenerateType,
						fetchOptions: context,
					}),
				);
				if (result) {
					onDone(result.value);
				}
			}}
			onClose={onClose}
		>
			{(options) => (
				<>
					{leading}
					<DatasetLoadForm
						handleTypeSelect={dialog.refresh}
						name={config.name}
						srcData={options.srcData}
					/>
				</>
			)}
		</RefreshableDialogShell>
	);
}

// 「generate」トリガーボタンとダイアログの開閉・保存後のパス反映を担う
function GenerateButton({
	setPath,
	children,
}: {
	setPath: (value: string) => void;
	children: (
		handleDialogClose: () => void,
		handleSave: (fileName: string) => void,
	) => ReactElement;
}) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return children(closeDialog, (fileName) => {
			setPath(fileName);
			closeDialog();
		});
	};
	return (
		<ResourceEditButton
			renderDialog={renderDialog}
			renderTrigger={(openDialog) => (
				<AddButton title="generate" handleClick={openDialog} />
			)}
		/>
	);
}

// 編集ボタンと並べて同じpropsで呼ばれるため、pathも受け付ける（未使用）
export type GenerateButtonProps = Partial<ResourceEditButtonProp> &
	Pick<ResourceEditButtonProp, "setPath">;

export type ModelGenerateConfig<BUILDER, MODEL> = GenerateDialogConfig & {
	endpoint: string;
	build: (builder: BUILDER) => MODEL;
	renderEditDialog: (
		model: MODEL,
		props: LoadedResourceDialogProps,
	) => ReactElement;
};

function ModelGenerateDialog<BUILDER, MODEL>({
	config,
	handleSave,
	handleDialogClose,
}: {
	config: ModelGenerateConfig<BUILDER, MODEL>;
	handleSave: (fileName: string) => void;
	handleDialogClose: () => void;
}) {
	const [generated, setGenerated] = useState<MODEL | null>(null);
	if (generated) {
		// 生成結果はまだファイルになっていないため、開いたファイルの場所は無い
		return config.renderEditDialog(generated, {
			fileName: "",
			location: null,
			handleDialogClose,
			handleSave,
		});
	}
	return (
		<GenerateFormDialog
			config={config}
			onClose={handleDialogClose}
			onExecute={(values, { apiUrl, withGenerateType, fetchOptions }) =>
				postDialogForm<BUILDER>(
					apiUrl,
					config.endpoint,
					withGenerateType(values),
					fetchOptions,
				)
			}
			onDone={(builder) => setGenerated(config.build(builder))}
		/>
	);
}

// 生成結果を編集ダイアログで確認・命名保存する形式
export function ModelGenerateButton<BUILDER, MODEL>({
	config,
	setPath,
}: {
	config: ModelGenerateConfig<BUILDER, MODEL>;
} & Pick<ResourceEditButtonProp, "setPath">) {
	return (
		<GenerateButton setPath={setPath}>
			{(handleDialogClose, handleSave) => (
				<ModelGenerateDialog
					config={config}
					handleDialogClose={handleDialogClose}
					handleSave={handleSave}
				/>
			)}
		</GenerateButton>
	);
}

type FileGenerate = (
	values: FormValues,
	context: GenerateExecuteContext,
) => Promise<string>;

// 生成のみで完結しパスを返す形式。generateは保存したファイル名を返す
export function FileGenerateButton({
	setPath,
	config,
	leading,
	generate,
}: GenerateButtonProps & {
	config: GenerateDialogConfig;
	leading?: ReactNode;
	generate: FileGenerate;
}) {
	return (
		<GenerateButton setPath={setPath}>
			{(handleDialogClose, handleSave) => (
				<GenerateFormDialog
					config={config}
					leading={leading}
					onClose={handleDialogClose}
					onExecute={generate}
					onDone={handleSave}
				/>
			)}
		</GenerateButton>
	);
}
