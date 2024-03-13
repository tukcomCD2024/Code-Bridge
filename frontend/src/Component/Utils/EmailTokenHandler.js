import React, { useEffect } from 'react';
import { useLocation, Navigate } from 'react-router-dom';
import toastr from "toastr";
import "toastr/build/toastr.css";

const isLoggedIn = () => {
  const userId = localStorage.getItem('userId');
  return !!userId; // userId가 있으면 true, 없으면 false 반환
};

const EmailTokenHandler = () => {
  const location = useLocation();

  useEffect(() => {
    // URL에서 token 값을 읽어옵니다.
    const urlParams = new URLSearchParams(location.search);
    const token = urlParams.get('token');

    if (token) { 
      localStorage.setItem('token', token);
    }

    if (isLoggedIn){
      fetchEmailInvitationToken();
    }
  }, [location]);

  const fetchEmailInvitationToken = async () => {
    try {
      const userId = localStorage.getItem('userId');
      const token = localStorage.getItem('token');
      console.log(userId);
      console.log(token);
      const response = await fetch("/api/user/organization/invitation/accept", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ userId, token }),
      });

      const contentType = response.headers.get('content-type');

      if (response.ok) {
        if (contentType && contentType.includes('text/plain')) {
          localStorage.removeItem("token");
          const responseMessage = await response.text();
          toastr.success(responseMessage);
        }
      }
    } catch (error) {
      console.error("Error: ", error);
      localStorage.removeItem("token");
      toastr.error("에러가 발생했습니다.");
    }
  }

  if (isLoggedIn()) {
    return <Navigate to="/main" />;
  } else {
    toastr.info("로그인 후 초대가 자동 수락됩니다.");
    return <Navigate to="/login" />;
  }
};

export default EmailTokenHandler;
