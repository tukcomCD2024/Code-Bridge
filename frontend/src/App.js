import "./styles.css";
import AuthPage from "./Component/Auth/AuthPage";
import Mainpage from "./Component/Mainpage/MainPage";
import LoginPage from "./Component/Auth/LoginPage";
import SignupPage from "./Component/Auth/SignupPage";
import { BrowserRouter, Routes, Route } from "react-router-dom";

export default function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<AuthPage />} />
          <Route path="/main" element={<Mainpage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/logout" element={<Mainpage />} />
          <Route path="/about" element={<Mainpage />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}
