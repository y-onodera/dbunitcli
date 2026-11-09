import { useEffect, useRef } from "react";

// 末尾から何px以内なら「末尾を見ている」とみなすか
const STICK_THRESHOLD_PX = 16;

// sidecarのジョブからpushされたログ行を表示する。末尾を見ている間だけ、新しい行が届いたら末尾へスクロールする
export function JobLog({
	lines,
	className = "",
}: {
	lines: string[];
	className?: string;
}) {
	const ref = useRef<HTMLPreElement>(null);
	const stickToBottomRef = useRef(true);
	useEffect(() => {
		if (ref.current && lines.length > 0 && stickToBottomRef.current) {
			ref.current.scrollTop = ref.current.scrollHeight;
		}
	}, [lines]);
	if (lines.length === 0) {
		return null;
	}
	return (
		<pre
			ref={ref}
			role="log"
			aria-label="execution log"
			onScroll={(ev) => {
				const el = ev.currentTarget;
				stickToBottomRef.current =
					el.scrollHeight - el.scrollTop - el.clientHeight <=
					STICK_THRESHOLD_PX;
			}}
			className={`${className} w-full max-h-60 overflow-auto rounded-lg border border-border-subtle bg-surface-subtle p-2 text-xs text-content whitespace-pre-wrap break-all`}
		>
			{lines.join("\n")}
		</pre>
	);
}
