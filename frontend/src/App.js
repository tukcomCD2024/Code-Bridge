import React from "react";

import "./styles.css";
import Mainpage from "./Component/Mainpage/MainPage";
import LoginPage from "./Component/User/LoginPage";
import SignupPage from "./Component/User/SignupPage";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import index from "./index"

export default function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Mainpage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/logout" element={<Mainpage />} />
          <Route path="/about" element={<Mainpage />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}
