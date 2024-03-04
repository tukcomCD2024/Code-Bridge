import React, { useState, useEffect, useRef } from "react";
import { useParams, Link, Route, Routes, useNavigate, useLocation } from "react-router-dom";
import styled, { keyframes, css } from "styled-components";
import { formatCreationTime } from "../Utils/formatCreationTime";
import OrganizationInfoModal from "./organizationInfo/organizationInfo";
import ImagetoBackend from "../imageToBackend";
import NoteDetail from "./NoteDetail";
import defaultImage from "../../image/NoneImage2.png";
import { ReactComponent as AddNoteIcon } from "../../image/addNote.svg";
import toastr from "toastr";
import "toastr/build/toastr.css";
toastr.options.positionClass = "toast-top-right";


function NoteCard({ note, index }) {
  return (
    <Link to={`/organization/${note.organizationId}/${note.id}`}>
      {/* {"📖"} */}
      <NoteContainer>
        <img src={note.image} alt={`Note-Picture-${index}`} />
        <NoteName>
          <small>{note.name}</small>
        </NoteName>
        <p
          style={{
            color: "#000000",
            cursor: "default",
            textDecoration: "underline white",
          }}
        >
          <small>&nbsp;</small>
        </p>
      </NoteContainer>
    </Link>
  );
}

function NoteModal({
  modalRef,
  handleCloseModal,
  noteName,
  setNoteName,
  myimage,
  uploadImage,
  isInvalid,
  handleCreate,
}) {
  return (
    <ModalContainer>
      <ModalContent ref={modalRef}>
        <CloseButton onClick={handleCloseModal} style={{ color: "red" }}>
          X
        </CloseButton>
        <p style={{ fontWeight: "bold" }}>📔 노트 생성</p>
        <NoteInputWrapper>
          <NoteInput
            id="NoteName"
            type="text"
            placeholder="생성하는 노트 이름을 입력해주세요."
            value={noteName}
            $isInvalid={isInvalid}
            onChange={(e) => setNoteName(e.target.value)}
          />
        </NoteInputWrapper>

        <StyledImage
          src={myimage || defaultImage}
          alt="Note-Picture"
          $isDefaultImage={myimage === defaultImage}
        />
        <ImagetoBackend onImageUpload={uploadImage} />
        <hr />
        <CreateButton disabled={isInvalid} onClick={handleCreate}>
          생성하기
        </CreateButton>
      </ModalContent>
    </ModalContainer>
  );
}

function NotePage() {
  const { id } = useParams();
  const organizationId = String(id);
  const location = useLocation(); // 현재 위치 정보를 가져옴
  const modalRef = useRef();
  const navigate = useNavigate();
  const [organization, setOrganization] = useState(null);
  const [myimage, setMyImage] = useState(null);
  const [noteName, setNoteName] = useState("");
  const [notes, setNotes] = useState([]); // 노트 상태 관리
  const [modalOpen, setModalOpen] = useState(false);
  const [OrganizationModalOpen, setOrganizationModalOpen] = useState(false);
  const [isInvalid, setIsInvalid] = useState(false);


  const uploadImage = (e) => {
    const selectedFile = e.target.files[0];

    // 파일이 선택되었고, 이미지 파일인 경우에만 처리
    if (selectedFile && isImageFile(selectedFile)) {
      setMyImage(URL.createObjectURL(selectedFile));
    } else {
      // 이미지 파일이 아닌 경우에 대한 처리 (예: 경고 메시지 등)
      alert("올바른 이미지 파일을 선택해주세요.");
    }
  };

  // 이미지 파일 여부를 확인하는 함수
  const isImageFile = (file) => {
    const allowedExtensions = ["jpg", "jpeg", "png", "gif"]; // 허용된 확장자들

    // 파일 이름에서 확장자 추출
    const fileName = file.name;
    const fileExtension = fileName.split(".").pop().toLowerCase();

    // 허용된 확장자들 중에 포함되어 있는지 확인
    return allowedExtensions.includes(fileExtension);
  };

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    const fetchNotesInformation = async () => {
      try {
        const response = await fetch(`/api/user/organization/${userId}`);
        if (response.ok) {
          const organizations = await response.json();
          const matchedOrganization = organizations.find(org => org.id === id);
          if (matchedOrganization) {
            setOrganization(matchedOrganization);
          }
        } else {
          console.error('Failed to fetch');
        }
      } catch (error) {
        console.error('Error fetching:', error);
      }
    };

    const fetchNotes = async () => {
      const organizaionId = id;
      try {
        const response = await fetch(`/api/user/note/${organizaionId}`);
          if (response.ok) {
            const data = await response.json();
            const fetchedNoteData = data.map(note => ({
              id: note.id,
              name: note.title,
              image: note.noteImageUrl,
              organizationId: organizationId

            }));
            setNotes(fetchedNoteData);
          } else {
            console.error("Failed to fetch");
          }
        } catch (error) {
          console.error('Error fetching', error);
        }
      };

    fetchNotesInformation();
    fetchNotes();
  }, [id, location]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setModalOpen(false);
        setMyImage(defaultImage);
        setNoteName("");
      }
    };

    if (modalOpen) {
      if (organization?.name == null) {
        toastr.info("정보를 불러오지 못했습니다.");
        navigate("/main");
        return;
      }
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
    setMyImage(defaultImage);
    localStorage.setItem("recentImageUrl", '');
    setNoteName("");
  };

  const handleOpenOrganizationModal = () => {
    setOrganizationModalOpen(true);
  };

  const handleCloseOrganizationModal = () => {
    setOrganizationModalOpen(false);
  };

  const handleCreate = async (e) => {
    e.preventDefault();

    if (noteName === "") {
      setIsInvalid(true);
      setTimeout(() => setIsInvalid(false), 800);
      return;
    }

    const organizationId = id;
    const title = noteName;
    const createUser = localStorage.getItem("userId");
    const noteImageUrl = localStorage.getItem('recentImageUrl') || 'https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/NoneImage2.png';
    
    const createNote = (noteId) => {
      const newNote = {
        id: noteId,
        name: noteName,
        image: myimage || defaultImage,
      };

      const updatedNotes = [...notes, newNote];
      setNotes(updatedNotes);
      localStorage.setItem("notes", JSON.stringify(updatedNotes));
      handleCloseModal();
    };
    
    try {
      const response = await fetch("/api/user/note", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ organizationId, title, createUser, noteImageUrl }),
      });
      if (response.ok) {
        const responseData = await response.json();
        const noteId = responseData.noteId;
        createNote(noteId);
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
    <div>
      <h1 style={{ textDecoration: "underline" }}>{organization?.name}</h1>{" "}
      {/* 옵셔널 체이닝 사용 */}
      <h3>[노트 목록]</h3>
      <OrganizationInfo onClick={handleOpenOrganizationModal}>
        Organization 정보 확인
      </OrganizationInfo>
      <NotesContainer>
        <StyledAddNoteIcon onClick={handleButtonClick} />
        {notes.map((note, index) => (
          <NoteCard note={note} index={index} key={note.id} />
        ))}
      </NotesContainer>
      {OrganizationModalOpen && (
        <OrganizationInfoModal
          modalOpen={OrganizationModalOpen}
          handleCloseModal={handleCloseOrganizationModal}
          organization={organization}
        />
      )}
      {modalOpen && (
        <NoteModal
          modalRef={modalRef}
          handleCloseModal={handleCloseModal}
          noteName={noteName}
          setNoteName={setNoteName}
          myimage={myimage}
          isInvalid={isInvalid}
          uploadImage={uploadImage}
          handleCreate={handleCreate}
        />
      )}
      <Routes>
        {notes.map((note) => (
          <Route
            path={`/organization/${organizationId}/${note.id}`}
            element={<NoteDetail note={note} notes={notes} />}
            key={note.id}
          />
        ))}
      </Routes>
    </div>
  );
}


const OrganizationInfo = styled.button`
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

const StyledAddNoteIcon = styled(AddNoteIcon)`
  width: 200px;
  cursor: pointer;
  margin-top: 10px;
`;

const StyledImage = styled.img`
  width: ${(props) => (props.$isDefaultImage ? "100%" : "100px")}; // 예시 크기
  height: auto;
  width: 100%; /* 너비를 최대값으로 설정 */
  object-fit: contain;
`;

const NoteName = styled.p`
  color: #000000;
  text-decoration: underline white;
  white-space: nowrap; /* 텍스트를 한 줄로 만들기 */
  overflow: hidden; /* 오버플로우된 텍스트 숨기기 */
  text-overflow: ellipsis; /* 오버플로우된 텍스트를 말줄임표로 표시 */
  max-width: 100%; /* 최대 너비 설정 (조절 가능) */
  display: block; /* 블록 레벨 요소로 만들기 (필요한 경우) */
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
  font-size: 19px;
  font-weight: bold;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
`;

const NoteInputWrapper = styled.div`
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

const NoteInput = styled.input`
  width: 90%;
  border: none;
  outline: none;
  padding: 10px;
  background-color: #ffffff;
  border: 1px solid #d0d0d0;
  border-radius: 8px;

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

const NotesContainer = styled.div`
  padding-left: 100px;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 20px;

  @media (max-width: 768px) {
    padding-left: 0px;
  }
`;

const NoteContainer = styled.div`
  display: flex;
  flex-direction: column; // 항목을 세로로 정렬
  align-items: center; // 항목들을 가운데 정렬
  width: 180px; // 너비 고정
  height: 320px;
  margin: 10px; // 주변 여백
  text-align: center;
  cursor: pointer; // 마우스 오버 시 커서 변경
  margin-bottom: 20px; // 하단 마진 추가로 각 카드 사이의 수직 간격 조정

  p,
  small {
    margin: 0; /* Remove top and bottom margins */
  }

  img {
    width: 200px; /* 너비 설정 */
    height: 100%; /* 높이 설정 */
    cursor: pointer;
    margin-top: 10px;
    border: 1px solid rgba(0, 0, 0, 0.2); // 블랙 색상에 알파값 0.2로 설정
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
  }

  @media (max-width: 768px) {
    width: 100%;
    padding-left: 10px;
  }
`;

export default NotePage;
