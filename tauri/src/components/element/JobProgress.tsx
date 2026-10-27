import { WhiteButton } from "./Button";
import { JobLog } from "./JobLog";

// sidecarの処理待ちの表示: 見出し・スピナー・届いたログ。onAbandonがあれば中断ボタンを出す
export function JobProgress({
	title,
	lines,
	onAbandon,
}: {
	title: string;
	lines: string[];
	onAbandon?: () => void;
}) {
	return (
		<div className="p-4 md:p-5 flex flex-col justify-center items-center">
			<h3 className="mb-5 text-lg font-normal text-content-muted">{title}</h3>
			<div className="block animate-spin h-10 w-10 border-4 border-primary rounded-full border-t-transparent" />
			<JobLog lines={lines} className="mt-4" />
			{onAbandon && (
				<div className="mt-4">
					<WhiteButton title="Abandoned" handleClick={onAbandon} />
				</div>
			)}
		</div>
	);
}
