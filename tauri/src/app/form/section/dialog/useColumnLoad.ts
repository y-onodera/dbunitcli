import { useState } from "react";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import { renderColumnLoadState } from "./SqlInsertParts";

// 1テーブルの列を読み込み、列選択のダイアログに渡す。読込中・失敗中はcolumnsがnull。
// 別のテーブルを開くと前回を中断し、閉じても中断する（アンマウントでも中断される）
export function useColumnLoad(
	onQueryColumns: (table: string, context: JobContext) => Promise<string[]>,
) {
	const job = useJob();
	const [target, setTarget] = useState<{
		table: string;
		columns: string[] | null;
	} | null>(null);

	const open = async (table: string) => {
		if (target?.table === table && job.running) {
			return;
		}
		setTarget({ table, columns: null });
		const result = await job.run((context) => onQueryColumns(table, context));
		if (result) {
			setTarget({ table, columns: result.value });
		}
	};

	const close = () => {
		job.cancel();
		setTarget(null);
	};

	// 読込中・失敗時の表示。読み込めていればnull
	const renderState = (title: string) =>
		target === null
			? null
			: renderColumnLoadState({
					title,
					table: target.table,
					columns: target.columns,
					loading: job.running,
					error: job.error,
					logs: job.logs,
					onClose: close,
				});

	return { target, job, open, close, renderState };
}
