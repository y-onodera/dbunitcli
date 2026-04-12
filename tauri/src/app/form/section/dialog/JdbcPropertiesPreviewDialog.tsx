import { Suspense, use } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { useJdbcReadContent } from "../../../../hooks/useJdbc";

type JdbcPropertiesPreviewDialogProps = {
	path: string;
	handleDialogClose: () => void;
	handleApply: (values: Partial<Record<string, string>>) => void;
};

function toJdbcFormValues(
	props: Record<string, string>,
): Partial<Record<string, string>> {
	const result: Partial<Record<string, string>> = {};
	if (props.url) {
		result.jdbcUrl = props.url;
	}
	if (props.user) {
		result.jdbcUser = props.user;
	}
	if (props.pass) {
		result.jdbcPass = props.pass;
	}
	return result;
}

export default function JdbcPropertiesPreviewDialog({
	path,
	handleDialogClose,
	handleApply,
}: JdbcPropertiesPreviewDialogProps) {
	const readContent = useJdbcReadContent();
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<Dialog
				promise={readContent(path)}
				path={path}
				handleDialogClose={handleDialogClose}
				handleApply={handleApply}
			/>
		</Suspense>
	);
}

function Dialog({
	promise,
	path,
	handleDialogClose,
	handleApply,
}: {
	promise: Promise<Record<string, string>>;
	path: string;
	handleDialogClose: () => void;
	handleApply: (values: Partial<Record<string, string>>) => void;
}) {
	const content = use(promise);
	const jdbcFormValues = toJdbcFormValues(content);

	return (
		<SettingDialog
			setting={jdbcFormValues}
			handleDialogClose={handleDialogClose}
			handleCommit={handleApply}
			commitLabel="Apply"
		>
			<div className="w-[600px]">
				<h2 className="text-lg font-bold mb-2">Properties File Preview</h2>
				<p className="text-sm text-gray-500 mb-3 break-all">{path}</p>
				<pre className="text-sm bg-gray-50 border border-gray-300 rounded-lg p-3 overflow-auto max-h-96 whitespace-pre-wrap break-all">
					{JSON.stringify(content, null, 2)}
				</pre>
			</div>
		</SettingDialog>
	);
}
