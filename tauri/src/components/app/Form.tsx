import React, { useEffect, useState } from "react";
import { open } from '@tauri-apps/api/dialog'
import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { environment } from "../../feature/httpClient";
import { CommandParam, CommandParams, SelectParameter, useSetSelectParameter, currentCommand, Parameter } from "../../context/SelectParameterProvider";
import "../../App.css";

type Prop = {
  prefix: string;
  element: CommandParam
  setPath?: Function
}
type FileProp = Prop & {
  setPath: Function
}
type SelectProp = Prop & {
  handleTypeSelect:Function;
}
const Forms:React.FC<SelectParameter> = (prop) => {
  const command = currentCommand(prop);
  const setParameter = useSetSelectParameter();
  const handleTypeSelect = async () => {      
    const formData = new FormData(document.querySelector('#commandForm') as HTMLFormElement);
    const data = Object.fromEntries(formData.entries());
    await fetch(environment.serverUrl()+ command +"/refresh"
      ,{
        method:"POST"
        ,responseType:ResponseType.JSON
        ,headers: {'Content-Type': 'application/json'}
        ,body: Body.json(data)
      })
      .then(response => setParameter(response.data as Parameter,command,prop.name))
    .catch((ex)=>alert(ex))
  }
return (
    <>
      {command == 'convert' ? 
        <>
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.convert.srcData.prefix} elements={prop.convert.srcData.elements} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.convert.srcData.prefix} elements={prop.convert.srcData.jdbc ? prop.convert.srcData.jdbc.elements : []} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.convert.srcData.prefix} elements={prop.convert.srcData.templateRender ? prop.convert.srcData.templateRender.elements : []} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.convert.convertResult.prefix} elements={prop.convert.convertResult.elements} />
        </>
       : command == 'compare' ?
        <>
          <Form handleTypeSelect={handleTypeSelect} prefix="" elements={prop.compare.elements} />
          {prop.compare.imageOption ? <Form handleTypeSelect={handleTypeSelect} prefix={prop.compare.imageOption.prefix} elements={prop.compare.imageOption.elements} />
                                    :<></>}
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.compare.newData.prefix} elements={prop.compare.newData.elements} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.compare.oldData.prefix} elements={prop.compare.oldData.elements} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.compare.convertResult.prefix} elements={prop.compare.convertResult.elements} />
          <Form handleTypeSelect={handleTypeSelect} prefix={prop.compare.expectData.prefix} elements={prop.compare.expectData.elements} />
        </>
       : command == 'generate' ?
       <>
         <Form handleTypeSelect={handleTypeSelect} prefix="" elements={prop.generate.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.generate.srcData.prefix} elements={prop.generate.srcData.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.generate.templateOption.prefix} elements={prop.generate.templateOption.elements} />
       </>
       : command == 'run' ?
       <>
         <Form handleTypeSelect={handleTypeSelect} prefix="" elements={prop.run.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.run.srcData.prefix} elements={prop.run.srcData.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.run.templateOption.prefix} elements={prop.run.templateOption.elements} />
          {prop.run.jdbcOption ?<Form handleTypeSelect={handleTypeSelect} prefix={prop.run.jdbcOption.prefix} elements={prop.run.jdbcOption.elements} />
                               :<></>
          }
       </>
       : command == 'parameterize' ?
       <>
         <Form handleTypeSelect={handleTypeSelect} prefix="" elements={prop.parameterize.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.parameterize.paramData.prefix} elements={prop.parameterize.paramData.elements} />
         <Form handleTypeSelect={handleTypeSelect} prefix={prop.parameterize.templateOption.prefix} elements={prop.parameterize.templateOption.elements} />
       </>
      :<></>
      }
    </>
  )
}
const Form:React.FC<CommandParams> = (prop) => {
  return (
    <>
      {prop.elements.map((element) => {
        if (element.attribute.type == 'FLG') {
          return <Check prefix={prop.prefix} element={element} key={prop.prefix+element.name}/>
        } else if (element.attribute.type == 'ENUM') {
          return <Select handleTypeSelect={prop.handleTypeSelect} prefix={prop.prefix} element={element} key={prop.prefix+element.name}/>
        }
        return <Text prefix={prop.prefix} element={element} key={prop.prefix+element.name}/>
      })}
    </>
  );
}
const Text:React.FC<Prop> = ({prefix,element}) => {
  const [path, setPath] = useState("");
  useEffect(()=>{setPath(element.value)},[element])
  return (
    <div >
      <label htmlFor={prefix + '_' + element.name} className="block 
          mb-2 
          text-sm text-gray-900 
          font-medium 
          dark:text-white">-{prefix && prefix + '.'}{element.name}{element.attribute.required && '*'}</label>
      <div className="flex">
        <input name={prefix ? '-' + prefix + '.' + element.name : '-' + element.name} id={prefix + '_' + element.name} type="text" className="block 
                  p-2.5 
                  w-full 
                  z-20 
                  text-sm text-gray-900 
                  bg-gray-50 
                  rounded-lg 
                  border border-gray-300 
                  ring-indigo-300 
                  focus-visible:ring 
                  dark:bg-gray-700 
                  dark:border-gray-600 
                  dark:placeholder-gray-400 
                  dark:text-white 
                  dark:focus:border-blue-500" 
                  required = {element.attribute.required} defaultValue={path} />
        {element.attribute.type.includes('FILE') && (
          <FileChooser prefix={prefix} element={element} setPath={setPath}/>
        )}
        {element.attribute.type.includes('DIR')&& (
          <DirectoryChooser prefix={prefix} element={element} setPath={setPath}/>
        )}
      </div>
    </div> 
  );
};
const FileChooser:React.FC<FileProp> = ({prefix,element,setPath})=>{
  const handleFileChooserClick = () => {
    open().then(files => files && setPath(files))
  }
  return (
    <> 
      <button type="button" id={prefix + '_' + element.name + 'FileChooser'} onClick={handleFileChooserClick} className="p-2.5 
                            ms-2 
                            text-sm 
                            font-medium 
                            text-white 
                            bg-indigo-500 
                            rounded-lg 
                            border border-gray-300 
                            ring-indigo-300 
                            focus-visible:ring 
                            hover:bg-indigo-600 
                            dark:bg-blue-600 
                            dark:hover:bg-indigo-700 
                            dark:focus:ring-indigo-800">
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="white">
          <path d="M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v240h-80v-200H520v-200H240v640h360v80H240Zm638 15L760-183v89h-80v-226h226v80h-90l118 118-56 57Zm-638-95v-640 640Z"/>
        </svg>
      </button>
    </>
  );
}
const DirectoryChooser:React.FC<FileProp> = ({prefix,element,setPath})=>{
  const handleDirectoryChooserClick = () => {
    open({directory:true}).then(files => files && setPath(files))
  }
  return (
    <> 
      <button type="button" id={prefix + '_' + element.name + 'DirectoryChooser'} onClick={handleDirectoryChooserClick} className="p-2.5 
                            ms-2 
                            text-sm 
                            font-medium 
                            text-white 
                            bg-indigo-500 
                            rounded-lg 
                            border border-gray-300 
                            ring-indigo-300 
                            focus-visible:ring 
                            hover:bg-indigo-600 
                            dark:bg-blue-600 
                            dark:hover:bg-indigo-700 
                            dark:focus:ring-indigo-800">
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="white">
          <path d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h240l80 80h320q33 0 56.5 23.5T880-640H447l-80-80H160v480l96-320h684L837-217q-8 26-29.5 41.5T760-160H160Zm84-80h516l72-240H316l-72 240Zm0 0 72-240-72 240Zm-84-400v-80 80Z"/>
        </svg>
      </button>
    </>
  );
}
const Check:React.FC<Prop> = ({prefix,element}) => {
  const [checked, setChecked] = useState(false);
  useEffect(() => {
    setChecked(element.value == "true")
  },[element])
  return (
    <div>
      <label htmlFor={prefix + '_' + element.name} className="block 
                                         mb-2 
                                         text-sm text-gray-900 
                                         font-medium 
                                         dark:text-gray-300">-{prefix && prefix + '.'}{element.name}</label>
      <input name={prefix ? '-' + prefix + '.' + element.name : '-' + element.name} id={prefix + '_' + element.name} type="checkbox" className="w-4 h-4 
                                                                      text-indigo-500 
                                                                      bg-gray-50 
                                                                      border border-gray-300 
                                                                      ring-indigo-300 
                                                                      focus-visible:ring 
                                                                      dark:bg-blue-600 
                                                                      dark:hover:bg-indigo-700 
                                                                      dark:focus:ring-indigo-800"
             checked={checked} value={`${checked}`}
             onChange={() => {setChecked(!checked)}}/>
      <input name={prefix ? '-' + prefix + '.' + element.name : '-' + element.name} id={prefix + '_' + element.name + 'hidden'} type="hidden" value={`${checked}`} />
    </div>
  );
}
const Select:React.FC<SelectProp> = ({handleTypeSelect ,prefix ,element}) => {
  const [selected, setSelected] = useState("");
  useEffect(()=>{setSelected(element.value)},[element])
  return (
    <div>
      <label htmlFor={prefix + '_' + element.name} className="block
                                        mb-2 
                                        text-sm text-gray-900
                                        font-medium 
                                        dark:text-white">-{prefix && prefix + '.'}{element.name}{element.attribute.required && '*'}</label>
      <select name={prefix ? '-' + prefix + '.' + element.name : '-' + element.name} id={prefix + '_' + element.name} className="block 
                                                     w-40 
                                                     p-2.5 
                                                     z-20 
                                                     bg-gray-50 
                                                     text-sm text-gray-900
                                                     rounded-lg 
                                                     border border-gray-300 
                                                     ring-indigo-300 
                                                     focus:ring 
                                                     focus-visible:ring 
                                                     dark:bg-gray-700 
                                                     dark:border-gray-600 
                                                     dark:placeholder-gray-400 
                                                     dark:text-white 
                                                     dark:focus:ring-blue-500 
                                                     dark:focus:border-blue-500" 
                                                     required
                                                     value={selected}
                                                     onChange={(event) => {setSelected(event.currentTarget.value);handleTypeSelect && handleTypeSelect();}}
                                                     >
        {element.attribute.selectOption.map((value) => {
          return <option key={prefix + element.name + value} value={value}>{value}</option>
        })};
      </select>
    </div> 
);
}
export default Forms;