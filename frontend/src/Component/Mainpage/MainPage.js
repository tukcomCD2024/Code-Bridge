import React, { useState, useRef, useEffect } from "react";
import { Routes, Route, Link } from "react-router-dom";
import Header from "./Header";
import ImagetoBackend from "../imageToBackend"; // ImagetoBackend 컴포넌트를 가져옴.
import NotePage from "../Note/NotePage"; // NotePage 컴포넌트를 가져옴.
import { formatCreationTime } from "../Utils/formatCreationTime";

import styled from "styled-components";
import { ReactComponent as AddNoteIcon } from "../../image/addNote.svg";
import defaultImage from "../../image/NoneImage2.png";

function OrganizationCard({ organization, index }) {
  return (
    <Link to={`/organization/${organization.id}`}>
      {/* {" "} */}
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

  const [selectedFile, setSelectedFile] = useState(null);


  const uploadImage = (e) => {
    const file = e.target.files[0];
    if (file && isImageFile(file)) {
      setMyImage(URL.createObjectURL(file));
      setSelectedFile(file); // 파일 객체를 selectedFile 상태로 설정
    } else {
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

  // const handleCreate = () => {
  //   const newOrganization = {
  //     id: Date.now(),
  //     name: organizationName,
  //     image: myimage || defaultImage,
  //     submissionTime: new Date().toISOString(),
  //   };

  //   const updatedOrganizations = [...organizations, newOrganization];
  //   setOrganizations(updatedOrganizations);
  //   localStorage.setItem("organizations", JSON.stringify(updatedOrganizations));
  //   handleCloseModal();
  // };

  const handleCreate = async () => {
    const owner = "qwer123";
    const name = organizationName;
  
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
        console.log('Success:', responseData);
        // 성공 처리 로직
        // 예: 새로운 organization을 상태에 추가하거나 사용자를 다른 페이지로 리다이렉션
        const newOrganization = {
          id: responseData.id, // 백엔드에서 생성된 organizationID를 사용한다고 가정
          name: organizationName,
          image: myimage || defaultImage,
          submissionTime: new Date().toISOString(), // 또는 백엔드에서 반환된 값을 사용
        };
        const updatedOrganizations = [...organizations, newOrganization];
        setOrganizations(updatedOrganizations);
        localStorage.setItem('organizations', JSON.stringify(updatedOrganizations));
        handleCloseModal();
      } else {
        // 응답이 ok가 아닌 경우 에러 처리
        const errorData = await response.json();
        alert(`생성 실패: ${errorData.message}`);
      }
    } catch (error) {
      // 네트워크 오류 또는 요청 완료 불가 등의 예외 처리
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");

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
      }
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
    width: 200px; /* 너비 설정 */
    height: 300px; /* 높이 설정 */
    cursor: pointer;
    margin-top: 10px;
    border: 1px solid rgba(0, 0, 0, 0.2); // 블랙 색상에 알파값 0.2로 설정
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
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