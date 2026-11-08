import { useEffect, useRef, useState } from "react";
import {
	CopyButton,
	DeleteButton,
	EditButton,
	FixButton,
	ParameterizeButton,
} from "../../components/element/ButtonIcon";
import { ControllTextBox } from "../../components/element/Input";
import {
	type ParameterizeFile,
	useDeleteParameterize,
	useLoadParameterizeFiles,
	useParameterActions,
} from "../../hooks/useSelectParameter";
import type { EditName } from "../main/Sidebar";
import ParameterizeDeleteDialog from "./ParameterizeDeleteDialog";
import ParameterizeOptionDialog from "./ParameterizeOptionDialog";

type MenuEditProp = {
	name: string;
	handleMenuDelete: () => void;
	handleMenuRename: (newName: string) => void;
	handleMenuCopy: () => void;
	handleMenuParameterize?: () => void;
};

export default function NameEditMenu({
	editName,
	setEditName,
}: {
	editName: EditName;
	setEditName: (editName: EditName) => void;
}) {
	const wrapperRef = useRef<HTMLDivElement>(null);
	useEffect(() => {
		function handleClickOutside(event: Event) {
			if (
				wrapperRef.current &&
				!wrapperRef.current.contains(event.target as HTMLElement)
			) {
				setEditName({} as EditName);
			}
		}
		document.addEventListener("mousedown", handleClickOutside);
		return () => document.removeEventListener("mousedown", handleClickOutside);
	}, [setEditName]);
	const { handleDelete, handleCopy, handleRename } = useParameterActions(
		editName.command,
		editName.name,
	);
	// メニューを閉じた後も開いておくため、メニュー（editName）とは別に持つ
	const [parameterizeTarget, setParameterizeTarget] = useState<{
		command: string;
		name: string;
	} | null>(null);
	const [deleteTarget, setDeleteTarget] = useState<{
		name: string;
		files: ParameterizeFile[];
	} | null>(null);
	const loadParameterizeFiles = useLoadParameterizeFiles();
	const deleteParameterize = useDeleteParameterize();
	const abortControllerRef = useRef<AbortController | null>(null);
	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		return () => controller.abort();
	}, []);
	// 参照するファイルがあるときだけ、一緒に消すかを確認する
	const deleteParameterizeOrConfirm = async (name: string) => {
		const files = await loadParameterizeFiles(name);
		if (files === null || abortControllerRef.current?.signal.aborted) {
			return;
		}
		if (files.length === 0) {
			deleteParameterize(name, []);
		} else {
			setDeleteTarget({ name, files });
		}
	};
	const canParameterize =
		editName.command && editName.command !== "parameterize";
	return (
		<>
			{editName.name && (
				<div
					ref={wrapperRef}
					className="absolute z-dropdown p-4 pb-0 text-content bg-surface border border-border-subtle rounded-lg shadow-dropdown"
					style={
						editName.afterEdge
							? { left: editName.x, top: editName.y - 150 }
							: { left: editName.x, top: editName.y }
					}
				>
					<MenuEdit
						name={editName.name}
						handleMenuDelete={() => {
							if (editName.command === "parameterize") {
								deleteParameterizeOrConfirm(editName.name);
							} else {
								handleDelete();
							}
							setEditName({} as EditName);
						}}
						handleMenuRename={(newName: string) => {
							handleRename(newName);
							setEditName({} as EditName);
						}}
						handleMenuCopy={() => {
							handleCopy();
							setEditName({} as EditName);
						}}
						handleMenuParameterize={
							canParameterize
								? () => {
										setParameterizeTarget({
											command: editName.command,
											name: editName.name,
										});
										setEditName({} as EditName);
									}
								: undefined
						}
					/>
				</div>
			)}
			{deleteTarget && (
				<ParameterizeDeleteDialog
					name={deleteTarget.name}
					files={deleteTarget.files}
					onClose={() => setDeleteTarget(null)}
				/>
			)}
			{parameterizeTarget && (
				<ParameterizeOptionDialog
					command={parameterizeTarget.command}
					name={parameterizeTarget.name}
					onClose={() => setParameterizeTarget(null)}
				/>
			)}
		</>
	);
}
function MenuEdit(prop: MenuEditProp) {
	const [hidden, setHidden] = useState(true);
	const [newName, setNewName] = useState(prop.name);
	const handleRenameTextOnChange = (text: string) => setNewName(text);
	return (
		<ul className="space-y-4">
			<li>
				<DeleteButton handleClick={prop.handleMenuDelete} />
			</li>
			<li>
				<div className="flex gap-x-0.5">
					<EditButton
						title="rename"
						handleClick={() => setHidden((current) => !current)}
					/>
					{!hidden && (
						<>
							<ControllTextBox
								name="menuRename"
								id="menuRename"
								required={false}
								wStyle="w-50"
								clearable={false}
								value={newName}
								handleChange={(text) =>
									handleRenameTextOnChange(text.target.value)
								}
							/>
							<FixButton
								title=""
								handleClick={() => {
									setHidden((current) => !current);
									prop.handleMenuRename(newName);
								}}
							/>
						</>
					)}
				</div>
			</li>
			<li>
				<CopyButton handleClick={prop.handleMenuCopy} />
			</li>
			{prop.handleMenuParameterize && (
				<li>
					<ParameterizeButton
						title="Parameterize"
						handleClick={prop.handleMenuParameterize}
					/>
				</li>
			)}
		</ul>
	);
}
