import React, { useState, useRef, useEffect } from "react";
import { Routes, Route, useLocation, useNavigate  } from "react-router-dom";
import Header from "./Header";
import NotePage from "../Note/NotePage"; // NotePage 컴포넌트를 가져옴.
import styled, { keyframes, css } from "styled-components";
import { defaultEmoji, emojiList } from "../Utils/emojiList";

function EmojiPicker({ onSelect }) {
  return (
    <EmojiContainer>
      {emojiList.map((emoji) => (
        <EmojiSelectButton key={emoji} onClick={() => onSelect(emoji)}>
          {emoji}
        </EmojiSelectButton>
      ))}
    </EmojiContainer>
  );
}

function OrganizationCard({ organization }) {
 const navigate = useNavigate(); // useNavigate 훅 사용

 const handleOrganizationClick = () => {
   navigate(`/organization/${organization.id}`); // 해당 조직 페이지로 이동
 };

 return (
  <OrganizationContainer onClick={handleOrganizationClick}>
  <span style={{ fontSize: '120px', padding: '0px 0px' }}>{organization.emoji || defaultEmoji}</span>
    <OrganizationName>
      {organization.name}
    </OrganizationName>
  </OrganizationContainer>
 );
}

function OrganizationModal({
  modalRef,
  handleCloseModal,
  organizationName,
  setOrganizationName,
  myEmoji,
  setMyEmoji,
  isInvalid,
  handleCreate,
}) {
  const handleSelectEmoji = (emoji) => {
    setMyEmoji(emoji);
  };

  return (
    <ModalContainer>
      <ModalContent ref={modalRef}>
        <CloseButton onClick={handleCloseModal}>X</CloseButton>
        <p style={{ fontWeight: "bold" }}>📚 Organization 생성하기</p>
        <OrganizationInputWrapper>
          <OrganizationInput
            id="OrganizationName"
            type="text"
            placeholder="Organization 이름을 입력해주세요."
            value={organizationName}
            $isInvalid={isInvalid}
            onChange={(e) => setOrganizationName(e.target.value)}
          />
        </OrganizationInputWrapper>
        <Emoji style={{ fontSize: "100px" }}>{myEmoji}</Emoji>
        <div>
          <EmojiPicker onSelect={handleSelectEmoji} />
        </div>
        <hr />
        <CreateButton disabled={isInvalid} onClick={handleCreate}>
          생성하기
        </CreateButton>
      </ModalContent>
    </ModalContainer>
  );
}

function MainPage() {
  const [toggle, setToggle] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const modalRef = useRef();

  const [myEmoji, setMyEmoji] = useState(defaultEmoji);
  const [organizationName, setOrganizationName] = useState("");
  const [organizations, setOrganizations] = useState([]);
  const [isInvalid, setIsInvalid] = useState(false);
  
  const location = useLocation(); // 현재 위치 정보를 가져옴
  const navigate = useNavigate(); // useNavigate 훅 사용
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    if(!userId){
      navigate("/login");
      alert("계정 정보가 없습니다. 로그인 후 접속하세요.");
      return;
    }
  }, [location]);

  useEffect(() => {
    const fetchOrganizations = async () => {
      try {
        const response = await fetch(`/api/user/organization/${userId}`);
          if (response.ok) {
            const data = await response.json();
              const fetchedOrganizationData = data.map(org => ({
              id: org.id,
              name: org.name,
              emoji: org.emoji,
              members: org.members,
            }));
            setOrganizations(fetchedOrganizationData);
          } else {
            console.error(`${userId}의 Organization을 불러오는데 실패했습니다.`);
          }
        } catch (error) {
          console.error('Error fetching organizations:', error);
        }
      };
      fetchOrganizations();
    }, [location, userId]);


  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setModalOpen(false);
        setMyEmoji(defaultEmoji);
        setOrganizationName("");
      }
    };

    if (modalOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [modalOpen]);

  const handleButtonClick = () => {
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
    setMyEmoji(defaultEmoji);
    setOrganizationName("");
  };

  const handleCreate = async (e) => {
    e.preventDefault();

    if (organizationName === "") {
      setIsInvalid(true); // 유효성 상태 업데이트
      setTimeout(() => setIsInvalid(false), 800); // 800ms 후 유효성 상태 초기화
      return;
    }

    const owner = localStorage.getItem("email");
    const name = organizationName;
    const emoji = myEmoji; // Organization 대표 마크를 이모지로 설정함.

    const createOrganization = (organizationId) => {
      const newOrganization = {
        id: organizationId,
        name: organizationName,
        emoji: myEmoji,
      };
  
      const updatedOrganizations = [...organizations, newOrganization];
      setOrganizations(updatedOrganizations);
      // localStorage.setItem("organizations", JSON.stringify(updatedOrganizations));
      handleCloseModal();
    };
  
    try {
      const response = await fetch("/api/user/organization", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name, owner, emoji }),
      });

      if (response.ok) {
        const responseData = await response.json();
        const organizationId = responseData.organizationId;
        createOrganization(organizationId);
        console.log("생성 성공:", responseData);

      } else {
        const errorData = await response.json();
        alert(`생성 실패: ${errorData.message}`);
      }
    } catch (error) {      
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <StContainer>
      <StHeader>
        <Header toggle={toggle} setToggle={setToggle} />
        <StOrgCreateBtn onClick={handleButtonClick}>
          Organization 생성하기
        </StOrgCreateBtn>
      </StHeader>
      <OrganizationsContainer>
      {organizations?.length > 0 ? (
            organizations.map((org, index) => (
              <OrganizationCard organization={org} index={index} key={org.id} />
            ))
          ) : (
            <NoOrganizationMessage>
              📢 소속된 Organization이 없습니다.
            </NoOrganizationMessage>
          )}
      </OrganizationsContainer>
      {modalOpen && (
        <OrganizationModal
          modalRef={modalRef}
          handleCloseModal={handleCloseModal}
          organizationName={organizationName}
          setOrganizationName={setOrganizationName}
          myEmoji={myEmoji}
          setMyEmoji={setMyEmoji}
          isInvalid={isInvalid}
          setIsInvalid={setIsInvalid}
          handleCreate={handleCreate}
        />
      )}
      <Routes>
        {organizations.map((org) => (
          <Route
            path={`/organization/${org.id}`}
            element={
              <NotePage organization={org} organizations={organizations} />
            }
            key={org.id}
          />
        ))}
      </Routes>
    </StContainer>
  );
}

const Emoji = styled.p`
  font-size: 120px;
  padding: 0px 0px;

  @media screen and (max-width: 1000px) {
    padding: 0px 0px;
  }
`;


const EmojiContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  height: 80px;
  overflow-y: scroll;
  gap: 6px;
  border: 1px solid #cccccc;
  border-radius: 10px;
  padding: 5px;
  margin-bottom: 10px;

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

const EmojiSelectButton = styled.button`
  font-size: 20px;
  border: none;
  background: none;
  cursor: pointer;

  &:hover {
    background-color: #cccccc;
    border-color: #cccccc;
    border-radius: 10px;
    color: #000000;
  }
`;

const StContainer = styled.div``;

const StHeader = styled.div`
  display: flex;
  flex-direction: column;
`;

const StOrgCreateBtn = styled.button`
  display: flex;
  flex-direction: column;
  margin-top: 10px;
  margin: 10px auto; /* Auto margin for centering horizontally */
  max-width: 1360px;
  width: 100%;
  height: 40px;
  border: 2px solid #ffffff;
  border-radius: 10px;
  background-color: #cccccc;
  text-align: center;
  align-items: center;
  line-height: 40px;
  font-size: 16px;
  color: #000000;
  cursor: pointer;

  @media screen and (max-width: 768px) {
    flex-direction: column;
    gap: 0px;
    width: 100%;
  }

  &:hover {
    background-color: #cccccc;
    border-color: #cccccc;
    color: #000000;
  }
`;

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

const CloseButton = styled.button`
  position: absolute;
  color: red;
  font-size: 19px;
  font-weight: bold;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
`;

const OrganizationInputWrapper = styled.div`
  display: block;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
`;

const shakeAnimation = keyframes`
  0% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  50% { transform: translateX(5px); }
  75% { transform: translateX(-5px); }
  100% { transform: translateX(0); }
`;

const OrganizationInput = styled.input`
  width: 90%;
  border: none;
  outline: none;
  padding: 10px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #d0d0d0;

  ${(props) =>
    props.$isInvalid &&
    css`
      border: 2px solid red;
      animation: ${shakeAnimation} 0.5s ease-in-out;
    `}
`;

// 모달창_생성하기 버튼
const CreateButton = styled.button`
  display: block;
  width: 100%;
  font-size: 15px;
  text-align: center;
  line-height: 40px;
  border-radius: 10px;
  border-color: #cccccc;
  border-width: 1px; /* Add border-width property */
  border-style: solid; /* Add border-style property */
  background-color: #cccccc;
  cursor: pointer;

  &:hover {
    background: ${(props) => (props.disabled ? "#cccccc" : "#bbbbbb")};
  }

  &:disabled {
    background-color: #e0e0e0;
    color: #a0a0a0;
    cursor: not-allowed;
  }
`;

const ModalContent = styled.div`
  background: #ffffff;
  padding: 20px;
  border-radius: 10px;
  width: 300px;
  text-align: center;
  position: relative;
`;

const OrganizationName = styled.p`
  color: #000000;
  text-decoration: underline white;
  white-space: nowrap; /* 텍스트를 한 줄로 만들기 */
  overflow: hidden; /* 오버플로우된 텍스트 숨기기 */
  text-overflow: ellipsis; /* 오버플로우된 텍스트를 말줄임표로 표시 */
  max-width: 100%; /* 최대 너비 설정 (조절 가능) */
  display: block; /* 블록 레벨 요소로 만들기 (필요한 경우) */
`;

const OrganizationsContainer = styled.div`
padding-left: 80px;
display: flex;
flex-wrap: wrap;
justify-content: start; /* 가로 축에서 중앙 정렬 */

gap: 20px;

@media (max-width: 768px) {
  padding-left: 0px;
}

a {
  color: inherit; /* 상위 요소로부터 색상을 상속받습니다. */
  text-decoration: none; /* 밑줄 등의 텍스트 장식을 제거합니다. */
}
`;

const OrganizationContainer = styled.div`
display: flex;
flex-direction: column; // 항목을 세로로 정렬
width: 180px;
margin: 10px; // 주변 여백
text-align: center;
cursor: pointer; // 마우스 오버 시 커서 변경
`;

const NoOrganizationMessage = styled.div`
  display: flex;
  width: 100%;
  justify-content: center;
  margin-top: 200px;
  margin-right: 80px;
  align-items: center;
  font-weight: bold;
  font-size: 20px;
  color: #666;

  @media screen and (max-width: 768px) {
    margin-right: 0px;
  }
`;

export default MainPage;
