import React, { useState, useRef, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Header from "./Header";
import ImagetoBackend from "../imageToBackend"; // ImagetoBackend 컴포넌트를 가져옴.
import NotePage from "../Note/NotePage"; // NotePage 컴포넌트를 가져옴.

import styled from "styled-components";
import { ReactComponent as AddNoteIcon } from "../../image/addNote.svg";
import defaultImage from "../../image/NoneImage2.png";

function OrganizationCard({ organization, index }) {
  return (
    <Link to={`/organization/${organization.id}`}>
      {" "}
      <OrganizationContainer>
        <img src={organization.image} alt={`Organization-Picture-${index}`} />
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
        <p style={{ fontWeight: "bold" }}>Organization 생성하기</p>

        <OrganizationInputWrapper>
          <OrganizationInput
            id="OrganizationName"
            type="text"
            placeholder="Organization 이름을 입력해주세요."
            value={organizationName}
            onChange={(e) => setOrganizationName(e.target.value)}
          />
        </OrganizationInputWrapper>

        <img
          src={myimage || defaultImage}
          alt="Organization-Picture"
          style={{ maxWidth: "300px", width: "100%", height: "auto" }}
        />

        {/* 백엔드 전송없이 사진 업로드 */}
        {/* <ImageUploadWrapper htmlFor="fileInput">
          <ImageUploadButton>이미지 찾기</ImageUploadButton>
        </ImageUploadWrapper>
        <input
          id="fileInput"
          type="file"
          onChange={uploadImage}
          style={{ display: "none" }}
        /> */}
        <ImagetoBackend onImageUpload={uploadImage} />
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

  const [myimage, setMyImage] = useState(null);
  const [organizationName, setOrganizationName] = useState("");
  const [organizations, setOrganizations] = useState([]);

  const uploadImage = (e) => {
    const selectedFile = e.target.files[0];

    // 파일이 선택되었고, 이미지 파일인 경우에만 처리
    if (selectedFile && isImageFile(selectedFile)) {
      setMyImage(URL.createObjectURL(selectedFile));
    } else {
      // 이미지 파일이 아닌 경우에 대한 처리 (예: 경고 메시지 등)
      alert("올바른 이미지 파일을 선택해주세요.");
      // 또는 다른 처리 방법을 선택할 수 있습니다.
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
        setMyImage(defaultImage);
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
    setMyImage(defaultImage);
    setOrganizationName("");
  };

  const handleCreate = () => {
    const newOrganization = {
      id: Date.now(),
      name: organizationName,
      image: myimage || defaultImage,
      submissionTime: new Date().toISOString(),
    };

    const updatedOrganizations = [...organizations, newOrganization];
    setOrganizations(updatedOrganizations);
    localStorage.setItem("organizations", JSON.stringify(updatedOrganizations));
    handleCloseModal();
  };

  return (
    <StContainer>
      <StHeader>
        <Header toggle={toggle} setToggle={setToggle} />
        <StOrgCreateBtn onClick={handleButtonClick}>
          Organization 생성 모달창 띄우는 버튼
        </StOrgCreateBtn>
        {/* <StyledAddNoteIcon onClick={handleButtonClick} /> */}
      </StHeader>
      {modalOpen && (
        <OrganizationModal
          modalRef={modalRef}
          handleCloseModal={handleCloseModal}
          organizationName={organizationName}
          setOrganizationName={setOrganizationName}
          myimage={myimage}
          uploadImage={uploadImage}
          handleCreate={handleCreate}
        />
      )}
      {/* Display Organization Name and Image on Main Page */}
      {organizations.map((org, index) => (
        <OrganizationCard organization={org} index={index} />
      ))}
      {/* NotePage를 위한 Route 추가 */}
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

// Add a new function to format the creation time
function formatCreationTime(submissionTime) {
  const submissionDate = new Date(submissionTime);
  const today = new Date();

  if (
    submissionDate.getFullYear() === today.getFullYear() &&
    submissionDate.getMonth() === today.getMonth() &&
    submissionDate.getDate() === today.getDate()
  ) {
    // If the submission time is today, display only the time
    return submissionDate.toLocaleTimeString();
  } else {
    // If the submission time is not today, display only the date
    return submissionDate.toLocaleDateString();
  }
}

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

// 노트 화면에서 사용할 요소(삭제X)
const StyledAddNoteIcon = styled(AddNoteIcon)`
  width: 15%;
  height: 15%;
  cursor: pointer;
  margin-top: 10px;
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

const ImageUploadWrapper = styled.label`
  display: block;
  margin: 0 auto;
  margin-top: 10px;
  cursor: pointer;
  padding: 0 80px;
`;

const ImageUploadButton = styled.span`
  display: block;
  text-align: center;
  line-height: 40px;
  border-radius: 10px;
  border-color: #cccccc;
  border-width: 1px; /* Add border-width property */
  border-style: solid; /* Add border-style property */
  background-color: #ffffff;
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
  width: 15%;
  text-align: center;
  display: inline-block;
  margin: 10px;

  p,
  small {
    margin: 0; /* Remove top and bottom margins */
  }

  img {
    width: 100%;
    height: auto;
    cursor: pointer;
    margin-top: 10px;
    max-width: 100%;
    max-height: 100%;
  }
`;

export default MainPage;
