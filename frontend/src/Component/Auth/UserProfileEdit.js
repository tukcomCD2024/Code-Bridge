import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import cameraIcon from "../../image/camera_icon.png";

const UserProfileEdit = () => {
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
      setIsEmailValid(false);
    } else if (name === "nickname") {
      setNickname(value);
      setIsNicknameValid(false); // 닉네임 변경 시 유효성 재검증
    } else if (name === "password") {
      setPassword(value);
    } else if (name === "passwordCheck") {
      setPasswordCheck(value);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // 내 정보 수정 제출 시 동작 코드
  };

  return (
    <Container>
      <ContentWrapper>
      <form onSubmit={handleSubmit}>
        <p style={{ fontWeight: "bold", fontSize: "25px" }}>내 정보 수정</p>
        <ProfilePicture>
          <CameraIcon src={cameraIcon} alt="Edit profile picture" />
        </ProfilePicture>
        <Nickname_InputWrapper>
          닉네임
          <Nickname_Input
            name="nickname"
            type="text"
            placeholder="닉네임을 입력하세요."
            onChange={handleInputChange}
            value={nickname}
          />
          <Nickname_Duplicate_CheckBtn>중복확인</Nickname_Duplicate_CheckBtn>
        </Nickname_InputWrapper>
        <Id_InputWrapper>
          이메일(ID)
          <Id_Input
            name="email"
            type="text"
            placeholder="이메일을 입력하세요."
            onChange={handleInputChange}
            value={email}
          />
          <ID_Duplicate_CheckBtn>중복확인</ID_Duplicate_CheckBtn>
        </Id_InputWrapper>
        <Password_InputWrapper>
          비밀번호
          <Password_Input
            name="password"
            type="password"
            placeholder="비밀번호를 입력해주세요."
            autoComplete="new-password"
            onChange={handleInputChange}
            value={password}
          />
        </Password_InputWrapper>
        <Passwordcheck_InputWrapper>
          비밀번호 확인
          <Passwordcheck_Input
            name="passwordCheck"
            type="password"
            placeholder="비밀번호를 한 번 더 입력해주세요."
            autoComplete="new-password"
            onChange={handleInputChange}
            value={passwordCheck}
          />
        </Passwordcheck_InputWrapper>
        <EditBtn>수정하기</EditBtn>
        </form>
        <HomeBtn onClick={() => navigate("/organization")}>
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
  width: 300px;
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

const EditBtn = styled.button`
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