import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import useMediaQuery from './Component/Utils/useMediaQuery';
import LoadingScreen from './Component/Utils/LoadingScreen';
import "./styles.css";

const AuthPage = lazy(() => import('./Component/Auth/AuthPage'));
const LoginPage = lazy(() => import('./Component/Auth/LoginPage'));
const SignupPage = lazy(() => import('./Component/Auth/SignupPage'));
const UserProfileEdit = lazy(() => import('./Component/Auth/UserProfileEdit'));
const OrganizationBar = lazy(() => import('./Component/Organization/Organization_Web'));
const OrganizationForMobile = lazy(() => import('./Component/Organization/Organization_Mobile'));
const NotePage = lazy(() => import('./Component/Note/NotePage'));
const Page = lazy(() => import('./Component/Page/Page'));
const Contribution = lazy(() => import('./Component/Contribution/ContributionPage'));
const EmailTokenHandler = lazy(() => import('./Component/Utils/EmailTokenHandler'));

export default function App() {
  const isMobile = useMediaQuery("(max-width: 768px)");

  return (
    <div className="App">
      <BrowserRouter>
        <Suspense fallback={<LoadingScreen />}>
          <Routes>
            <Route path="/" element={<AuthPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/editProfile" element={<UserProfileEdit />} />
            <Route path="/organization/*" element={isMobile ? <OrganizationForMobile /> : <OrganizationBar />} />
            <Route path="/organization/:id/*" element={<NotePage />} />
            <Route path="/organization/:id/:noteId/:pageId" element={<Page />} />
            <Route path="/organization/invitation/approve" element={<EmailTokenHandler />} />
            <Route path="/contribution/:id" element={<Contribution />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </div>
  );
}