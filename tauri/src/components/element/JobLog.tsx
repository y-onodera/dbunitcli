import { useEffect, useRef } from "react";

// sidecarのジョブからpushされたログ行を表示する。新しい行が届いたら末尾へスクロールする
export function JobLog({ lines }: { lines: string[] }) {
	const ref = useRef<HTMLPreElement>(null);
	useEffect(() => {
		if (ref.current && lines.length > 0) {
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
			className="w-full max-h-60 overflow-auto rounded border border-border bg-surface-subtle p-2 text-xs text-content whitespace-pre-wrap break-all"
		>
			{lines.join("\n")}
		</pre>
	);
}
