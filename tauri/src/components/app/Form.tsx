import { ReactNode } from "react";

const formid="commandForm"
export function formData(validate:boolean){
    const formElement = document.querySelector('#'+formid) as HTMLFormElement;
    if(validate && !formElement.reportValidity()) {
      return {validationError:true}
    }
    const formData = new FormData(formElement);
    return Object.fromEntries(formData.entries());
}
export default function Form(props:{children:ReactNode}){
  return (
    <>
      <div className="p-4 rounded-lg mt-14">
        <form id={formid} className="grid gap-6 mb-6 grid-cols-1" noValidate
          onSubmit={(e) => {
            e.preventDefault();
          }}
        >
          {props.children}
        </form>
      </div>
    </>
  );
}  