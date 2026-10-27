import { JobProgress } from "../../components/element/JobProgress";
import { useSidecarLog } from "../../hooks/useSidecarLog";

// 起動中（sidecarの応答待ち）の表示。sidecarの出力を出し、起動に失敗したときの原因も見えるようにする
export default function SidecarLoading() {
	const logs = useSidecarLog();
	return (
		<div className="mx-auto max-w-4xl">
			<JobProgress title="Loading..." lines={logs} />
		</div>
	);
}
