import type { ReactNode } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
} from "../../../../components/dialog";
import { WhiteButton } from "../../../../components/element/Button";
import {
	ControllTextBox,
	InputLabel,
	SelectBox,
} from "../../../../components/element/Input";
import { JobProgress } from "../../../../components/element/JobProgress";
import type { Conjunction } from "../../../../utils/textInsert";

export type SelectOption<T extends string> = { value: T; label: string };
export type SelectGroup<T extends string> = {
	label: string;
	options: SelectOption<T>[];
};

export const CONJUNCTION_OPTIONS: SelectOption<Conjunction>[] = [
	{ value: "AND", label: "AND" },
	{ value: "OR", label: "OR" },
];

// valueを省略すると先頭の選択肢が初期値になる。optgroupで分けるときはgroups、分けないときはoptionsを渡す。
// groupsのlabelが空ならoptgroupで囲まない。
// 条件付きで出し入れする欄はvalueを渡す（再マウントで表示が先頭に戻り、呼び出し側のstateとずれるため）
export function OptionSelect<T extends string>({
	id,
	label,
	options,
	groups = [{ label: "", options: options ?? [] }],
	value,
	onChange,
}: {
	id: string;
	label: string;
	options?: SelectOption<T>[];
	groups?: SelectGroup<T>[];
	value?: T;
	onChange: (value: T) => void;
}) {
	return (
		<div className="flex-1">
			<InputLabel text={label} id={id} required={false} />
			<SelectBox
				name={id}
				id={id}
				required={false}
				wStyle="w-full"
				defaultValue={value ?? groups[0].options[0].value}
				handleOnChange={(value) => onChange(value as T)}
			>
				{groups.map((group) =>
					group.label === "" ? (
						renderOptions(group.options)
					) : (
						<optgroup key={group.label} label={group.label}>
							{renderOptions(group.options)}
						</optgroup>
					),
				)}
			</SelectBox>
		</div>
	);
}

function renderOptions<T extends string>(options: SelectOption<T>[]) {
	return options.map((o) => (
		<option key={o.value} value={o.value}>
			{o.label}
		</option>
	));
}

export function OptionText({
	id,
	label,
	value,
	required = false,
	disabled = false,
	clearable,
	onChange,
}: {
	id: string;
	label: string;
	value: string;
	required?: boolean;
	disabled?: boolean;
	clearable?: boolean;
	onChange: (value: string) => void;
}) {
	return (
		<div className="flex-1">
			<InputLabel text={label} id={id} required={required} />
			<ControllTextBox
				name={id}
				id={id}
				required={required}
				disabled={disabled}
				clearable={clearable}
				value={value}
				handleChange={(ev) => onChange(ev.target.value)}
			/>
		</div>
	);
}

export function OptionCheck({
	id,
	label,
	checked,
	disabled = false,
	onChange,
}: {
	id: string;
	label: string;
	checked: boolean;
	disabled?: boolean;
	onChange: (checked: boolean) => void;
}) {
	return (
		<label htmlFor={id} className="flex items-center gap-1 mb-1 text-sm">
			<input
				id={id}
				type="checkbox"
				checked={checked}
				disabled={disabled}
				onChange={(e) => onChange(e.target.checked)}
				className="w-4 h-4 text-primary"
			/>
			{label}
		</label>
	);
}

// 挿入される内容を先に見せ、用途の選び間違いに気付けるようにする
export function InsertPreview({ lines }: { lines: string[] }) {
	if (lines.length === 0) {
		return null;
	}
	return (
		<DialogField last>
			<pre
				data-testid="insert-preview"
				className="text-caption whitespace-pre-wrap break-all overflow-y-auto max-h-40"
			>
				{lines.join("\n")}
			</pre>
		</DialogField>
	);
}

// 入れ子のダイアログの枠: 見出しと対象のテーブル名
export function NestedDialogFrame({
	title,
	table,
	onClose,
	children,
}: {
	title: string;
	table?: string;
	onClose: () => void;
	children: ReactNode;
}) {
	return (
		<ModalOverlay onClose={onClose}>
			<DialogTitle>{title}</DialogTitle>
			{table !== undefined && (
				<p className="text-caption mb-4 truncate" title={table}>
					{table}
				</p>
			)}
			{children}
		</ModalOverlay>
	);
}

// 列の読込中・失敗時の表示。読み込めていればnullを返す
export function renderColumnLoadState({
	title,
	table,
	columns,
	loading,
	error,
	logs,
	onClose,
}: {
	title: string;
	table: string;
	columns: string[] | null;
	loading: boolean;
	error: string;
	logs: string[];
	onClose: () => void;
}): ReactNode {
	if (loading) {
		return (
			<ModalOverlay onClose={onClose}>
				<JobProgress title="Loading..." lines={logs} onAbandon={onClose} />
			</ModalOverlay>
		);
	}
	if (error || columns === null) {
		return (
			<NestedDialogFrame title={title} table={table} onClose={onClose}>
				{error && <JobFailure message={error} lines={logs} />}
				<DialogActions>
					<WhiteButton title="Close" handleClick={onClose} />
				</DialogActions>
			</NestedDialogFrame>
		);
	}
	return null;
}
