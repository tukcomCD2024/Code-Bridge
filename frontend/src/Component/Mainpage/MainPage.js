import React, { useState, useRef, useEffect } from "react";
import { Routes, Route, Link } from "react-router-dom";
import Header from "./Header";
import NotePage from "../Note/NotePage"; // NotePage 컴포넌트를 가져옴.
import { formatCreationTime } from "../Utils/formatCreationTime";
import styled from "styled-components";
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
  return (
    <Link to={`/organization/${organization.id}`}>
      <OrganizationContainer>
        <Emoji>{organization.emoji || defaultEmoji}</Emoji>
        <p>
          <small>{organization.name}</small>
        </p>
        <p>
          <small>{formatCreationTime(organization.submissionTime)}</small>
        </p>
      </OrganizationContainer>
    </Link>
  );
}

function OrganizationModal({
  modalRef,
  handleCloseModal,
  organizationName,
  setOrganizationName,
  myEmoji,
  setMyEmoji,
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
            onChange={(e) => setOrganizationName(e.target.value)}
          />
        </OrganizationInputWrapper>
        <Emoji style={{ fontSize: "100px" }}>{myEmoji}</Emoji>
        <div>
          <EmojiPicker onSelect={handleSelectEmoji} />
        </div>
        <hr />
        <CreateButton onClick={handleCreate}>생성하기</CreateButton>
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

  useEffect(() => {
    // 로컬 스토리지에서 데이터 불러오기
    const savedOrganizations = localStorage.getItem("organizations");
    if (savedOrganizations) {
      setOrganizations(JSON.parse(savedOrganizations));
    }
  }, []);

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

  const createOrganization = () => {
    const newOrganization = {
      id: Date.now(),
      name: organizationName,
      emoji: myEmoji,
      submissionTime: new Date().toISOString(),
    };

    const updatedOrganizations = [...organizations, newOrganization];
    setOrganizations(updatedOrganizations);
    localStorage.setItem("organizations", JSON.stringify(updatedOrganizations));
    handleCloseModal();
  };

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
      alert("Organization 이름을 입력해주세요.");
      return;
    }

    const owner = localStorage.getItem("userId");
    const name = organizationName;
    const organizationEmoji = myEmoji; // Organization 대표 마크를 이모지로 설정함.

    try {
      const response = await fetch("/api/user/organization", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name, owner }),
      });

      if (response.ok) {
        const responseData = await response.json();
        console.log("생성 성공:", responseData);
        createOrganization();
      } else {
        const errorData = await response.json();
        alert(`생성 실패: ${errorData.message}`);
      }
    } catch (error) {
      createOrganization(); // 유저 정보와 연결되면 삭제해야 하는 코드
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <StContainer>
      <StHeader>
        <Header toggle={toggle} setToggle={setToggle} />
        <StOrgCreateBtn onClick={handleButtonClick}>
          Organization 생성 모달창 띄우는 버튼
        </StOrgCreateBtn>
      </StHeader>
      {modalOpen && (
        <OrganizationModal
          modalRef={modalRef}
          handleCloseModal={handleCloseModal}
          organizationName={organizationName}
          setOrganizationName={setOrganizationName}
          myEmoji={myEmoji}
          setMyEmoji={setMyEmoji}
          handleCreate={handleCreate}
        />
      )}

      {organizations.length > 0 ? (
        organizations.map((org, index) => (
          <OrganizationCard organization={org} index={index} key={org.id} />
        ))
      ) : (
        <NoOrganizationMessage>
          📢 소속된 Organization이 없습니다.
        </NoOrganizationMessage>
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
  font-size: 100px; /* 이모지 기본 크기 */

  @media screen and (max-width: 768px) {
    font-size: 50px;
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
  border-radius: 10px;
  background-color: #ffff99;
`;

const OrganizationInput = styled.input`
  background-color: #ffff99;
  border: none;
  outline: none;
  width: 80%;
  padding: 5px;
  border-radius: 5px;
`;

// 모달창_생성하기 버튼
const CreateButton = styled.span`
  display: block;
  text-align: center;
  line-height: 40px;
  border-radius: 10px;
  border-color: #cccccc;
  border-width: 1px; /* Add border-width property */
  border-style: solid; /* Add border-style property */
  background-color: #cccccc;
  cursor: pointer;

  &:hover {
    background: #cccccc;
    box-shadow: 0 0 5px #cccccc, 0 0 5px #cccccc, 0 0 5px #cccccc,
      0 0 5px #cccccc;
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

const OrganizationContainer = styled.div`
  width: 14%;
  text-align: center;
  display: inline-block;
  margin: 5px;

  p,
  small {
    margin: 0px; /* Remove top and bottom margins */
  }
`;

const NoOrganizationMessage = styled.div`
  display: flex;
  justify-content: center;
  margin-top: 200px;
  align-items: center;
  font-weight: bold;
  font-size: 20px;
  color: #666;
`;

export default MainPage;
