import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const SignupPage = () => {
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [isEmailAvailable, setIsEmailAvailable] = useState(true);
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(true);
  const [resultMessage, setResultMessage] = useState(""); // 상태 추가

  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "email") {
      setEmail(value);
      setIsEmailAvailable(true); // 이메일 변경 시 사용 가능 여부 재설정
    } else if (name === "nickname") {
      setNickname(value);
      setIsNicknameAvailable(true); // 닉네임 변경 시 사용 가능 여부 재설정
    } else if (name === "password") {
      setPassword(value);
    } else if (name === "passwordCheck") {
      setPasswordCheck(value);
    }
  };

  const handleEmailDuplicateCheck = () => {
    if (email === "") {
      alert("이메일를 입력하세요.");
      return;
    }
    fetch(`/user/signup?email=${email}`)
      .then((response) => response.json())
      .then((data) => {
        if (data.isAvailable === false) {
          // 중복된 닉네임이 있음을 사용자에게 알림
          alert("이메일의 닉네임이 존재합니다.");
        } else {
          setIsEmailAvailable(data.isAvailable);
          setResultMessage(data.message);
        }
      })
      .catch((error) => {
        console.error("Error:", error);
        alert("처리에 실패하였습니다.");
      });
  };

  const handleNicknameDuplicateCheck = () => {
    if (nickname === "") {
      alert("닉네임을 입력하세요.");
      return;
    }
    fetch(`/user/signup?nickname=${nickname}`)
      .then((response) => response.json())
      .then((data) => {
        if (data.isAvailable === false) {
          // 중복된 닉네임이 있음을 사용자에게 알림
          alert("이미 가입된 닉네임이 존재합니다.");
        } else {
          setIsNicknameAvailable(true);
          setResultMessage(data.message);
        }
      })
      .catch((error) => {
        console.error("Error:", error);
        alert("처리에 실패하였습니다.");
      });
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); // 폼 제출 기본 동작 방지

    if (
      password === "" ||
      passwordCheck === "" ||
      email === "" ||
      nickname === ""
    ) {
      alert("모든 칸을 빠짐없이 입력해주세요.");
      return;
    } else if (password !== passwordCheck) {
      alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    try {
      const response = await fetch("/api/user/signUp", {
        // URL 경로 확인 필요 ("/api/user/signUp" 가 정확한지 확인)
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, nickname, password }), // 직접적으로 데이터 전송
      });
      if (response.ok) {
      
        alert("회원가입 성공! 로그인을 진행해주세요.");
        navigate("/login");
      } else {
        // 서버 응답에서 반환된 에러 메시지를 사용하여 사용자에게 보다 구체적인 정보 제공
        const errorData = await response.json();
        alert(`회원가입 실패: ${errorData.message}`);
      }
    } catch (error) {
      console.error("Error:", error);
      alert("처리에 실패하였습니다.");
    }
  };

  return (
    <Container>
      <ContentWrapper>
        <p style={{ fontWeight: "bold", fontSize: "25px" }}>회원가입</p>
        <Email_InputWrapper>
          이메일(ID)
          <Email_Input
            name="email"
            type="text"
            placeholder="이메일을 입력하세요."
            onChange={handleInputChange} // 이 부분을 추가
            value={email}
          />
          <Email_Duplicate_CheckBtn onClick={handleEmailDuplicateCheck}>
            중복확인
          </Email_Duplicate_CheckBtn>
        </Email_InputWrapper>
        <Nickname_InputWrapper>
          닉네임
          <Nickname_Input
            name="nickname"
            type="text"
            placeholder="닉네임을 입력하세요."
            onChange={handleInputChange}
            value={nickname}
          />
          <Nickname_Duplicate_CheckBtn onClick={handleNicknameDuplicateCheck}>
            중복확인
          </Nickname_Duplicate_CheckBtn>{" "}
        </Nickname_InputWrapper>
        <Password_InputWrapper>
          비밀번호
          <Password_Input
            name="password" // 올바른 name 속성 값으로 변경
            type="password"
            placeholder="비밀번호를 입력해주세요."
            value={password}
            onChange={handleInputChange} // 이 부분을 추가
          />
        </Password_InputWrapper>
        <Passwordcheck_InputWrapper>
          비밀번호 확인
          <Passwordcheck_Input
            name="passwordCheck" // 올바른 name 속성 값으로 변경
            type="password"
            placeholder="비밀번호를 한 번 더 입력해주세요."
            value={passwordCheck}
            onChange={handleInputChange} // 이 부분을 추가}
          />
        </Passwordcheck_InputWrapper>
        <SignupBtn onClick={handleSubmit}>회원가입</SignupBtn>
        <HomeBtn onClick={() => navigate("/")}>
          <small>홈으로 돌아가기</small>
        </HomeBtn>
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
  position: relative;
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

const Email_Duplicate_CheckBtn = styled.button`
  position: absolute;
  top: 46px;
  right: 5px;
  margin-left: 10px;
  padding: 5px 5px;
  font-size: 11px;
  border: 1px solid #666;
  background-color: #fff;
  color: #333;
  cursor: pointer;
  border-radius: 50px;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;

  &:hover {
    background-color: #666;
    color: #fff;
    border-color: #fff;
  }
`;

const Nickname_Duplicate_CheckBtn = styled.button`
  position: absolute;
  top: 46px;
  right: 5px;
  margin-left: 10px;
  padding: 5px 5px;
  font-size: 11px;
  border: 1px solid #666;
  background-color: #fff;
  color: #333;
  cursor: pointer;
  border-radius: 50px;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;

  &:hover {
    background-color: #666;
    color: #fff;
    border-color: #fff;
  }
`;

const Nickname_InputWrapper = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Nickname_Input = styled.input`
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

const Passwordcheck_InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Passwordcheck_Input = styled.input`
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

const SignupBtn = styled.button`
  display: flex;
  flex-direction: column;
  margin: 20px auto; /* Auto margin for centering horizontally */
  margin-bottom: 0px;
  width: 250px;
  height: 40px;
  border: 0px solid #ffffff;
  border-radius: 1px;
  background-color: #ffffcc;
  text-align: center;
  align-items: center;
  line-height: 40px;
  font-size: 16px;
  color: #000000;
  cursor: pointer;
  border-radius: 20px;
  border-color: #ffffcc;

  &:hover {
    background-color: #f7f7b5;
  }
`;

const IsNotUser = styled.div`
  display: "flex";
  align-items: "center";
`;

const HomeBtn = styled.span`
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

export default SignupPage;