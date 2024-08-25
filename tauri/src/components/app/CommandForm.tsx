import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { useSetSelectParameter, Parameter, ConvertParams, CompareParams, GenerateParams, RunParams, ParameterizeParams, useSelectParameter, DatasetSource } from "../../context/SelectParameterProvider";
import { formData } from "./Form";
import FormElements from "./FormElement";
import "../../App.css";
import { useEnviroment } from "../../context/EnviromentProvider";

export default function CommandForm() {
  const environment = useEnviroment();
  const prop = useSelectParameter();
  const command = prop.command;
  const setParameter = useSetSelectParameter();
  const handleTypeSelect = async () => {      
    await fetch(environment.apiUrl + command + "/refresh"
      ,{
        method:"POST"
        ,responseType:ResponseType.JSON
        ,headers: {'Content-Type': 'application/json'}
        ,body: Body.json(formData(false))
      })
      .then(response => setParameter(response.data as Parameter,command,prop.name))
    .catch((ex)=>alert(ex))
  }
  return (
    <>
      {command == 'convert' ? 
          <ConvertForm handleTypeSelect={handleTypeSelect} name={prop.name} convert={prop.convert} />
       : command == 'compare' ?
          <CompareForm handleTypeSelect={handleTypeSelect} name={prop.name} compare={prop.compare} />
       : command == 'generate' ?
          <GenerateForm handleTypeSelect={handleTypeSelect} name={prop.name} generate={prop.generate} />
       : command == 'run' ?
          <RunForm handleTypeSelect={handleTypeSelect} name={prop.name} run={prop.run} />
       : command == 'parameterize' ?
          <ParameterizeForm handleTypeSelect={handleTypeSelect} name={prop.name} parameterize={prop.parameterize} />
       :<></>
      }
    </>
  )
}
export function ConvertForm(prop:{handleTypeSelect:Function,name:string,convert:ConvertParams}) {
  const srcData = prop.convert.srcData;
  const convertResult = prop.convert.convertResult;
  return (
    <>
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={srcData} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={convertResult.prefix} elements={convertResult.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={convertResult.prefix} elements={convertResult.jdbc ? convertResult.jdbc.elements : []} />
    </>
  );
}
export function CompareForm(prop:{handleTypeSelect:Function,name:string,compare:CompareParams}) {
  const imageOption = prop.compare.imageOption;
  const newData = prop.compare.newData;
  const oldData = prop.compare.oldData;
  const expectData = prop.compare.expectData;
  const convertResult = prop.compare.convertResult;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.compare.elements} />
      {prop.compare.imageOption && <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={imageOption.prefix} elements={imageOption.elements} />}
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={newData} />
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={oldData} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={convertResult.prefix} elements={convertResult.elements} />
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={expectData} />
    </>
  );
}
export function GenerateForm(prop:{handleTypeSelect:Function,name:string,generate:GenerateParams}) {
  const srcData = prop.generate.srcData;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.generate.elements} />
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={srcData} />
      {prop.generate.templateOption && <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={prop.generate.templateOption.prefix} elements={prop.generate.templateOption.elements} />}
    </>
  );
}
export function RunForm(prop:{handleTypeSelect:Function,name:string,run:RunParams}) {
  const srcData = prop.run.srcData;
  const templateOption = prop.run.templateOption;
  const jdbcOption = prop.run.jdbcOption;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.run.elements} />
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={srcData} />
      {prop.run.jdbcOption && <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={templateOption.prefix} elements={templateOption.elements} /> }
      {prop.run.jdbcOption && <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={jdbcOption.prefix} elements={jdbcOption.elements} /> }
    </>
  );
}
export function ParameterizeForm(prop:{handleTypeSelect:Function,name:string,parameterize:ParameterizeParams}) {
  const paramData = prop.parameterize.paramData;
  const templateOption = prop.parameterize.templateOption;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.parameterize.elements} />
      <DatasetLoadForm handleTypeSelect={prop.handleTypeSelect} name={prop.name} srcData={paramData} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={templateOption.prefix} elements={templateOption.elements} />
    </>
  );
}
export function DatasetLoadForm(prop:{handleTypeSelect:Function,name:string,srcData:DatasetSource}) {
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={prop.srcData.prefix} elements={prop.srcData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={prop.srcData.prefix} elements={prop.srcData.jdbc ? prop.srcData.jdbc.elements : []} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={prop.srcData.prefix} elements={prop.srcData.templateRender ? prop.srcData.templateRender.elements : []} />
    </>
  );
}
