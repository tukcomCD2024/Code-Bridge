import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import GoogleLoginBtn from "../../image/googleLoginBtn.png";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [resultMessage, setResultMessage] = useState("");
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "email") {
      setEmail(value);
    } else if (name === "password") {
      setPassword(value);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password === "" || email === "") {
      alert("이메일(ID)과 비밀번호를 모두 입력해주세요.");
      return;
    }

    try {
      const response = await fetch("/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        const { name, userId } = data;
        localStorage.setItem("userId", userId); // 백엔드로부터 받은 유저 (고유)아이디
        localStorage.setItem("email", email); // 로그인한 아이디
        console.log(userId);
        navigate("/main");
      } else {
        const errorData = await response.json();
        alert(`로그인 실패: ${errorData.message}`);
      }
    } catch (error) {
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <Container>
      <ContentWrapper>
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
        <LoginBtn onClick={handleSubmit}>로그인</LoginBtn>
        <IsNotUser>
          <p style={{ display: "inline", margin: "0", marginRight: "8px" }}>
            <small>회원이 아니신가요?</small>
          </p>
          <SignupBtn onClick={() => navigate("/signup")}>
            <small>회원가입하기</small>
          </SignupBtn>
        </IsNotUser>
        <GoogleLoginBtnContainer>
          <GoogleLoginImg
            src={GoogleLoginBtn}
            alt="Google Login Button"
            onClick={() => navigate("/signup")}
          />
        </GoogleLoginBtnContainer>
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
  align-items: center;

  background-color: rgba(138, 43, 226, 0.2);
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
  background-color: #ffffcc;
  text-align: center;
  align-items: center;
  line-height: 40px;
  font-size: 16px;
  color: #000000;
  cursor: pointer;
  border-radius: 20px;

  &:hover {
    background-color: #f7f7b5;
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

const GoogleLoginBtnContainer = styled.div`
  margin-top: 10px; // Adjust the margin as needed
`;

const GoogleLoginImg = styled.img`
  margin-top: 10px;
  width: 300px;
  height: auto; // Maintain the aspect ratio
  cursor: pointer; // Add cursor pointer for interaction
`;

export default LoginPage;
