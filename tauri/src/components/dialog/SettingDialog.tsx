import { type ReactNode, useState } from "react";
import { type OperationResult, saveOnSuccess } from "../../utils/fetchUtils";
import { BlueButton, WhiteButton } from "../element/Button";
import {
	AddButton,
	FactCheckButton,
	RemoveButton,
} from "../element/ButtonIcon";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
	SelectBox,
} from "../element/Input";
import { DialogBody } from "./DialogLayout";
import { FullDialog } from "./FullDialog";
import { DialogFooter, DialogTitle } from "./ModalOverlay";

type CommonProps = {
	handleDialogClose: () => void;
	children: ReactNode;
	commitLabel?: string;
	commitDisabled?: boolean;
	title?: string;
	helpCommand?: string;
	helpLabel?: string;
};

// 入力中のファイル名へ保存し、成功したらonSaved(保存先)を呼ぶ
export type SaveThen = (onSaved: (path: string) => void) => void;

type SettingMode<T> = {
	setting: T;
	handleCommit: (newSettings: T) => void;
	fileName?: never;
	save?: never;
	handleSave?: never;
	// Commitの右に並べる追加ボタン
	footerActions?: () => ReactNode;
};

type FileMode = {
	setting?: never;
	handleCommit?: never;
	fileName: string;
	// 入力中のファイル名へ保存する。成功したときだけhandleSaveを呼ぶ（失敗時はダイアログを開いたまま）
	save: (path: string) => Promise<OperationResult>;
	handleSave: (path: string) => void;
	// Saveの右に並べる追加ボタン。保存が必要ならsaveThenを使う
	footerActions?: (saveThen: SaveThen) => ReactNode;
};

export type SettingDialogProps<T> = CommonProps & (SettingMode<T> | FileMode);

export function SettingDialog<T>(props: SettingDialogProps<T>) {
	const [fileNameValue, setFileNameValue] = useState(props.fileName ?? "");
	const isFileMode = props.fileName !== undefined;
	const saveThen: SaveThen = (onSaved) => {
		if (isFileMode) {
			saveOnSuccess(
				() => props.save(fileNameValue),
				() => onSaved(fileNameValue),
			);
		}
	};

	return (
		<FullDialog onClose={props.handleDialogClose}>
			{props.title && (
				<div className="px-4 pt-4">
					<DialogTitle command={props.helpCommand} label={props.helpLabel}>
						{props.title}
					</DialogTitle>
				</div>
			)}
			{isFileMode ? (
				<div className="relative overflow-x-auto flex flex-col gap-4">
					{props.children}
				</div>
			) : (
				<DialogBody size="fluid">{props.children}</DialogBody>
			)}
			{isFileMode && (
				<div className="px-4 pt-4">
					<div className="grid grid-cols-5 pb-2">
						<InputLabel
							id="fileNameLabel"
							text="name"
							required={false}
							wStyle="p-2.5"
						/>
						<ControllTextBox
							name="fileName"
							id="fileName"
							required={true}
							wStyle="col-start-2 col-span-4"
							value={fileNameValue}
							handleChange={(ev) => setFileNameValue(ev.target.value)}
						/>
					</div>
				</div>
			)}
			<DialogFooter>
				<BlueButton
					title={props.commitLabel ?? "Save"}
					disabled={props.commitDisabled}
					handleClick={() => {
						if (isFileMode) {
							saveThen(props.handleSave);
						} else {
							props.handleCommit(props.setting as T);
						}
					}}
				/>
				{isFileMode ? props.footerActions?.(saveThen) : props.footerActions?.()}
				<WhiteButton title="Close" handleClick={props.handleDialogClose} />
			</DialogFooter>
		</FullDialog>
	);
}

export function Fieldset(props: { children: ReactNode; legend?: string }) {
	return (
		<fieldset className="border border-border-subtle p-2.5 m-2">
			{props.legend && (
				<legend className="text-caption font-semibold px-1">
					{props.legend}
				</legend>
			)}
			{props.children}
		</fieldset>
	);
}

export function Check(props: {
	handleOnChange: (checked: boolean) => void;
	name: string;
	value: string;
}) {
	return (
		<div className="grid grid-cols-5 justify-center">
			<InputLabel
				id={props.name}
				text={props.name}
				required={false}
				wStyle="p-2.5"
			/>
			<div className="p-2.5">
				<CheckBox
					name={props.name}
					id={props.name}
					defaultValue={props.value}
					handleOnChange={props.handleOnChange}
				/>
			</div>
		</div>
	);
}

export function Text(props: {
	name: string;
	value: string;
	handleChange: (text: React.ChangeEvent<HTMLInputElement>) => void;
	handleBlur?: (ev: React.FocusEvent<HTMLInputElement>) => void;
	ignoreLabel?: boolean;
	list?: string;
}) {
	return (
		<div className="grid grid-cols-5 justify-center pb-2">
			{!props.ignoreLabel && (
				<InputLabel
					id={props.name}
					text={props.name}
					required={false}
					wStyle="p-2.5"
				/>
			)}
			<ControllTextBox
				name={props.name}
				id={props.name}
				required={true}
				wStyle="col-start-2 col-span-3"
				value={props.value}
				handleChange={props.handleChange}
				handleBlur={props.handleBlur}
				list={props.list}
			/>
		</div>
	);
}

export function Select(props: {
	defaultValue: string;
	handleOnChange: (selected: string) => Promise<void>;
	name: string;
	children: ReactNode;
}) {
	return (
		<div className="grid grid-cols-5 justify-center pb-2">
			<InputLabel
				id={props.name}
				text={props.name}
				required={false}
				wStyle="p-2.5"
			/>
			<SelectBox
				name={props.name}
				id={props.name}
				required={true}
				wStyle="col-start-2 col-span-2"
				defaultValue={props.defaultValue}
				handleOnChange={props.handleOnChange}
			>
				{props.children}
			</SelectBox>
		</div>
	);
}

export function Arrays(props: {
	name: string;
	values: string[];
	handleChange: (text: string, index: number) => void;
	handleRemove: (index: number) => void;
	ignoreLabel?: boolean;
	showCaption?: boolean;
	list?: string;
	handleSelect?: (index: number) => void;
}) {
	return (
		<>
			{props.values.length === 0 ? (
				<ArraysText
					key={0}
					name={props.name}
					val={""}
					index={0}
					handleChange={props.handleChange}
					handleRemove={props.handleRemove}
					ignoreLabel={props.ignoreLabel}
					showCaption={props.showCaption}
					list={props.list}
					handleSelect={props.handleSelect}
				/>
			) : (
				props.values.map((val, index) => {
					return (
						<ArraysText
							key={val ?? index}
							name={props.name}
							val={val}
							index={index}
							handleChange={props.handleChange}
							handleRemove={props.handleRemove}
							ignoreLabel={props.ignoreLabel}
							showCaption={props.showCaption}
							list={props.list}
							handleSelect={props.handleSelect}
						/>
					);
				})
			)}
			<div className="grid grid-cols-5 justify-center pb-2">
				{props.values.length > 0 && props.values[props.values.length - 1] && (
					<div className="col-start-2">
						<AddButton
							handleClick={() =>
								props.handleChange("new item", props.values.length + 1)
							}
						/>
					</div>
				)}
			</div>
		</>
	);
}

export function ArraysText(props: {
	name: string;
	val: string;
	index: number;
	handleChange: (text: string, index: number) => void;
	handleRemove: (index: number) => void;
	ignoreLabel?: boolean;
	showCaption?: boolean;
	list?: string;
	handleSelect?: (index: number) => void;
}) {
	const [text, setText] = useState(props.val);
	const handleBlur = (newVal: React.FocusEvent<HTMLInputElement>) =>
		props.handleChange(newVal.target.value, props.index);
	const handleRemove = () => props.handleRemove(props.index);
	const showLabel = !props.ignoreLabel && props.index === 0;
	return (
		<>
			{showLabel && props.showCaption && (
				<div className="grid grid-cols-5 pb-1">
					<span className="col-start-2 text-caption">column</span>
				</div>
			)}
			<div className="grid grid-cols-5 pb-2">
				{showLabel && (
					<InputLabel
						id={props.name}
						text={props.name}
						required={false}
						wStyle="p-2.5"
					/>
				)}
				<ControllTextBox
					name={props.name}
					id={props.name}
					required={true}
					wStyle="col-start-2"
					value={text}
					handleChange={(ev) => setText(ev.target.value)}
					handleBlur={handleBlur}
					list={props.list}
				/>
				{(props.handleSelect || props.index > 0) && (
					<div className="col-start-3 flex items-center">
						{props.handleSelect && (
							<FactCheckButton
								title="select"
								handleClick={() => props.handleSelect?.(props.index)}
							/>
						)}
						{props.index > 0 && <RemoveButton handleClick={handleRemove} />}
					</div>
				)}
			</div>
		</>
	);
}

export function KeyValues(props: {
	name: string;
	values: object;
	handleChange: (index: number, value: { [prop: string]: string }) => void;
	handleRemove: (index: number) => void;
}) {
	const entries = Object.entries(props.values);
	return (
		<>
			{entries.length === 0 ? (
				<KeyValueText
					propKey={""}
					name={props.name}
					value={""}
					index={0}
					handleChange={props.handleChange}
					handleRemove={props.handleRemove}
				/>
			) : (
				entries.map(([key, value], index) => {
					return (
						<KeyValueText
							key={key}
							propKey={key}
							name={props.name}
							value={value.toString()}
							index={index}
							handleChange={props.handleChange}
							handleRemove={props.handleRemove}
						/>
					);
				})
			)}
			<div className="grid grid-cols-5 justify-center pb-2">
				{entries.length > 0 && (
					<div className="col-start-2">
						<AddButton
							handleClick={() =>
								props.handleChange(entries.length, { "new item": "" })
							}
						/>
					</div>
				)}
			</div>
		</>
	);
}

export function KeyValueText(props: {
	name: string;
	propKey: string;
	value: string;
	index: number;
	handleChange: (index: number, value: { [prop: string]: string }) => void;
	handleRemove: (index: number) => void;
}) {
	const [key, setKey] = useState(props.propKey);
	const [value, setValue] = useState(props.value);
	const handleKeyBlur = (newVal: React.FocusEvent<HTMLInputElement>) =>
		props.handleChange(
			props.index,
			newVal.target.value ? { [newVal.target.value]: value } : {},
		);
	const handleValueBlur = (newVal: React.FocusEvent<HTMLInputElement>) =>
		props.handleChange(props.index, { [key]: newVal.target.value });
	const handleRemove = () => props.handleRemove(props.index);
	return (
		<>
			{props.index === 0 && (
				<div className="grid grid-cols-5 pb-1">
					<span className="col-start-2 text-caption">name</span>
					<span className="col-start-3 col-span-2 ml-1 text-caption">
						expression
					</span>
				</div>
			)}
			<div className="grid grid-cols-5 pb-2">
				{props.index === 0 && (
					<InputLabel
						id={props.name}
						text={props.name}
						required={false}
						wStyle="p-2.5"
					/>
				)}
				<ControllTextBox
					name={props.name}
					id={props.name}
					required={true}
					wStyle="col-start-2 p-2.5"
					value={key}
					handleChange={(ev) => setKey(ev.target.value)}
					handleBlur={handleKeyBlur}
				/>
				<ControllTextBox
					name={props.name}
					id={props.name}
					required={true}
					wStyle="col-start-3 col-span-2 ml-1 "
					value={value}
					disabled={!key || key === ""}
					handleChange={(ev) => setValue(ev.target.value)}
					handleBlur={handleValueBlur}
				/>
				{props.index > 0 && (
					<div className="col-start-5">
						<RemoveButton handleClick={handleRemove} />
					</div>
				)}
			</div>
		</>
	);
}
