// 入力欄の下に出す検証エラー
export default function FieldErrorMessages(prop: {
	messages: string[];
	hidden?: boolean;
}) {
	if (prop.hidden) {
		return null;
	}
	return prop.messages.map((message) => (
		<p key={message} className="text-xs text-error">
			{message}
		</p>
	));
}
