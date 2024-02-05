import React, { useState, useEffect, useRef } from "react";
import { useParams, Link, Route, Routes } from "react-router-dom";
import ImagetoBackend from "../imageToBackend";
import NoteDetail from "./NoteDetail";

import { formatCreationTime } from "../Utils/formatCreationTime";

import styled from "styled-components";
import defaultImage from "../../image/NoneImage2.png";
import { ReactComponent as AddNoteIcon } from "../../image/addNote.svg";

function NoteCard({ note, index }) {
  // note prop 추가
  return (
    <Link to={`/organization/${note.organizationId}/${note.id}`}>
      {/* {"📖"} */}
      {/* 수정된 경로 */}
      <NoteContainer>
        <img src={note.image} alt={`Note-Picture-${index}`} />
        <p>
          <small>{note.name}</small>
        </p>
        <p>
          <small>{formatCreationTime(note.submissionTime)}</small>
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
            onChange={(e) => setNoteName(e.target.value)}
          />
        </NoteInputWrapper>

        <StyledImage
          src={myimage || defaultImage}
          alt="Note-Picture"
          isDefaultImage={myimage === defaultImage}
        />
        <ImagetoBackend onImageUpload={uploadImage} />
        <hr />
        <CreateButton onClick={handleCreate}>생성하기</CreateButton>
      </ModalContent>
    </ModalContainer>
  );
}

function NotePage() {
  const { id } = useParams();
  const organizationId = Number(id);
  const [organization, setOrganization] = useState(null);
  const [myimage, setMyImage] = useState(null);
  const [noteName, setNoteName] = useState("");
  const [notes, setNotes] = useState([]); // 노트 상태 관리
  const [modalOpen, setModalOpen] = useState(false);
  const modalRef = useRef();

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
    // 로컬 스토리지에서 organizations 데이터를 가져옴
    const storedOrganizations = JSON.parse(
      localStorage.getItem("organizations")
    );
    if (storedOrganizations) {
      // URL에서 가져온 id와 일치하는 organization을 찾음
      const foundOrganization = storedOrganizations.find(
        (org) => org.id === organizationId
      );
      setOrganization(foundOrganization);
    }
  }, [organizationId]);

  useEffect(() => {
    // 로컬 스토리지에서 노트 데이터 불러오기
    const savedNotes = JSON.parse(localStorage.getItem("notes")) || {};
    // 현재 조직에 대한 노트만 설정
    if (savedNotes[organizationId]) {
      setNotes(savedNotes[organizationId]);
    } else {
      setNotes([]);
    }
  }, [organizationId]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setModalOpen(false);
        setMyImage(defaultImage);
        setNoteName("");
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
    setMyImage(defaultImage);
    setNoteName("");
  };

  const handleCreate = () => {
    const newNote = {
      id: Date.now(),
      name: noteName,
      image: myimage || defaultImage,
      submissionTime: new Date().toISOString(),
      organizationId: organizationId,
    };

    // 노트 저장 로직 수정
    const savedNotes = JSON.parse(localStorage.getItem("notes")) || {};
    const organizationNotes = savedNotes[organizationId]
      ? savedNotes[organizationId]
      : [];
    const updatedNotes = [...organizationNotes, newNote];
    savedNotes[organizationId] = updatedNotes;

    setNotes(updatedNotes);
    localStorage.setItem("notes", JSON.stringify(savedNotes));
    handleCloseModal();
  };

  return (
    <div>
      <h1 style={{ textDecoration: "underline" }}>{organization?.name}</h1>{" "}
      {/* 옵셔널 체이닝 사용 */}
      <h3>[노트 목록]</h3>
      {/* <StNoteCreateBtn onClick={handleButtonClick}>
        Note 생성 모달창 띄우는 버튼
      </StNoteCreateBtn> */}
      <StyledAddNoteIcon onClick={handleButtonClick} />
      {modalOpen && (
        <NoteModal
          modalRef={modalRef}
          handleCloseModal={handleCloseModal}
          noteName={noteName}
          setNoteName={setNoteName}
          myimage={myimage}
          uploadImage={uploadImage}
          handleCreate={handleCreate}
        />
      )}
      {notes.map((note, index) => (
        <NoteCard note={note} index={index} />
      ))}
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

const StNoteCreateBtn = styled.button`
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
  width: 230px;
  height: 385px;
  cursor: pointer;
  margin-top: 10px;
`;

const StyledImage = styled.img`
  width: ${(props) => (props.isDefaultImage ? "100%" : "100px")}; // 예시 크기
  height: auto;
  width: 100%; /* 너비를 최대값으로 설정 */
  object-fit: contain;
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
  border-radius: 10px;
  background-color: #ffff99;
`;

const NoteInput = styled.input`
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

const NoteContainer = styled.div`
  width: 15%;
  text-align: center;
  display: inline-block;
  margin: 10px 0px; //상하 마진 10px, 좌우 마진 5px

  p,
  small {
    margin: 0; /* Remove top and bottom margins */
  }

  img {
    width: 200px; /* 너비 설정 */
    height: 300px; /* 높이 설정 */
    cursor: pointer;
    margin-top: 10px;
    border: 1px solid rgba(0, 0, 0, 0.2); // 블랙 색상에 알파값 0.2로 설정
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
  }
`;

export default NotePage;
