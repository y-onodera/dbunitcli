import { createContext, Dispatch, ReactNode, SetStateAction, useContext, useState } from "react";

export type Attribute = {
    type: string;
    required: boolean;
    selectOption: string[];
}
export type CommandParameter ={
    name: string;
    value: string;
    attribute: Attribute;
}
export type CommandParameters = {
  handleTypeSelect:Function;
  prefix:string;
  elements: CommandParameter[]
}
export type DatasetSource = CommandParameters & {
  jdbc: CommandParameters;
  templateRender: CommandParameters;
}
export type ConvertParameters = {
  srcData: DatasetSource;
  convertResult: CommandParameters;
}
export type CompareParameters = {
  elements: CommandParameter[]
  newData: DatasetSource;
  oldData: DatasetSource;
  imageOption: CommandParameters;
  convertResult: CommandParameters;
  expectData: DatasetSource;
}
export type GenerateParameters = {
  elements: CommandParameter[]
  srcData: DatasetSource;
  templateOption: CommandParameters;
  convertResult: CommandParameters;
}
export type RunParameters = {
  elements: CommandParameter[]
  srcData: DatasetSource;
  templateOption: CommandParameters;
  jdbcOption: CommandParameters;
  convertResult: CommandParameters;
}
export type ParameterizeParameters = {
  elements: CommandParameter[]
  paramData: DatasetSource;
  templateOption: CommandParameters;
}
export type CommandParametersProp = {
  name:string;
  convert:ConvertParameters;
  compare:CompareParameters;
  generate:GenerateParameters;
  run:RunParameters;
  parameterize:ParameterizeParameters;
}
const selectParameterContext = createContext<CommandParametersProp>({} as CommandParametersProp);
const setSelectParameterContext = createContext<Dispatch<SetStateAction<CommandParametersProp>>>(
    () => undefined
);
export default function SelectParameterProvider(props:{children:ReactNode}) {
    const [parameter, setParameter] = useState<CommandParametersProp>({} as CommandParametersProp);
    return (
        <selectParameterContext.Provider value={parameter}>
          <setSelectParameterContext.Provider value={setParameter}>
            {props.children}
          </setSelectParameterContext.Provider>
        </selectParameterContext.Provider>
      );
}
export const useSelectParameter = () => useContext(selectParameterContext);
export const useSetSelectParameter = () => useContext(setSelectParameterContext);
export function currentCommand(parameter:CommandParametersProp) {
  return parameter.convert ? 'convert' : parameter.compare ? 'compare' : parameter.generate ? 'generate' : parameter.run ? 'run' : 'parameterize';
}
