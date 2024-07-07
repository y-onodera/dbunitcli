import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { environment } from "../../feature/httpClient";
import { useSetSelectParameter, currentCommand, Parameter, SelectParameter, ConvertParams, CompareParams, GenerateParams, RunParams, ParameterizeParams } from "../../context/SelectParameterProvider";
import { formData } from "./Form";
import FormElements from "./FormElement";
import "../../App.css";

export default function CommandForm(prop:SelectParameter) {
  const command = currentCommand(prop);
  const setParameter = useSetSelectParameter();
  const handleTypeSelect = async () => {      
    await fetch(environment.serverUrl()+ command +"/refresh"
      ,{
        method:"POST"
        ,responseType:ResponseType.JSON
        ,headers: {'Content-Type': 'application/json'}
        ,body: Body.json(formData())
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
function ConvertForm(prop:{handleTypeSelect:Function,name:string,convert:ConvertParams}) {
  const srcData = prop.convert.srcData;
  const convertResult = prop.convert.convertResult;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={srcData.prefix} elements={srcData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={srcData.prefix} elements={srcData.jdbc ? srcData.jdbc.elements : []} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={srcData.prefix} elements={srcData.templateRender ? srcData.templateRender.elements : []} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={convertResult.prefix} elements={convertResult.elements} />
    </>
  );
}
function CompareForm(prop:{handleTypeSelect:Function,name:string,compare:CompareParams}) {
  const imageOption = prop.compare.imageOption;
  const newData = prop.compare.newData;
  const oldData = prop.compare.oldData;
  const expectData = prop.compare.expectData;
  const convertResult = prop.compare.convertResult;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.compare.elements} />
      {prop.compare.imageOption ? <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={imageOption.prefix} elements={imageOption.elements} />
                                :<></>}
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={newData.prefix} elements={newData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={oldData.prefix} elements={oldData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={convertResult.prefix} elements={convertResult.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={expectData.prefix} elements={expectData.elements} />
    </>
  );
}
function GenerateForm(prop:{handleTypeSelect:Function,name:string,generate:GenerateParams}) {
  const srcData = prop.generate.srcData;
  const templateOption = prop.generate.templateOption;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.generate.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={srcData.prefix} elements={srcData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={templateOption.prefix} elements={templateOption.elements} />
    </>
  );
}
function RunForm(prop:{handleTypeSelect:Function,name:string,run:RunParams}) {
  const srcData = prop.run.srcData;
  const templateOption = prop.run.templateOption;
  const jdbcOption = prop.run.jdbcOption;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.run.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={srcData.prefix} elements={srcData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={templateOption.prefix} elements={templateOption.elements} />
       {prop.run.jdbcOption ?<FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={jdbcOption.prefix} elements={jdbcOption.elements} />
                            :<></>
       }
    </>
  );
}
function ParameterizeForm(prop:{handleTypeSelect:Function,name:string,parameterize:ParameterizeParams}) {
  const paramData = prop.parameterize.paramData;
  const templateOption = prop.parameterize.templateOption;
  return (
    <>
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix="" elements={prop.parameterize.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={paramData.prefix} elements={paramData.elements} />
      <FormElements handleTypeSelect={prop.handleTypeSelect} name={prop.name} prefix={templateOption.prefix} elements={templateOption.elements} />
    </>
  );
}
