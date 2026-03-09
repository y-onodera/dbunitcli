import { useEffect, useState } from "react";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import { useJdbcReadContent } from "../../hooks/useJdbc";

type JdbcPropertiesPreviewDialogProps = {
	path: string;
	handleDialogClose: () => void;
	handleApply: (values: Partial<Record<string, string>>) => void;
};

function parseProperties(content: string): Record<string, string> {
	const result: Record<string, string> = {};
	for (const line of content.split("\n")) {
		const trimmed = line.trim();
		if (!trimmed || trimmed.startsWith("#") || trimmed.startsWith("!"))
			continue;
		const sepIdx = trimmed.indexOf("=");
		if (sepIdx < 0) continue;
		const key = trimmed.substring(0, sepIdx).trim();
		const value = trimmed.substring(sepIdx + 1).trim();
		result[key] = value;
	}
	return result;
}

function extractJdbcValues(
	content: string,
): Partial<Record<string, string>> {
	// Try JSON format first
	try {
		const json = JSON.parse(content) as Record<string, string>;
		const result: Partial<Record<string, string>> = {};
		if (json.url) result.jdbcUrl = json.url;
		if (json.user) result.jdbcUser = json.user;
		const pass = json.password ?? json.pass;
		if (pass) result.jdbcPass = pass;
		return result;
	} catch {
		// Fall through to properties format
	}

	// Parse Java Properties format (url=, user=, pass=)
	const props = parseProperties(content);
	const result: Partial<Record<string, string>> = {};
	if (props.url) result.jdbcUrl = props.url;
	if (props.user) result.jdbcUser = props.user;
	const pass = props.pass ?? props.password;
	if (pass) result.jdbcPass = pass;
	return result;
}

export default function JdbcPropertiesPreviewDialog({
	path,
	handleDialogClose,
	handleApply,
}: JdbcPropertiesPreviewDialogProps) {
	const [content, setContent] = useState<string | null>(null);
	const readContent = useJdbcReadContent();

	useEffect(() => {
		readContent(path).then(setContent);
	}, [path, readContent]);

	const parsedValues = content !== null ? extractJdbcValues(content) : {};

	return (
		<SettingDialog
			setting={parsedValues}
			handleDialogClose={handleDialogClose}
			handleCommit={handleApply}
			commitLabel="Apply"
		>
			<div className="w-[600px]">
				<h2 className="text-lg font-bold mb-2">Properties File Preview</h2>
				<p className="text-sm text-gray-500 mb-3 break-all">{path}</p>
				{content === null ? (
					<p className="text-sm text-gray-400 p-3">Loading...</p>
				) : (
					<pre className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 overflow-auto max-h-96 whitespace-pre-wrap break-all">
						{content || "(empty)"}
					</pre>
				)}
			</div>
		</SettingDialog>
	);
}
