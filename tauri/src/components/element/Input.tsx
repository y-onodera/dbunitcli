import { type ReactNode, useRef, useState } from "react";
import { getErrorMessage } from "../../utils/fetchUtils";
import { ClearIcon } from "./Icon";

export function InputLabel(props: {
	text: string;
	id: string;
	required: boolean;
	hidden?: boolean;
	wStyle?: string;
}) {
	return (
		<label
			htmlFor={props.id}
			style={props.hidden ? { display: "none" } : {}}
			className={`block
                   ${props.wStyle ? props.wStyle : "w-full"}
                   font-medium text-sm text-content wrap-anywhere
                   `}
		>
			{props.text}
			{props.required && "*"}
		</label>
	);
}
// 手で全部消したときと同じinputイベントを起こし、onChangeや親でまとめて受けるchange（検証エラーの消去等）をそのまま動かす
function clearInput(input: HTMLInputElement | null) {
	if (!input) {
		return;
	}
	Object.getOwnPropertyDescriptor(
		HTMLInputElement.prototype,
		"value",
	)?.set?.call(input, "");
	input.dispatchEvent(new Event("input", { bubbles: true }));
	input.focus();
}
// 入力欄の右端に重ねる、入力を消すボタン。マウス操作用でTab移動の対象にはしない
function ClearButton(props: { label: string; handleClick: () => void }) {
	return (
		<button
			type="button"
			tabIndex={-1}
			aria-label={props.label}
			className="absolute inset-y-0 end-0 flex items-center pe-2 text-content-muted hover:text-link-hover"
			onClick={props.handleClick}
		>
			<ClearIcon title="clear" />
		</button>
	);
}
export function ControllTextBox(props: {
	name: string;
	id: string;
	required: boolean;
	value: string;
	placeholder?: string;
	/** 枠の配置と幅（格子の位置など）。入力欄そのものの見た目は変えない */
	wStyle?: string;
	list?: string;
	disabled?: boolean;
	hidden?: boolean;
	/** falseなら入力を消すボタンを出さない */
	clearable?: boolean;
	handleChange: (ev: React.ChangeEvent<HTMLInputElement>) => void;
	handleBlur?: (ev: React.FocusEvent<HTMLInputElement>) => void;
}) {
	const inputRef = useRef<HTMLInputElement>(null);
	const showClear =
		(props.clearable ?? true) && props.value !== "" && !props.disabled;
	return (
		<div
			className={`relative ${props.wStyle ?? "w-full"}`}
			style={props.hidden ? { display: "none" } : {}}
		>
			<input
				ref={inputRef}
				name={props.name}
				id={props.id}
				type="text"
				list={props.list ?? ""}
				className={`${inputStyle("w-full")} ${showClear ? "pe-8" : ""}`}
				required={props.required}
				disabled={!!props.disabled}
				value={props.value}
				placeholder={props.placeholder}
				onChange={props.handleChange}
				onBlur={(ev) => props.handleBlur?.(ev)}
				autoComplete={props.list ? "off" : "on"}
			/>
			{showClear && (
				<ClearButton
					label={`clear ${props.name}`}
					handleClick={() => clearInput(inputRef.current)}
				/>
			)}
		</div>
	);
}
export function SelectBox(props: {
	name: string;
	id: string;
	required: boolean;
	hidden?: boolean;
	wStyle?: string;
	defaultValue?: string;
	handleOnChange?: (selected: string) => void | Promise<void>;
	children: ReactNode;
}) {
	const [selected, setSelected] = useState(props.defaultValue ?? "");
	// 設定の読込み等で親から初期値が変わったら選択を合わせる
	const [prevDefault, setPrevDefault] = useState(props.defaultValue);
	if (props.defaultValue !== prevDefault) {
		setPrevDefault(props.defaultValue);
		setSelected(props.defaultValue ?? "");
	}
	return (
		<select
			name={props.name}
			id={props.id}
			className={inputStyle(props.wStyle ? props.wStyle : "w-40")}
			required={props.required}
			style={props.hidden ? { display: "none" } : {}}
			value={selected}
			onChange={(event) => {
				const value = event.currentTarget.value;
				setSelected(value);
				Promise.resolve(props.handleOnChange?.(value)).catch((error) => {
					console.error("Select Error:", error);
					alert(`Failed to apply ${props.name}: ${getErrorMessage(error)}`);
				});
			}}
		>
			{props.children}
		</select>
	);
}
export function CheckBox(props: {
	name: string;
	id: string;
	hidden?: boolean;
	defaultValue?: string;
	handleOnChange?: (checked: boolean) => void;
}) {
	const [checked, setChecked] = useState(props.defaultValue === "true");
	// 設定の読込み等で親から初期値が変わったらチェックを合わせる
	const [prevDefault, setPrevDefault] = useState(props.defaultValue);
	if (props.defaultValue !== prevDefault) {
		setPrevDefault(props.defaultValue);
		setChecked(props.defaultValue === "true");
	}
	return (
		<>
			<input
				name={props.name}
				id={props.id}
				type="checkbox"
				className="w-4 h-4
                           text-primary
                           border border-border
                           ring-primary-ring
                           focus-visible:ring-3 "
				style={props.hidden ? { display: "none" } : {}}
				checked={checked}
				value={`${checked}`}
				onChange={() => {
					setChecked(!checked);
					props.handleOnChange?.(!checked);
				}}
			/>
			<input
				name={props.name}
				id={`${props.id}hidden`}
				type="hidden"
				value={`${checked}`}
			/>
		</>
	);
}
export function ResourceDatalist({
	id,
	resources,
}: {
	id: string;
	resources: string[];
}) {
	return (
		<datalist id={`${id}_list`}>
			{resources.map((resource) => (
				<option key={resource} value={resource} />
			))}
		</datalist>
	);
}
export function PreviewField({
	id,
	label,
	value,
	placeholder,
}: {
	id: string;
	label: string;
	value: string;
	placeholder?: string;
}) {
	return (
		<div>
			<InputLabel text={label} id={id} required={false} />
			<div
				id={id}
				className="block p-2.5 
                           w-full 
                           text-sm text-content-secondary 
                           rounded-lg 
                           bg-surface-muted 
                           border border-border 
                           font-mono 
                           break-all"
			>
				{value || <span className="text-content-disabled">{placeholder}</span>}
			</div>
		</div>
	);
}
// 高さのある flex 縦並び（最大化中のダイアログ）では残りを埋め、それ以外では min-h-96 の高さになる
export function TextArea({
	ref,
	...props
}: {
	id?: string;
	ref?: React.Ref<HTMLTextAreaElement>;
	value: string;
	readOnly?: boolean;
	placeholder?: string;
	spellCheck?: boolean;
	onChange?: (ev: React.ChangeEvent<HTMLTextAreaElement>) => void;
}) {
	return (
		<textarea
			id={props.id}
			ref={ref}
			className={`p-2.5
                       w-full flex-1 min-h-96
                       text-sm text-content
                       rounded-lg
                       bg-input
                       disabled:bg-input-disabled
                       border border-border
                       ring-primary-ring
                       focus-visible:ring-3`}
			value={props.value}
			readOnly={props.readOnly}
			placeholder={props.placeholder}
			spellCheck={props.spellCheck}
			onChange={props.onChange}
		/>
	);
}
export function FilterInput({
	value,
	onChange,
	placeholder,
}: {
	value: string;
	onChange: (value: string) => void;
	placeholder?: string;
}) {
	const inputRef = useRef<HTMLInputElement>(null);
	return (
		<div className="relative mb-1">
			<input
				ref={inputRef}
				type="text"
				value={value}
				onChange={(e) => onChange(e.target.value)}
				placeholder={placeholder}
				className={`w-full px-2 py-1 text-sm border border-border rounded bg-input focus-visible:ring-3 ring-primary-ring ${value === "" ? "" : "pe-8"}`}
			/>
			{value !== "" && (
				<ClearButton
					label="clear filter"
					handleClick={() => clearInput(inputRef.current)}
				/>
			)}
		</div>
	);
}
function inputStyle(wStyle: string) {
	return `block
               p-2.5
               ${wStyle}
               text-sm text-content
               rounded-lg
               bg-input
               disabled:bg-input-disabled
               border border-border
               ring-primary-ring
               focus-visible:ring-3 `;
}
