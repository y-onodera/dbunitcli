import { useState } from "react";
import { Arrays, Check, Fieldset, KeyValues, Select, SettingDialog, Text } from "../../components/dialog/SettingDialog";
import { ExpandButton } from "../../components/element/ButtonIcon";
import type { DatasetSetting } from "../../model/DatasetSettings";

export default function DatasetSettingDaialog(props: {
    setting: DatasetSetting
    handleDialogClose: () => void;
    handleCommit: (newSettings: DatasetSetting) => void;
}) {
    const [target, setTarget] = useState(props.setting)
    const handleTargetChange = async (select: string) => setTarget(current => current.replace(select))
    const [showOptional, setShowOptional] = useState(false);
    const toggleOptional = () => setShowOptional(!showOptional)

    return (
        <SettingDialog setting={target} handleDialogClose={props.handleDialogClose} handleCommit={props.handleCommit}>
            <Fieldset>
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
            </Fieldset>
            <Fieldset>
                <Check name="split"
                    value={target.split ? "true" : "false"}
                    handleOnChange={checked => setTarget(cur => cur.withSplit(checked))}
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
                        <Text name="prefix" value={target.prefix ?? ""}
                            handleChange={newVal => setTarget(cur => cur.with({ prefix: newVal.target.value }))}
                        />
                        <Text name="tableName" value={target.tableName ?? ""}
                            handleChange={newVal => setTarget(cur => cur.with({ tableName: newVal.target.value }))}
                        />
                        <Text name="suffix" value={target.suffix ?? ""}
                            handleChange={newVal => setTarget(cur => cur.with({ suffix: newVal.target.value }))}
                        />
                    </>
                }
            </Fieldset>
            <Fieldset>
                <ExpandButton
                    toggleOptional={toggleOptional}
                    showOptional={showOptional}
                    caption="Additinaol Columns"
                />
                {showOptional && (
                    <>
                        <KeyValues name="string" values={target.string}
                            handleChange={(index, newValue) => setTarget(cur => cur.replaceString(index, newValue))}
                            handleRemove={(index) => setTarget(cur => cur.removeString(index))}
                        />
                        <KeyValues name="number" values={target.number}
                            handleChange={(index, newValue) => setTarget(cur => cur.replaceNumber(index, newValue))}
                            handleRemove={(index) => setTarget(cur => cur.removeNumber(index))}
                        />
                        <KeyValues name="boolean" values={target.boolean}
                            handleChange={(index, newValue) => setTarget(cur => cur.replaceBoolean(index, newValue))}
                            handleRemove={(index) => setTarget(cur => cur.removeBoolean(index))}
                        />
                        <KeyValues name="function" values={target.function}
                            handleChange={(index, newValue) => setTarget(cur => cur.replaceFunction(index, newValue))}
                            handleRemove={(index) => setTarget(cur => cur.removeFunction(index))}
                        />
                    </>
                )}
            </Fieldset>
            <Fieldset>
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
            </Fieldset>
        </SettingDialog>
    );
}