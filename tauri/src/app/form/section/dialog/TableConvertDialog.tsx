import { useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import type { JobContext } from "../../../../hooks/useJob";
import type { ConvertOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import { openDirectory } from "../element/Chooser";
import ResultFormSection from "../ResultFormSection";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import { useRefreshableDialog } from "./useRefreshableDialog";

export type TableConvert = (
	result: FormValues,
	context: JobContext,
) => Promise<string>;

// プレビュー中のテーブルだけをconvertする。resultフォームはconvertの既定値から入力する
export default function TableConvertDialog({
	tableName,
	convert,
	onClose,
}: {
	tableName: string;
	convert: TableConvert;
	onClose: () => void;
}) {
	const [resultDir, setResultDir] = useState<string | null>(null);
	if (resultDir !== null) {
		return (
			<TableConvertResultDialog
				tableName={tableName}
				resultDir={resultDir}
				onClose={onClose}
			/>
		);
	}
	return (
		<TableConvertFormDialog
			tableName={tableName}
			convert={convert}
			onDone={setResultDir}
			onClose={onClose}
		/>
	);
}

function TableConvertFormDialog({
	tableName,
	convert,
	onDone,
	onClose,
}: {
	tableName: string;
	convert: TableConvert;
	onDone: (resultDir: string) => void;
	onClose: () => void;
}) {
	const dialog = useRefreshableDialog<ConvertOptions>({
		refreshEndpoint: "convert/refresh",
		formId: "tableConvertForm",
	});
	return (
		<RefreshableDialogShell
			command="sections/result-form"
			label="result"
			title={`${tableName} — Export`}
			formId="tableConvertForm"
			loadState={dialog.loadState}
			executing={dialog.executing}
			executeError={dialog.executeError}
			logs={dialog.logs}
			onExecute={async () => {
				const result = await dialog.execute(convert);
				if (result) {
					onDone(result.value);
				}
			}}
			onClose={onClose}
		>
			{(options) => (
				<ResultFormSection
					resultOption={options.convertResult}
					handleTypeSelect={dialog.refresh}
				/>
			)}
		</RefreshableDialogShell>
	);
}

function TableConvertResultDialog({
	tableName,
	resultDir,
	onClose,
}: {
	tableName: string;
	resultDir: string;
	onClose: () => void;
}) {
	return (
		<FullDialog onClose={onClose}>
			<DialogBody size="sm">
				<DialogTitle>{tableName} — Export</DialogTitle>
				<p className="text-sm text-content-muted">Export completed.</p>
			</DialogBody>
			<DialogFooter>
				{resultDir && (
					<BlueButton
						title="Open Directory"
						handleClick={() => openDirectory(resultDir)}
					/>
				)}
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogFooter>
		</FullDialog>
	);
}
