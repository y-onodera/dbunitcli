import { core } from "@tauri-apps/api";
import { useState } from "react";
import {
	DialogBody,
	DialogFooter,
	DialogTitle,
	FullDialog,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { JdbcConnectionProvider } from "../../../../context/JdbcConnectionProvider";
import type { ConvertOptions } from "../../../../model/SelectParameter";
import type { FormValues } from "../../../../utils/formValues";
import ResultFormSection from "../ResultFormSection";
import { RefreshableDialogShell } from "./RefreshableDialogShell";
import type { DialogFetchOptions } from "./refreshableDialogForm";
import { useRefreshableDialog } from "./useRefreshableDialog";

export type TableConvert = (
	result: FormValues,
	fetchOptions: DialogFetchOptions,
) => Promise<string>;

// プレビュー中のテーブルだけをconvertする。resultフォームはconvertの既定値から入力する
export default function TableConvertDialog({
	tableName,
	convert,
	onClose,
}: {
	tableName: string;
	// 読込元のJDBC接続はプレビュー側で確定させたものを使う（下のJdbcConnectionProviderの影響を受けない）
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
	// resultのjdbc入力で読込元（プレビュー）の接続状態を書き換えないよう分離する
	return (
		<JdbcConnectionProvider>
			<TableConvertFormDialog
				tableName={tableName}
				convert={convert}
				onDone={setResultDir}
				onClose={onClose}
			/>
		</JdbcConnectionProvider>
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
			onExecute={() =>
				dialog.execute(async (values, context) => {
					const resultDir = await convert(values, context);
					if (!context.signal.aborted) {
						onDone(resultDir);
					}
				})
			}
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
						handleClick={() =>
							core.invoke("open_directory", { path: resultDir })
						}
					/>
				)}
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogFooter>
		</FullDialog>
	);
}
