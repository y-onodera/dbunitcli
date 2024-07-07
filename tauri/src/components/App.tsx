import Form from "./app/Form";
import Forms from "./app/Forms";
import Footer from "./app/Footer";
import { useSelectParameter } from "../context/SelectParameterProvider";
import "../App.css";

export default function App() {
  const parameter = useSelectParameter();
  return (
    <Form>
      <Forms {...parameter} />
      <Footer {...parameter} />
    </Form>
  );
}