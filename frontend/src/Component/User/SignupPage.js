import React, { useState } from "react";
import styled from "styled-components";

const SignupPage = () => {
  const [username, setUsername] = useState("");
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [resultMessage, setResultMessage] = useState("");
  const [isUsernameAvailable, setIsUsernameAvailable] = useState(true);
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(true);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "username") {
      setUsername(value);
      setIsUsernameAvailable(true); // Reset availability when username changes
    } else if (name === "nickname") {
      setNickname(value);
      setIsNicknameAvailable(true); // Reset availability when nickname changes
    } else if (name === "email") {
      setEmail(value);
    } else if (name === "password") {
      setPassword(value);
    } else if (name === "passwordCheck") {
      setPasswordCheck(value);
    }
  };

  const handleUsernameDuplicateCheck = () => {
    // Implement logic for checking username duplication
    // Example fetch call:
    // fetch(`/api/checkUsername?username=${username}`)
    //   .then(response => response.json())
    //   .then(data => {
    //     setIsUsernameAvailable(data.isAvailable);
    //     setResultMessage(data.message);
    //   })
    //   .catch(error => {
    //     console.error('Error:', error);
    //   });
  };

  const handleNicknameDuplicateCheck = () => {
    // Implement logic for checking nickname duplication
    // Example fetch call:
    // fetch(`/api/checkNickname?nickname=${nickname}`)
    //   .then(response => response.json())
    //   .then(data => {
    //     setIsNicknameAvailable(data.isAvailable);
    //     setResultMessage(data.message);
    //   })
    //   .catch(error => {
    //     console.error('Error:', error);
    //   });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Add logic for handling form submission
    // Example fetch call:
    // fetch('/api/signup', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //   },
    //   body: JSON.stringify({ username, nickname, email, password, passwordCheck }),
    // })
    //   .then(response => response.json())
    //   .then(data => {
    //     // Handle response data
    //     setResultMessage(data.resultMessage);
    //   })
    //   .catch(error => {
    //     console.error('Error:', error);
    //   });
  };

  return (
    <Container>
      <ContentWrapper>
        <p style={{ fontWeight: "bold", fontSize: "25px" }}>회원가입</p>
        <Id_InputWrapper>
          아이디
          <span>
            <Id_Input
              id="id"
              type="text"
              placeholder="ID를 입력해주세요."
              //   value={id}
            />
            <ID_Duplicate_CheckBtn>중복확인</ID_Duplicate_CheckBtn>
          </span>
        </Id_InputWrapper>
        <Nickname_InputWrapper>
          닉네임
          <Nickname_Input
            Nickname="Nickname"
            type="text"
            placeholder="닉네임을 입력하세요."
            //   value={Nickname}
          />
          <Nickname_Duplicate_CheckBtn>중복확인</Nickname_Duplicate_CheckBtn>
        </Nickname_InputWrapper>
        <Email_InputWrapper>
          이메일
          <Email_Input
            Nickname="Nickname"
            type="text"
            placeholder="닉네임을 입력하세요."
            //   value={Nickname}
          />
        </Email_InputWrapper>
        <Password_InputWrapper>
          비밀번호
          <Password_Input
            Nickname="password"
            type="text"
            placeholder="비밀번호를 입력해주세요."
            //   value={password}
          />
        </Password_InputWrapper>
        <Passwordcheck_InputWrapper>
          비밀번호 확인
          <Passwordcheck_Input
            Nickname="passwordcheck"
            type="text"
            placeholder="비밀번호를 한 번 더 입력해주세요."
            //   value={password}
          />
        </Passwordcheck_InputWrapper>
        <SignupBtn>회원가입</SignupBtn>
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
const Id_InputWrapper = styled.div`
  display: flex;

  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Id_Input = styled.input`
  flex: 1;
  position: relative;
  background-color: #f0f0f0;
  border: none;
  outline: none;
  padding: 10px;
  width: 30vh;
  border-radius: 20px;

  &:focus {
    background-color: #cccccc;
  }
`;

const ID_Duplicate_CheckBtn = styled.button`
  position: absolute;
  margin-left: 10px;
  padding: 8px 12px;
  border: 1px solid #666;
  background-color: #fff;
  color: #333;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;

  &:hover {
    background-color: #666;
    color: #fff;
    border-color: #fff;
  }
`;

const Nickname_Duplicate_CheckBtn = styled.button`
  margin-left: 10px;
  padding: 8px 12px;
  border: 1px solid #666;
  background-color: #fff;
  color: #333;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;

  &:hover {
    background-color: #666;
    color: #fff;
    border-color: #fff;
  }
`;

const Nickname_InputWrapper = styled.div`
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
  width: 30vh;
  border-radius: 20px;

  &:focus {
    background-color: #cccccc;
  }
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
  width: 30vh;
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
  width: 30vh;
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
  width: 30vh;
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
  width: 30vh;
  height: 40px;
  border: 2px solid #ffffff;
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
    border-width: 1px;
  }
`;

const IsNotUser = styled.div`
  display: "flex";
  align-items: "center";
`;

export default SignupPage;
