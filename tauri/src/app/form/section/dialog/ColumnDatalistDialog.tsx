import { useState } from "react";
import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
	Td,
	Tr,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useAbortSignalRef } from "../../../../hooks/useAbortSignalRef";
import { useColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import { useJobLog } from "../../../../hooks/useJobLog";
import type { DatasetSetting } from "../../../../model/DatasetSettings";
import { getErrorMessage } from "../../../../utils/fetchUtils";

export default function ColumnDatalistDialog({
	tableNames,
	target,
	onLoad,
	onClose,
}: {
	tableNames: string[];
	target: DatasetSetting;
	onLoad: (columns: string[]) => void;
	onClose: () => void;
}) {
	const filteredTables = filterTablesByTarget(tableNames, target);
	// 読込中のテーブル、または直前の読込の失敗メッセージ
	const [load, setLoad] = useState<{ loading: string } | { error: string }>({
		error: "",
	});
	const srcInfo = useDatasetSrcInfo();
	const fetchColumnNames = useColumnNamesFetcher();
	const abortControllerRef = useAbortSignalRef();
	const jobLog = useJobLog();

	// 閉じると（アンマウントでabort）sidecar側の読込も中断する
	const handleLoadColumns = (tableName: string) => {
		if ("loading" in load) {
			return;
		}
		if (!abortControllerRef.current) {
			return;
		}
		setLoad({ loading: tableName });
		jobLog.reset();
		const { signal } = abortControllerRef.current;
		fetchColumnNames(srcInfo, tableName, signal, jobLog.append).then(
			(columns) => {
				if (!abortControllerRef.current?.signal.aborted) {
					onLoad(columns);
					onClose();
				}
			},
			// 失敗したら閉じずに、エラーと実行中のログを残す（別のテーブルを選び直せる）
			(ex) => {
				if (!abortControllerRef.current?.signal.aborted) {
					setLoad({ error: getErrorMessage(ex) });
				}
			},
		);
	};

	function renderRow(name: string) {
		return (
			<Tr key={name}>
				<Td>{name}</Td>
				<Td align="right">
					<BlueButton
						title="Load Columns"
						handleClick={() => handleLoadColumns(name)}
					/>
				</Td>
			</Tr>
		);
	}

	function renderBody() {
		if (filteredTables.length === 0) {
			return <StatusMessage kind="empty">No matching tables.</StatusMessage>;
		}
		return (
			<DialogField last>
				<DataTable>{filteredTables.map(renderRow)}</DataTable>
			</DialogField>
		);
	}

	return (
		<ModalOverlay width="w-lg" zClass="z-modal-nested">
			<DialogTitle>Load Column Datalist</DialogTitle>
			{"loading" in load ? (
				<JobProgress
					title={`Loading ${load.loading}...`}
					lines={jobLog.logs}
					onAbandon={onClose}
				/>
			) : (
				<>
					{load.error && (
						<JobFailure message={load.error} lines={jobLog.logs} />
					)}
					{renderBody()}
					<DialogActions>
						<WhiteButton title="Close" handleClick={onClose} />
					</DialogActions>
				</>
			)}
		</ModalOverlay>
	);
}

function filterTablesByTarget(
	tableNames: string[],
	target: DatasetSetting,
): string[] {
	if (target.handler() === "name" && target.name && target.name.length > 0) {
		const nameSet = new Set(target.name.map((n) => n.toLowerCase()));
		return tableNames.filter((t) => nameSet.has(t.toLowerCase()));
	}
	if (target.handler() === "pattern" && target.pattern?.string) {
		try {
			const regex = new RegExp(target.pattern.string, "i");
			return tableNames.filter((t) => regex.test(t));
		} catch {
			return tableNames;
		}
	}
	return tableNames;
}
