import { type ReactNode, useEffect, useRef, useState } from "react";
import type { MetadataSetting } from "../../../model/MetadataSettings";
import { BlueButton, WhiteButton } from "../../element/Button";
import { AddButton, RemoveButton } from "../../element/ButtonIcon";
import { CheckBox, ControllTextBox, InputLabel, SelectBox } from "../../element/Input";

export default function SettingDaialog(props: {
    setting: MetadataSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: MetadataSetting) => void;
}) {
    const dialogRef = useRef<HTMLDialogElement>(null);
    useEffect(() => { dialogRef.current?.showModal() }, [])
    const [target, setTarget] = useState(props.setting)
    const handleTargetChange = async (select: string) => setTarget(current => current.replace(select))
    return (
        <>
            <dialog ref={dialogRef} onClose={props.handleDialogClose}
                className="overflow-y-auto fixed 
                        top-0 right-0 left-0 
                        z-50
                      bg-white
                        border border-gray-200"
            >
                <div className="p-4 rounded-lg mt-2">
                    <Select name="target" defaultValue={target.handler()} handleOnChange={handleTargetChange}>
                        <option value="name">name</option>
                        <option value="pattern">pattern</option>
                        <option value="outerJoin">outerJoin</option>
                        <option value="innerJoin">innerJoin</option>
                        <option value="fullJoin">fullJoin</option>
                    </Select>
                    {target.join()
                        ?
                        <>
                            <Text name="left" value={target.join()?.left ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceJoin({ left: newVal.target.value }))}
                            />
                            <Text name="right" value={target.join()?.right ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceJoin({ right: newVal.target.value }))}
                            />
                            <Select name="condition" defaultValue={target.join()?.on ? "on" : "column"}
                                handleOnChange={async (select) => setTarget(cur => cur.replaceJoin({ [select]: select === "on" ? "" : [] }))}
                            >
                                <option value="on">on</option>
                                <option value="column">column</option>
                            </Select>
                            {target.join()?.on
                                ?
                                <Text name="on" ignoreLabel={true} value={target.join()?.on ?? ""}
                                    handleChange={newVal => setTarget(cur => cur.replaceJoin({ on: newVal.target.value }))}
                                />
                                :
                                <Arrays name="column" ignoreLabel={true} values={target.join()?.column ?? []}
                                    handleChange={(text, index) => setTarget(cur => cur.replaceJoinColumn(text, index))}
                                    handleRemove={(index) => setTarget(cur => cur.removeJoinColumn(index))}
                                />
                            }
                        </>
                        : target.pattern
                            ?
                            <>
                                <Text name="pattern" ignoreLabel={true} value={target.pattern?.string ?? ""}
                                    handleChange={(text) => setTarget(cur => cur.replacePattern({ string: text.target.value }))}
                                />
                                <Arrays name="patternExclue" values={target.pattern?.exclude ?? []}
                                    handleChange={(text, index) => setTarget(cur => cur.replacePatternExclude(text, index))}
                                    handleRemove={(index) => setTarget(cur => cur.removePatternExclude(index))}
                                />
                            </>
                            :
                            <Arrays name="name" values={target.name ?? []}
                                handleChange={(text, index) => setTarget(cur => cur.replaceName(text, index))}
                                handleRemove={(index) => setTarget(cur => cur.removeName(index))}
                            />
                    }
                    <Check name="split"
                        value={target.split ? "true" : "false"}
                        handleOnChange={async checked => setTarget(cur => cur.withSplit(checked))}
                    />
                    {target.split
                        ?
                        <>
                            <Text name="prefix" value={target.split.prefix ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceSplit({ prefix: newVal.target.value }))}
                            />
                            <Text name="tableName" value={target.split.tableName ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceSplit({ tableName: newVal.target.value }))}
                            />
                            <Text name="suffix" value={target.split.suffix ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceSplit({ suffix: newVal.target.value }))}
                            />
                            <Text name="limit" value={target.split.limit ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceSplit({ limit: newVal.target.value }))}
                            />
                            <Arrays name="breakKey" values={target.split.breakKey ?? []}
                                handleChange={(text, index) => setTarget(cur => cur.replaceSplitBreakKey(text, index))}
                                handleRemove={(index) => setTarget(cur => cur.removeSplitBreakKey(index))}
                            />
                        </>
                        :
                        <>
                            <Text name="prefix" value={target.tableName?.prefix ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceTableName({ prefix: newVal.target.value }))}
                            />
                            <Text name="tableName" value={target.tableName?.tableName ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceTableName({ tableName: newVal.target.value }))}
                            />
                            <Text name="suffix" value={target.tableName?.suffix ?? ""}
                                handleChange={newVal => setTarget(cur => cur.replaceTableName({ suffix: newVal.target.value }))}
                            />
                        </>
                    }
                    <Arrays name="exclude" values={target.exclude}
                        handleChange={(text, index) => setTarget(cur => cur.replaceExclude(text, index))}
                        handleRemove={(index) => setTarget(cur => cur.removeExclude(index))}
                    />
                    <Arrays name="include" values={target.include}
                        handleChange={(text, index) => setTarget(cur => cur.replaceInclude(text, index))}
                        handleRemove={(index) => setTarget(cur => cur.removeInclude(index))}
                    />
                    <Arrays name="keys" values={target.keys}
                        handleChange={(text, index) => setTarget(cur => cur.replaceKeys(text, index))}
                        handleRemove={(index) => setTarget(cur => cur.removeKeys(index))}
                    />
                    <Arrays name="filter" values={target.filter}
                        handleChange={(text, index) => setTarget(cur => cur.replaceFilter(text, index))}
                        handleRemove={(index) => setTarget(cur => cur.removeFilter(index))}
                    />
                    <Arrays name="order" values={target.order}
                        handleChange={(text, index) => setTarget(cur => cur.replaceOrder(text, index))}
                        handleRemove={(index) => setTarget(cur => cur.removeOrder(index))}
                    />
                </div>
                <div className="flex items-center justify-end">
                    <BlueButton title="Commit" handleClick={() => props.handleCommit(target)} />
                    <WhiteButton title="Close" handleClick={props.handleDialogClose} />
                </div>
            </dialog>
        </>
    );
}
function Check(props: {
    handleOnChange: ((checked: boolean) => Promise<void>); name: string, value: string
}) {
    return (
        <div className="grid grid-cols-5 justify-center">
            <InputLabel id={props.name} name={props.name} required={false} wStyle="p-2.5 w=1/5" />
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
function Select(props: {
    defaultValue: string, handleOnChange: ((selected: string) => Promise<void>), name: string, children: ReactNode
}) {
    return (
        <div className="grid grid-cols-5 justify-center pb-2">
            <InputLabel id={props.name} name={props.name} required={false} wStyle="p-2.5 w=1/5" />
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
function Text(props: { name: string, value: string, handleChange: (text: React.ChangeEvent<HTMLInputElement>) => void, ignoreLabel?: boolean }) {
    return (
        <>
            <div className="grid grid-cols-5 justify-center pb-2">
                {!props.ignoreLabel &&
                    <InputLabel id={props.name} name={props.name} required={false} wStyle="p-2.5 w=1/5" />
                }
                <ControllTextBox
                    name={props.name}
                    id={props.name}
                    required={true}
                    wStyle="col-start-2 col-span-3"
                    value={props.value}
                    handleChange={props.handleChange}
                />
            </div>
        </>
    );
}
function Arrays(props: {
    name: string, values: string[]
    , handleChange: (text: string, index: number) => void
    , handleRemove: (index: number) => void
    , ignoreLabel?: boolean
}) {
    const [text, setText] = useState("");
    return (
        <>
            {props.values.length === 0
                ?
                <div className="grid grid-cols-5 justify-center pb-2">
                    {!props.ignoreLabel &&
                        <InputLabel id={props.name} name={props.name} required={false} wStyle="p-2.5 w=1/5" />
                    }
                    <ControllTextBox
                        name={props.name}
                        id={props.name}
                        required={true}
                        wStyle="col-start-2 col-span-3"
                        value={text}
                        handleChange={(ev) => setText(ev.target.value)}
                        handleBlur={(ev) => props.handleChange(ev.target.value, 0)}
                    />
                </div>
                :
                props.values.map((val, index) => {
                    return (
                        <ArraysText key={val ?? index} name={props.name} val={val} index={index}
                            handleChange={props.handleChange}
                            handleRemove={props.handleRemove}
                            ignoreLabel={props.ignoreLabel}
                        />
                    )
                })
            }
            <div className="grid grid-cols-5 justify-center pb-2">
                {(props.values.length > 0 && props.values[props.values.length - 1]) &&
                    <div className="col-start-2">
                        <AddButton handleClick={() => props.handleChange("", props.values.length + 1)} />
                    </div>
                }
            </div>
        </>
    );
}
function ArraysText(props: {
    name: string, val: string, index: number
    , handleChange: (text: string, index: number) => void
    , handleRemove: (index: number) => void
    , ignoreLabel?: boolean
}) {
    const [text, setText] = useState(props.val);
    const handleBlur = (newVal: React.FocusEvent<HTMLInputElement>) => props.handleChange(newVal.target.value, props.index)
    const handleRemove = () => props.handleRemove(props.index)
    return (
        <div className="grid grid-cols-5 pb-2">
            {(!props.ignoreLabel && props.index === 0) &&
                <InputLabel id={props.name} name={props.name} required={false} wStyle="p-2.5 w=1/5" />
            }
            <ControllTextBox
                name={props.name}
                id={props.name}
                required={true}
                wStyle="col-start-2 col-span-3"
                value={text}
                handleChange={(ev) => setText(ev.target.value)}
                handleBlur={handleBlur}
            />
            {props.index > 0 &&
                <div className="col-start-5">
                    <RemoveButton handleClick={handleRemove} />
                </div>
            }
        </div>
    )
}