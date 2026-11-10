// 入力欄の下に出す検証エラー
export default function FieldErrorMessages(prop: {
	messages: string[];
	hidden?: boolean;
}) {
	if (prop.hidden) {
		return null;
	}
	// 同じ文言を重ねて出しても意味がなく、keyも重なるため1つにまとめる
	return [...new Set(prop.messages)].map((message) => (
		<p key={message} className="text-xs text-error">
			{message}
		</p>
	));
}
