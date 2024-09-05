import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import toastr from "toastr";
import "toastr/build/toastr.css";

import GoogleLoginBtn from "../../image/googleLoginBtn.png";
import NaverLoginBtn from "../../image/naverLoginBtn.png";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "email") {
      setEmail(value);
    } else if (name === "password") {
      setPassword(value);
    }
  };

  const fetchEmailInvitationToken = async (userId, token) => {
    try {
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
          const responseMessage = await response.text();
          console.log(responseMessage);
          toastr.remove();
          toastr.success("<strong>초대 수락 완료!</strong> <br/>확인 불가 시, 새로고침하세요.");
        }
      } else {
        const errorMessage = await response.text();
        console.log(errorMessage);
        toastr.remove();
        toastr.error("<strong>초대 수락 실패!</strong> <br/>초대장 버튼을 다시 누르세요.");
      }
    } catch (error) {
      console.error("Error: ", error);
    } finally {
      localStorage.removeItem("token");
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password === "" || email === "") {
      toastr.remove();
      toastr.error("<strong>로그인 실패!</strong><br/>모든 칸을 입력하세요.");
      return;
    }
  
    try {
      const token = localStorage.getItem('token');
      const response = await fetch("/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password, token }),
      });
  
      // Content-Type 헤더를 체크하여 응답 타입 판별
      const contentType = response.headers.get('content-type');
      const access = response.headers.get('access');
      const refresh = response.headers.get('refresh');

      if (response.ok) {
        if (contentType && contentType.includes('application/json')) {
          const data = await response.json();
          const { name, userId } = data;
          localStorage.setItem("userId", userId); // 백엔드로부터 받은 유저 (고유)아이디
          localStorage.setItem("nickname", name); // 백엔드로부터 받은 유저 닉네임
          localStorage.setItem("email", email); // 로그인 성공 시 이메일 저장(백엔드에서 받은게 아님)
          localStorage.setItem("access", access); // accessToken 저장
          localStorage.setItem("refresh", refresh); // refreshToken 저장
          if (token != undefined){
            fetchEmailInvitationToken(userId, token);
          }
          navigate("/organization");
        }
      } else {
        // 에러 응답 처리
        if (response.status === 401) {
          toastr.remove();
          toastr.error("<strong>로그인 실패!</strong><br/>존재하지 않은 계정입니다.");
        } else if (contentType && contentType.includes('text/plain')) {
          // 응답이 텍스트 형식인 경우
          const errorMessage = await response.text();
          alert(`로그인 실패: ${errorMessage}`);
        } else {
          alert("로그인에 실패했습니다. 다시 시도해주세요.");
        }
      }
    } catch (error) {
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };

  // google login 버튼 클릭 함수
  const handleGoogleLoginClick = () => {
    window.location.href = "/api/oauth2/authorization/google";
  };

  // naver login 버튼 클릭 함수
  const handleNaverLoginClick = () => {
    window.location.href = "/api/oauth2/authorization/naver";
  };

  return (
    <Container>
    <ContentWrapper>
      <form onSubmit={handleSubmit}> {/* 폼 요소 추가 */}
        <p style={{ fontWeight: "bold", fontSize: "25px" }}>로그인</p>
        <Email_InputWrapper>
          이메일
          <Email_Input
            name="email"
            type="text"
            placeholder="이메일(ID)를 입력해주세요."
            onChange={handleInputChange}
            value={email}
          />
        </Email_InputWrapper>
        <Password_InputWrapper>
          비밀번호
          <Password_Input
            name="password"
            type="password"
            placeholder="비밀번호를 입력해주세요."
            onChange={handleInputChange}
            value={password}
          />
        </Password_InputWrapper>
        <LoginBtn type="submit">로그인</LoginBtn>
      </form>
      <IsNotUser>
        <p style={{ display: "inline", margin: "0", marginRight: "8px" }}>
          <small>회원이 아니신가요?</small>
        </p>
        <SignupBtn onClick={() => navigate("/signup")}>
          <small>회원가입하기</small>
        </SignupBtn>
      </IsNotUser>
      <br />
      <SocialLoginBtnContainer>
        <SocialLoginImg
          src={GoogleLoginBtn}
          alt="Google Login Button"
          onClick={handleGoogleLoginClick} // onClick 이벤트 핸들러 설정
        />
        <SocialLoginImg
          src={NaverLoginBtn}
          alt="handleNaverLoginClick"
          onClick={handleNaverLoginClick} // onClick 이벤트 핸들러 설정
        />
      </SocialLoginBtnContainer>
    </ContentWrapper>
  </Container>
  );
};

// Styled components
const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 350px;
  padding: 2rem;
  padding-bottom: 1.2rem;
  align-items: center;
  // background-color: rgba(138, 43, 226, 0.2); // 보라색
  background-color: rgba(255, 250, 209, 1);
  border-radius: 10px;
  margin: 0 auto;
`;

const Email_InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Email_Input = styled.input`
  flex: 1;
  background-color: #f0f0f0;
  border: none;
  outline: none;
  padding: 10px;
  width: 300px;
  border-radius: 20px;

  &:focus {
    background-color: #cccccc;
  }
`;

const Password_InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Password_Input = styled.input`
  flex: 1;
  background-color: #f0f0f0;
  border: none;
  outline: none;
  width: 300px;
  padding: 10px;
  border-radius: 20px;

  &:focus {
    background-color: #cccccc;
  }
`;

const LoginBtn = styled.button`
  display: flex;
  flex-direction: column;
  margin: 20px auto; /* Auto margin for centering horizontally */
  margin-bottom: 0px;
  width: 250px;
  height: 40px;
  border: #ffffcc;
  border-radius: 1px;
  // background-color: #ffffcc;
  background-color: rgba(0, 100, 255, 0.7);
  text-align: center;
  align-items: center;
  line-height: 40px;
  font-size: 16px;
  color: #000000;
  cursor: pointer;
  border-radius: 20px;

  &:hover {
    // background-color: #f7f7b5;
      background-color: rgba(0, 100, 255, 0.9);
  }
`;

const IsNotUser = styled.div`
  display: "flex";
  align-items: "center";
`;

const SignupBtn = styled.span`
  display: "inline-block";
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  font-weight: bold;
  color: #0000ff;
  text-decoration: underline;

  &:hover {
    color: #000000;
  }
`;

const SocialLoginBtnContainer = styled.div`
  margin-top: 10px; // Adjust the margin as needed
  border-radius: 10px;
`;

const SocialLoginImg = styled.img`
  width: 300px;
  height: auto; // Maintain the aspect ratio
  cursor: pointer; // Add cursor pointer for interaction
`;

export default LoginPage;
