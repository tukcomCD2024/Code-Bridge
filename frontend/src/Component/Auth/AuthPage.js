import React from "react";
import styled from "styled-components";
import veryBigEye from "../../image/veryBigEye.gif";
import book1 from "../../image/book1.gif";
import { useNavigate } from "react-router-dom";

function AuthPage() {
  const navigate = useNavigate();

  return (
    <FlexContainer>
      <ImageWrapper>
        <BigEyeImage src={veryBigEye} alt="Very Big Eye" />
        <BookImage src={book1} alt="Book" />
      </ImageWrapper>
      <AuthBox>
        <ToMain
          style={{ fontSize: "30px", fontWeight: "bold", marginBottom: "30px" }}
          onClick={() => navigate("/main")}
        >
          ShareNote
        </ToMain>
        <LoginBtn onClick={() => navigate("/login")}>로그인</LoginBtn>
        <SignupBtn onClick={() => navigate("/signup")}>회원가입</SignupBtn>
      </AuthBox>
    </FlexContainer>
  );
}

export default AuthPage;

const FlexContainer = styled.div`
  display: flex;
  align-items: stretch;
  height: 100vh;
  background-color: #f5f5f5;
`;

const ImageWrapper = styled.div`
  flex: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #ddd;
  position: relative; /* 이를 relative로 설정 */
`;

const AuthBox = styled.div`
  flex: 1;
  background-color: #ffffff;
  border: 10px double #f5f5f5;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

const ToMain = styled.div`
  cursor: pointer;

  &:hover {
    color: #202632;
    text-decoration: underline;
  }
`;

const LoginBtn = styled.button`
  background-color: #0064ff;
  color: #ffffff;
  border: 1px solid #000000;
  border-radius: 10px;
  font-weight: bold;
  font-size: 14px;
  padding: 10px;
  cursor: pointer;

  &:hover {
    background-color: #000000;
    color: #f5f5f5;
  }
`;

const SignupBtn = styled.button`
  background-color: #f5f5f5;
  border: 1px solid #000000;
  border-radius: 10px;
  font-weight: bold;
  font-size: 14px;
  margin-top: 10px;
  padding: 10px;
  cursor: pointer;

  &:hover {
    background-color: #000000;
    color: #f5f5f5;
  }
`;

const BigEyeImage = styled.img`
  width: 300px;
  height: auto;
  position: absolute; /* 이 이미지를 absolute로 설정 */
  left: 50%; /* 중앙 정렬을 위해 */
  top: 48%; /* 중앙 정렬을 위해 */
  transform: translate(-50%, -50%); /* 정확히 중앙에 위치하도록 조정 */

  @media screen and (max-width: 768px) {
    flex-direction: column;
    gap: 0px;
    width: 80%;
  }
`;

const BookImage = styled.img`
  width: 600px;
  height: auto;
  position: absolute; /* 이 이미지도 absolute로 설정 */
  left: 50%; /* 중앙 정렬을 위해 */
  top: 40%; /* 중앙 정렬을 위해 */
  transform: translate(-50%, -50%); /* 정확히 중앙에 위치하도록 조정 */

  @media screen and (max-width: 768px) {
    display: none;
  }
`;
