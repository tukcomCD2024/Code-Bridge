import "./styles.css";
import AuthPage from "./Component/Auth/AuthPage";
import Mainpage from "./Component/Mainpage/MainPage";
import LoginPage from "./Component/Auth/LoginPage";
import SignupPage from "./Component/Auth/SignupPage";
import NotePage from "./Component/Note/NotePage";
import UserProfileEdit from "./Component/Auth/UserProfileEdit";
import Page from "./Component/Page/Page";
import Contribution from "./Component/Contribution/ContributionPage";
import EmailTokenHandler from "./Component/Utils/EmailTokenHandler";
import { BrowserRouter, Routes, Route  } from "react-router-dom";

export default function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<AuthPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/main/*" element={<Mainpage />} />
          <Route path="/editProfile" element={<UserProfileEdit />} />
          <Route path="/organization/invitation/approve" element={<EmailTokenHandler />} />
          <Route path="/organization/:id/*" element={<NotePage />} /> 
          <Route path="/organization/:id/:noteId/:pageId" element={<Page />} /> 
          {/* <Route path="/contribution/:id" element={<Contribution />} />  */}
        </Routes>
      </BrowserRouter>
    </div>
  );
}
