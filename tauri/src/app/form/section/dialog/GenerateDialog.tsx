import { type ReactElement, type ReactNode, useState } from "react";
import { AddButton } from "../../../../components/element/ButtonIcon";
import type { GenerateOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import { DatasetLoadForm } from "../DatasetLoadForm";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import ResourceEditButton, {
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import { type DialogJob, postDialogForm } from "./refreshableDialogForm";
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
	isAborted: () => boolean;
	// generate/refreshと同じgenerateType付きで値を送る
	withGenerateType: (values: FormValues) => FormValues;
	// 生成をジョブとして送るためのオプション（ログ表示・ダイアログを閉じたときの中断）
	job: DialogJob;
};

function GenerateFormDialog({
	config,
	leading,
	onExecute,
	onClose,
}: {
	config: GenerateDialogConfig;
	leading?: ReactNode;
	onExecute: (
		values: FormValues,
		context: GenerateExecuteContext,
	) => Promise<void>;
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
			onExecute={() =>
				dialog.execute((values) =>
					onExecute(values, {
						apiUrl: dialog.apiUrl,
						isAborted: dialog.isAborted,
						withGenerateType: dialog.withGenerateType,
						job: dialog.job(),
					}),
				)
			}
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

// 既存の呼び出し元はpathも渡すため、編集ボタンと同じpropsを受け付ける（pathは未使用）
type GenerateButtonProps = Partial<ResourceEditButtonProp> &
	Pick<ResourceEditButtonProp, "setPath">;

// 生成結果を既存の編集ダイアログで確認・命名保存する形式（Settings・XlsxSchema・FixedColumnDef）
export function createModelGenerateButton<BUILDER, MODEL>(
	config: GenerateDialogConfig & {
		endpoint: string;
		build: (builder: BUILDER) => MODEL;
		renderEditDialog: (
			model: MODEL,
			props: {
				fileName: string;
				handleDialogClose: () => void;
				handleSave: (fileName: string) => void;
			},
		) => ReactElement;
	},
) {
	function ModelGenerateDialog({
		handleSave,
		handleDialogClose,
	}: {
		handleSave: (fileName: string) => void;
		handleDialogClose: () => void;
	}) {
		const [generated, setGenerated] = useState<MODEL | null>(null);
		if (generated) {
			return config.renderEditDialog(generated, {
				fileName: "",
				handleDialogClose,
				handleSave,
			});
		}
		return (
			<GenerateFormDialog
				config={config}
				onClose={handleDialogClose}
				onExecute={async (
					values,
					{ apiUrl, isAborted, withGenerateType, job },
				) => {
					const builder = await postDialogForm<BUILDER>(
						apiUrl,
						config.endpoint,
						withGenerateType(values),
						job,
					);
					if (!isAborted()) {
						setGenerated(config.build(builder));
					}
				}}
			/>
		);
	}

	return function ModelGenerate({ setPath }: GenerateButtonProps) {
		return (
			<GenerateButton setPath={setPath}>
				{(handleDialogClose, handleSave) => (
					<ModelGenerateDialog
						handleDialogClose={handleDialogClose}
						handleSave={handleSave}
					/>
				)}
			</GenerateButton>
		);
	};
}

type FileGenerate = (
	values: FormValues,
	context: GenerateExecuteContext,
) => Promise<string | null>;

function FileGenerateDialog({
	config,
	leading,
	useGenerate,
	handleDialogClose,
	handleSave,
}: {
	config: GenerateDialogConfig;
	leading?: ReactNode;
	useGenerate: () => FileGenerate;
	handleDialogClose: () => void;
	handleSave: (fileName: string) => void;
}) {
	const generate = useGenerate();
	return (
		<GenerateFormDialog
			config={config}
			leading={leading}
			onClose={handleDialogClose}
			onExecute={async (values, context) => {
				const fileName = await generate(values, context);
				if (fileName !== null) {
					handleSave(fileName);
				}
			}}
		/>
	);
}

// 生成のみで完結しパスを返す形式（XlsxTemplate）。
// useGenerateはダイアログ表示時にのみ呼ばれ、中断された場合はnull・成功時は保存したファイル名を返す関数を返す
export function FileGenerateButton({
	setPath,
	config,
	leading,
	useGenerate,
}: GenerateButtonProps & {
	config: GenerateDialogConfig;
	leading?: ReactNode;
	useGenerate: () => FileGenerate;
}) {
	return (
		<GenerateButton setPath={setPath}>
			{(handleDialogClose, handleSave) => (
				<FileGenerateDialog
					config={config}
					leading={leading}
					useGenerate={useGenerate}
					handleDialogClose={handleDialogClose}
					handleSave={handleSave}
				/>
			)}
		</GenerateButton>
	);
}
