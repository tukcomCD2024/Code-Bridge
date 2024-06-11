import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import toastr from "toastr";
import "toastr/build/toastr.css";

const SignupPage = () => {
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [isEmailValid, setIsEmailValid] = useState(false); // 이메일 유효성 검사 상태
  const [isNicknameValid, setIsNicknameValid] = useState(false); // 닉네임 유효성 검사 상태

  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "email") {
      setEmail(value);
      setIsEmailValid(false);      // 이메일 변경 시 유효성 재검증
    } else if (name === "nickname") {
      setNickname(value);
      setIsNicknameValid(false); // 닉네임 변경 시 유효성 재검증
    } else if (name === "password") {
      setPassword(value);
    } else if (name === "passwordCheck") {
      setPasswordCheck(value);
    }
  };

  const handleEmailDuplicateCheck = async () => {
    if (email === "") {
      alert("이메일을 입력하세요.");
      return;
    }
    try {
      const response = await fetch(`/api/user/uniqueEmail/${email}`, {
        method: "POST",
      });
      if (response.ok) {
        const isUnique = await response.json();
        if (isUnique) {
          setIsEmailValid(true);
          toastr.remove();
          toastr.info("사용 가능한 이메일 주소입니다.");
        } else {
          setEmail("");
          alert("입력하신 이메일의 계정이 이미 존재합니다.");
        }
      } else if (response.status === 404) {
        alert("서버에서 요청한 리소스를 찾을 수 없습니다.");
      } else {
        throw new Error(`[오류] 에러 코드: ${response.status}`);
      }
    } catch (error) {
      console.error("Error:", error);
      alert("처리에 실패하였습니다.");
    }
  };
  
  const handleNicknameDuplicateCheck = async () => {
    if (nickname === "") {
      alert("닉네임을 입력하세요.");
      return;
    }
    try {
        const response = await fetch(`/api/user/uniqueNickname/${nickname}`, {
          method: "POST",
        });
        if (response.ok) {
          const isUnique = await response.json();
          if (isUnique) {
            setIsNicknameValid(true);
            toastr.remove();
            toastr.info("사용 가능한 닉네임입니다.");
          } else {
            setNickname("");
            alert("입력하신 닉네임이 이미 존재합니다.");
          }
        } else if (response.status === 404) {
          alert("서버에서 요청한 리소스를 찾을 수 없습니다.");
        } else {
          throw new Error(`[오류] 에러 코드: ${response.status}`);
        }
      } catch (error) {
        console.error("Error:", error);
        alert("처리에 실패하였습니다.");
      }
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

    if(!isEmailValid) {
      toastr.remove();
      toastr.error("이메일 중복확인 후 진행하세요.");
      return;
    }

    if(!isNicknameValid) {
      toastr.remove();
      toastr.error("닉네임 중복확인 후 진행하세요.");
      return;
    }

    try {
      const response = await fetch("/api/user/signUp", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, nickname, password }), // 직접적으로 데이터 전송
      });
      if (response.ok) {
        toastr.success("<strong>회원가입 성공!</strong><br/>로그인을 진행해주세요.");
        navigate("/login");
      } else {
        const errorData = await response.text();
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
        <form onSubmit={handleSubmit}>
          <p style={{ fontWeight: "bold", fontSize: "25px" }}>회원가입</p>
          <Email_InputWrapper>
            이메일(ID)
            <Email_Input
              name="email"
              type="text"
              placeholder="이메일을 입력하세요."
              onChange={handleInputChange}
              value={email}
            />
            <Email_Duplicate_CheckBtn type="button" onClick={handleEmailDuplicateCheck}>
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
            <Nickname_Duplicate_CheckBtn type="button" onClick={handleNicknameDuplicateCheck}>
              중복확인
            </Nickname_Duplicate_CheckBtn>{" "}
          </Nickname_InputWrapper>
          <Password_InputWrapper>
            비밀번호
            <Password_Input
              name="password"
              type="password"
              placeholder="비밀번호를 입력해주세요."
              value={password}
              onChange={handleInputChange}
            />
          </Password_InputWrapper>
          <Passwordcheck_InputWrapper>
            비밀번호 확인
            <Passwordcheck_Input
              name="passwordCheck"
              type="password"
              placeholder="비밀번호를 한 번 더 입력해주세요."
              value={passwordCheck}
              onChange={handleInputChange}
            />
          </Passwordcheck_InputWrapper>
          <SignupBtn
            type="submit"
            style={{ cursor: !(isEmailValid && isNicknameValid && password && password === passwordCheck) ? 'not-allowed' : 'pointer'}}
          >회원가입</SignupBtn>
        </form>
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
