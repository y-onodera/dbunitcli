import { JobLog } from "../element/JobLog";
import { StatusMessage } from "./DialogLayout";

// sidecarのジョブが失敗したときの表示: エラーメッセージと、原因を追えるよう実行中に届いたログ
export function JobFailure({
	message,
	lines,
}: {
	message: string;
	lines: string[];
}) {
	return (
		<>
			<StatusMessage kind="error">Error: {message}</StatusMessage>
			<JobLog lines={lines} className="mb-3" />
		</>
	);
}
