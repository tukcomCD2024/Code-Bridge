import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import styled from "styled-components";
import toastr from "toastr";
import "toastr/build/toastr.css";

const OrganizationInfoModal = ({
  modalOpen,
  handleCloseModal,
  organization,
}) => {
  const modalRef = useRef();
  const navigate = useNavigate();

  const [userEmailInput, setUserEmailInput] = useState(""); // 사용자 이메일 입력 상태 관리
  const [isSendButtonDisabled, setIsSendButtonDisabled] = useState(false);

  const { id } = useParams();
  const organizationId = String(id);

  // 이메일 입력 처리 함수
  const handleEmailInputChange = (event) => {
    setUserEmailInput(event.target.value);
  };

  // 이메일 형식 검증 함수
  const validateEmail = (email) => {
    const re =
      /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
  };

  // 이메일 전송 처리 함수
  const handleSendInvitation = async () => {
    const nickname = localStorage.getItem('nickname');

    // 이메일 형식 검증
    if (!validateEmail(userEmailInput)) {
      toastr.remove();
      toastr.error("유효하지 않는 이메일입니다.");
      return;
    }

    const endpoint = "/api/user/organization/invitation";
    try {
    setIsSendButtonDisabled(true);
    toastr.info("잠시만 기다려주세요...");
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email: userEmailInput, nickname, organizationId }), // 입력된 이메일 데이터를 JSON 형태로 변환하여 전송
      });

      if (!response.ok) throw new Error("Network response was not ok.");
      toastr.remove();
      toastr.options.positionClass = "toast-top-right";
      toastr.success("초대 메일이 성공적으로 전송되었습니다.");
      setUserEmailInput("");
      setIsSendButtonDisabled(false);
    } catch (error) {
      toastr.clear();
      toastr.options.positionClass = "toast-top-right";
      toastr.error("초대 메일 전송에 실패했습니다.");
      setIsSendButtonDisabled(false);
    }
  };

  const handleKeyPress = (event) => {
    if (event.key === "Enter") {
      handleSendInvitation();
    }
  };

  useEffect(() => {
    if (organization?.name == null) {
      toastr.options.positionClass = "toast-top-right";
      toastr.info("정보를 불러오지 못했습니다.");
      navigate("/main");
    }
  }, [organization, navigate]);

  // 모달 바깥 클릭시 닫기 로직
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        handleCloseModal();
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [handleCloseModal]);

  if (!modalOpen) return null; // 모달이 열리지 않았다면 렌더링하지 않음

  // 추방 버튼 클릭 핸들러 함수
  const handleKickUser = (nickname) => {
    alert(`${nickname}을(를) 추방하려고 합니다.`);
  };

  return (
    <ModalContainer>
      <ModalContent ref={modalRef}>
        <CloseButton onClick={handleCloseModal} style={{ color: "red" }}>
          X
        </CloseButton>
        <LeftContainer>
          <p>현재 Organization</p>
          <LeftInsideContainer>
            <p style={{ fontWeight: "bold", fontSize: "25px", whiteSpace: "nowrap", overflow: "hidden", textOverflow:"ellipsis",maxWidth: "270px" }}>
              {organization?.name || "조직 정보 없음"}
            </p>
            <p style={{ fontSize: "100px" }}>{organization?.emoji || ""}</p>
          </LeftInsideContainer>
        </LeftContainer>
        <RightContainer>
          <TopContainer>
            <Title>&nbsp;&nbsp;📪 멤버 초대하기</Title>
            <InvitationInputWrapper>
              <InvitationInput
                type="text"
                value={userEmailInput} // input value 바인딩
                onKeyPress={handleKeyPress}
                onChange={handleEmailInputChange} // 입력 값 변경 처리
                placeholder="초대하려는 유저의 이메일을 입력하세요."
              />
              <SendButton 
              onClick={handleSendInvitation} 
              disabled={isSendButtonDisabled} 
              style={{ cursor: isSendButtonDisabled ? 'not-allowed' : 'pointer'}}>보내기</SendButton>
            </InvitationInputWrapper>
          </TopContainer>
          <BottomContainer>
            {" "}
            <Title>
              &nbsp;&nbsp;◾ 멤버 목록 ({organization.members.length}명)
            </Title>
            <UserList>
              {organization.members.map((nickname, index) => (
                <div
                  key={index}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    marginBottom: "8px",
                  }}
                >
                  {nickname}
                  <KickButton onClick={() => handleKickUser(nickname)}>
                    추방
                  </KickButton>
                </div>
              ))}
            </UserList>
          </BottomContainer>
        </RightContainer>
      </ModalContent>
    </ModalContainer>
  );
};

const ModalContainer = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
`;

const ModalContent = styled.div`
  background: #ffffff;
  padding: 20px;
  border-radius: 10px;
  width: 700px;

  text-align: center;
  position: relative;
  display: flex;
`;

const HalfContainer = styled.div`
  padding: 10px; /* 내부 여백 추가 */
`;

const LeftContainer = styled(HalfContainer)`
  display: flex;
  flex-direction: column; // 자식 요소들을 수직 방향으로 나열
  justify-content: center;
  align-items: center;
  flex: 1.3; /* 왼쪽 컨테이너가 차지하는 공간 */
  border-right: 2px dotted #000; /* 오른쪽에 세로선 추가 */

  p {
    margin: 20px 0; /* 상하 마진 10px, 좌우 마진 0 */
  }
`;

const LeftInsideContainer = styled(HalfContainer)`
  border-radius: 8px;
  width: 70%;
  height: 70%;
  background-color: rgba(255, 255, 180, 0.5);
`;

// RightContainer 정의
const RightContainer = styled(HalfContainer)`
  flex: 2;
  display: flex;
  flex-direction: column; // 상하 구분을 위해 컬럼 방향으로 설정
`;

// RightContainer 내부에 상하 구분을 위한 스타일 컴포넌트
const TopContainer = styled.div`
  flex: 1; // 상단 컨테이너가 차지하는 공간 비율
  flex-direction: column; // 자식 요소들을 수직 방향으로 나열
  display: flex;
  margin-bottom: 25px;
  justify-content: center;
  align-items: flex-start;
  padding: 1px 10px; // 내부 여백 추가
`;

const Title = styled.div`
  width: 100%;
  font-size: 15px;
  text-align: left; // 텍스트를 왼쪽으로 정렬
  margin-bottom: 5px; // 입력 필드와의 간격 추가
`;

const BottomContainer = styled.div`
  flex: 2; // 하단 컨테이너가 차지하는 공간 비율
  flex-direction: column; // 자식 요소들을 수직 방향으로 나열
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0px 10px; // 내부 여백 추가
`;

const UserList = styled.div`
  background: #ffffff;
  padding: 11px 20px;
  border: 1px solid #d0d0d0;
  width: 100%;
  height: 100%;
  max-height: 200px; // 또는 적절한 높이 값으로 설정
  overflow-y: auto; // 내용이 넘칠 경우 세로 스크롤을 허용
  border-radius: 8px;
  position: relative;
  box-sizing: border-box; // box-sizing 속성 설정

  &::-webkit-scrollbar {
    width: 7px; // 스크롤바의 너비
  }

  &::-webkit-scrollbar-thumb {
    background: #888; // 스크롤바 썸의 배경색
    border-radius: 10px; // 스크롤바 썸에 마우스 호버 시 색상
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #555; // 스크롤바 썸에 마우스 호버 시 색상
  }

  &::-webkit-scrollbar-corner {
    background: transparent; // 스크롤바 코너 배경을 투명하게 설정
  }
`;

const CloseButton = styled.button`
  position: absolute;
  font-size: 19px;
  font-weight: bold;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
`;

const InvitationInputWrapper = styled.div`
  display: flex;
  width: 100%;
  text-align: center;
  line-height: 40px;
  border-radius: 10px;
`;

const InvitationInput = styled.input`
  flex-grow: 1;
  border: none;
  outline: none;
  padding: 10px;
  background-color: #ffffff;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
`;

const SendButton = styled.button`
  padding: 8px; // 버튼 내부 여백

  border-radius: 4px; // 테두리 둥글게
  background-color: #007bff; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경
  margin-left: 8px;

  &:hover {
    background-color: #0056b3; // 마우스 오버 시 버튼 배경색 변경
  }
`;

const KickButton = styled.button`
  padding: 5px; // 버튼 내부 여백
  border-radius: 4px; // 테두리 둥글게
  background-color: #ff9999; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경
  margin-left: 8px;

  &:hover {
    background-color: #f77777; // 마우스 오버 시 버튼 배경색 변경
  }
`;

export default OrganizationInfoModal;
