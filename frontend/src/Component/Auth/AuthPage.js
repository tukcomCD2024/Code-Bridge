import React, { useEffect } from "react";
import styled from "styled-components";
import veryBigEye from "../../image/veryBigEye.gif";
import book from "../../image/book2.gif";
import { useNavigate } from "react-router-dom";

function AuthPage() {
  const navigate = useNavigate();

    useEffect(() => {
      localStorage.clear();
    }, []);

  return (
    <FlexContainer>
      <ImageWrapper>
        {/* <BigEyeImage src={veryBigEye} alt="Very Big Eye" /> */}
        <BookImage src={book} alt="Book" />
        <CommentWrapper>
          <p>Let's Share with me!!</p>
        </CommentWrapper>
      </ImageWrapper>
      <AuthBox>
        <ToMain>
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
  // background-color: #FFFFE0;
`;

const ImageWrapper = styled.div`
  flex: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #FFFFE0;
  position: relative; 
`;

const CommentWrapper = styled.div`
  width: 50%;
  margin-top: 30%;
  font-size: 25px;
  font-weight: 600;
  color: #FF8C00;
`;

const AuthBox = styled.div`
  flex: 1;
  background-color: #ffffff;
  border: 10px double #f5f5f5;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const ToMain = styled.div`
  font-size: 30px;
  font-weight: bold;
  margin-bottom: 30px;
`;

const LoginBtn = styled.button`
  width: 100%;
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
  width: 100%;
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
  position: absolute; 
  left: 50%; 
  top: 48%; 
  transform: translate(-50%, -50%);

  @media screen and (max-width: 768px) {
    flex-direction: column;
    gap: 0px;
    width: 80%;
  }
`;

const BookImage = styled.img`
  width: 53%;
  height: auto;
  position: absolute;
  left: 50%; 
  top: 40%; 
  transform: translate(-50%, -50%) rotate(20deg);

  @media screen and (max-width: 768px) {
    display: none;
  }
`;
