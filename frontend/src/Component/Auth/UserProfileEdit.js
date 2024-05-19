import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import cameraIcon from "../../image/camera_icon.png";

const UserProfileEdit = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [resultMessage, setResultMessage] = useState("");
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "username") {
      setUsername(value);
    } else if (name === "password") {
      setPassword(value);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Add logic for handling form submission
    // Example fetch call:
    // fetch('/userController?action=login', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //   },
    //   body: JSON.stringify({ username, password }),
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
        <p style={{ fontWeight: "bold", fontSize: "25px" }}>내 정보 수정</p>
        <ProfilePicture>
          <CameraIcon src={cameraIcon} alt="Edit profile picture" />
        </ProfilePicture>
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
        <Id_InputWrapper>
          이메일(ID)
          <Id_Input
            id="id"
            type="text"
            placeholder="이메일을 입력하세요."
            //   value={id}
          />
          <ID_Duplicate_CheckBtn>중복확인</ID_Duplicate_CheckBtn>
        </Id_InputWrapper>
        <Password_InputWrapper>
          비밀번호
          <Password_Input
            id="password"
            type="password"
            placeholder="비밀번호를 입력해주세요."
            //   value={password}
          />
        </Password_InputWrapper>
        <Passwordcheck_InputWrapper>
          비밀번호 확인
          <Passwordcheck_Input
            Nickname="passwordcheck"
            type="password"
            placeholder="비밀번호를 한 번 더 입력해주세요."
            //   value={password}
          />
        </Passwordcheck_InputWrapper>
        <EditBtn>수정하기</EditBtn>
        <HomeBtn onClick={() => navigate("/main")}>
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

const Id_InputWrapper = styled.div`
  display: flex;
  position: relative;
  flex-direction: column;
  align-items: flex-start;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
  border-radius: 10px;
`;

const Id_Input = styled.input`
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

const ID_Duplicate_CheckBtn = styled.button`
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
  width: 30vh;
  border-radius: 20px;

  &:focus {
    background-color: #cccccc;
  }
`;

const EditBtn = styled.button`
  display: flex;
  flex-direction: column;
  margin: 20px auto; /* Auto margin for centering horizontally */
  margin-bottom: 0px;
  width: 30vh;
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

const ProfilePicture = styled.div`
  position: relative;
  width: 80px;
  height: 80px;
  background-color: #e0e0e0;
  border-radius: 50%;
  margin: 20px auto;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const CameraIcon = styled.img`
  position: absolute;
  bottom: 0;
  left: 0;
  width: 25px;
  height: 25px;
  margin: 0px;
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

export default UserProfileEdit;
