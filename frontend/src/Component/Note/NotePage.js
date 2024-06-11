import React, { useState, useEffect, useRef } from "react";
import { useParams, Link, Route, Routes, useNavigate, useLocation } from "react-router-dom";
import styled, { keyframes, css } from "styled-components";
import OrganizationInfoModal from "./organizationInfo/organizationInfo";
import OrganizationContainer from "../Organization/Organization_Web";
import ImagetoBackend from "../Utils/imageToBackend";
import noneImage from "../../image/NoneImage2.png";
import defaultImage from "../../image/defaultNote2.png";
import backgroundImage from "../../image/organizationBackgroundImage.png";
import AddNoteIcon from "../../image/addNote.svg";
import toastr from "toastr";
import "toastr/build/toastr.css";
toastr.options.positionClass = "toast-top-right";

function NoteCard({ note, index }) {
  return (
    <div>
      <NoteContainer>
        <BookWrapper>
          <BookItems>
            <MainBookWrap>
              <Link to={`/organization/${note.organizationId}/${note.id}/${note.pageId}`}>
                <BookCover>
                  <BookInside />
                  <BookImage>
                    <img src={note.image} alt={`Note-Picture-${index}`} />
                    <Effect />
                    <Light />
                  </BookImage>
                </BookCover>
              </Link>
            </MainBookWrap>
          </BookItems>
        </BookWrapper>
      </NoteContainer>
      <NoteName>
        {note.name}
      </NoteName>
    </div>
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
          src={myimage || noneImage}
          alt="Note-Picture"
          $isDefaultImage={myimage === noneImage}
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
  const navigate = useNavigate();

  const userId = localStorage.getItem('userId');
  const refresh = localStorage.getItem("refresh");
  const access = localStorage.getItem("access");

  const modalRef = useRef();
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

    const fetchOrganizationInfo = async () => {
      try {
        const response = await fetch(`/api/user/organization/${userId}`, {
          headers: {
            "access": access,
            "refresh": refresh,
          },
        });
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

    useEffect(() => {
      const fetchNotes = async () => {
        try {
          const response = await fetch(`/api/user/note/${organizationId}`, {
            headers: {
              "access": access,
              "refresh": refresh,
            },
          });
          if (response.ok) {
            const data = await response.json();
            const fetchedNoteData = await Promise.all(data.map(async (note) => {
              // Fetch pageId for each note
              try {
                const pageResponse = await fetch(`/api/page/search`, {
                  method: "POST",
                  headers: {
                    "Content-Type": "application/json",
                    "access": access,
                    "refresh": refresh,
                  },
                  body: JSON.stringify({ organizationId, noteId: note.id, createUserId: userId }),
                });
                if (pageResponse.ok) {
                  const pageData = await pageResponse.json();
                  if (pageData && pageData.length > 0) {
                    return {
                      id: note.id,
                      name: note.title,
                      image: note.noteImageUrl,
                      organizationId: id,
                      pageId: pageData[0].pageId,
                    };
                  }
                } else {
                  console.error(`Failed to fetch: HTTP status ${pageResponse.status}`);
                }
              } catch (error) {
                console.error('Error fetching page', error);
              }
              return null; // if pageId fetching fails
            }));
            setNotes(fetchedNoteData.filter(note => note !== null)); // Filter out null values
          } else {
            console.error("Failed to fetch");
          }
        } catch (error) {
          console.error('Error fetching notes', error);
        }
      };
    
      fetchOrganizationInfo();
      fetchNotes();
    }, [id, location]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setModalOpen(false);
        setMyImage(null);
        localStorage.removeItem("recentImageUrl");
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
    setMyImage(null);
    localStorage.removeItem("recentImageUrl");
    setNoteName("");
  };

  const handleOpenOrganizationModal = () => {
    if (organization?.name == null) {
      toastr.info("정보를 불러오지 못했습니다.");
      navigate("/organization");
      return;
    }
    
    fetchOrganizationInfo();
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
    const createUserId = localStorage.getItem("userId");
    // const noteImageUrl = localStorage.getItem('recentImageUrl') || 'https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/NoneImage2.png';
    const noteImageUrl = localStorage.getItem('recentImageUrl') || defaultImage;
    
    const createNote = (noteId, pageId) => {
      const newNote = {
        id: noteId,
        pageId: pageId,
        name: noteName,
        image: myimage || defaultImage,
        organizationId: organizationId
      };
      console.log(newNote);
      const updatedNotes = [...notes, newNote];
      setNotes(updatedNotes);
      fetchOrganizationInfo();
      handleCloseModal();
    };
    
    try {
      const response = await fetch("/api/user/note", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "access": access,
          "refresh": refresh,
        },
        body: JSON.stringify({ organizationId, title, createUser, noteImageUrl }),
      });
      if (!response.ok) {
        const errorData = await response.json();
        alert(`노트 생성 실패: ${errorData.message}`);
        return;
      }
      const responseData = await response.json(); // 한 번만 호출
      const noteId = responseData.noteId;
      console.log("노트 생성 성공:", responseData);

      const responsePage = await fetch("/api/page", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "access": access,
          "refresh": refresh,
        },
        body: JSON.stringify({ organizationId, noteId, createUserId }),
      });
      if (!responsePage.ok) {
        const errorData = await responsePage.json();
        alert(`페이지 생성 실패: ${errorData.message}`);
        return;
      }
      const responseDataPage = await responsePage.json(); // 한 번만 호출
      const pageId = responseDataPage.pageId;
      console.log("페이지 생성 성공:", responseDataPage);
      createNote(noteId, pageId);
    } catch (error) {
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };
  
  const removeOrganization = async () => {
    if (organization?.name == null) {
      toastr.info("정보를 불러오지 못했습니다.");
      navigate("/organization");
      return;
    }

    const isConfirmed = window.confirm(`"${organization?.name}" 의 모든 데이터를 삭제하시겠습니까? \n\n${organization.notes.length}개의 노트가 삭제되고, ${organization.members.length}명의 멤버가 추방됩니다.\n계속 진행하시려면 확인을 눌러주세요.`);

    if (isConfirmed) {
      try {
        const response = await fetch("/api/user/organization", {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            "access": access,
            "refresh": refresh,
          },
          body: JSON.stringify({ organizationId, userLoginId: localStorage.getItem("email") }),
        });
        const contentType = response.headers.get('content-type');
        // 삭제 성공
        if (response.ok) {
          if (contentType && contentType.includes('application/json')) {
          navigate("/organization");
          toastr.info("정상적으로 삭제되었습니다.");
          }
        // 비정상적 상황
        } else {
          if (contentType && contentType.includes('text/plain')) {
            const errorMessage = await response.text();
            alert(`실패: ${errorMessage}`);
          } else {
            alert("이미 삭제된 Organization 입니다.");
            navigate("/organization");
          }
        }
      } catch (error) {
        console.error("Error: ", error);
        alert("처리 중 오류가 발생했습니다.");
      }
  }
};

  return (
    <div style={{ minHeight: '98vh', backgroundImage: `url(${backgroundImage})`, backgroundSize: 'cover', backgroundPosition: 'center', backgroundRepeat: 'repeat-y' }}>
       <OrganizationContainer 
       OrgName={organization?.name} 
       OrgEmoji={organization?.emoji} 
       OrgId={organizationId} 
       removeOrganization={removeOrganization} 
       handleOpenOrganizationModal={handleOpenOrganizationModal}/> 
      {organization && (
          <>            
              <h1 style={{ paddingLeft: "23vw", marginBottom: "0px" }}>{organization.name}</h1>
              <h4 style={{ paddingLeft: "23vw", marginTop: "5px" }}>{notes.length}개의 노트</h4>
              <NotesContainer>
                  <NoteContainer>
                    <BookWrapper>
                      <BookItems>
                        <MainBookWrap>
                            <BookCover>
                              <BookInside />
                              <BookImage onClick={handleButtonClick}>
                                <img src={AddNoteIcon} alt={`Add-Note`} style={{ width: '240px' }}/>
                                <Effect />
                                <Light style={{ backgroundColor: "#FFBE98" }} />
                              </BookImage>
                            </BookCover>
                        </MainBookWrap>
                      </BookItems>
                    </BookWrapper>
                  </NoteContainer>
                  {notes.map((note, index) => (
                      <NoteCard note={note} index={index} key={note.id} />
                  ))}
              </NotesContainer>
          </>
      )}
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
            path={`/organization/${organizationId}/${note.id}/${note.pageId}`}
            key={note.id}
          />
        ))}
      </Routes>
    </div>
  );
}

const StyledImage = styled.img`
  width: ${(props) => (props.$isDefaultImage ? "100%" : "100px")}; // 예시 크기
  height: 320px;
  max-height: 80%;
  width: 100%; /* 너비를 최대값으로 설정 */
  object-fit: contain;
`;

const NoteName = styled.p`
  color: #000000;
  font-size: 17px;
  // text-decoration: underline white;
  width: 180px;
  max-width: 100%;
  white-space: nowrap; /* 텍스트를 한 줄로 만들기 */
  overflow: hidden; /* 오버플로우된 텍스트 숨기기 */
  text-overflow: ellipsis; /* 오버플로우된 텍스트를 말줄임표로 표시 */
  display: block;
  margin: 0px 40px;
  margin-top: -105px;
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
  z-index: 2;
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
  overflow-x: hidden;
  overflow-y: hidden;
  padding-bottom: 30px;
  padding-left: 26vw;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 20px;
`;

const NoteContainer = styled.div`
  display: flex;
  flex-direction: row; // 항목을 세로로 정렬
  align-items: center; // 항목들을 가운데 정렬
  width: 180px; // 너비 고정
  height: 320px;
  margin: 20px 100px; // 주변 여백
  margin-left: 0px;
  margin-bottom: 60px;
  text-align: center;
  cursor: default;

  @media (max-width: 768px) {
    width: 100%;
  }
`;

const BookWrapper = styled.div`
  width: 900px;
  position: relative;
  margin: 0 auto;
  column-count: 3;
  column-gap: 12px;
  padding: 4px;
`;

const BookItems = styled.div`
  position: relative;
  cursor: default;
  padding: 16px;
  margin: 0;
  display: grid;
  break-inside: avoid;
`;

const MainBookWrap = styled.div`
  position: relative;
`;

const BookCover = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
`;

const BookInside = styled.div`
  position: absolute;
  width: 90%;
  height: 80%;
  top: 1%;
  left: 16px;
  border: 1px solid #00000010;
  border-right: 1px solid grey;
  border-radius: 2px 6px 6px 2px;
  background: white;
  box-shadow: 10px 40px 40px -10px #00000030, inset -2px 0 0 grey,
    inset -3px 0 0 #dbdbdb, inset -4px 0 0 white, inset -5px 0 0 #dbdbdb,
    inset -6px 0 0 white, inset -7px 0 0 #dbdbdb, inset -8px 0 0 white,
    inset -9px 0 0 #dbdbdb;
`;

const BookImage = styled.div`
  line-height: 0;
  position: relative;
  width: 220px;
  height: 320px;
  max-height: 80%;
  border-radius: 2px 6px 6px 2px;
  box-shadow: 6px 6px 18px -2px rgba(0, 0, 0, 0.2),
    24px 28px 40px -6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease-in-out;
  transform: perspective(2000px) rotateY(-15deg) translateX(-10px) scaleX(0.94);
  background-color: white;
  cursor: pointer;

  &:hover {
    transform: perspective(2000px) rotateY(0deg) translateX(0px) scaleX(1);
    transform-style: preserve-3d;
    box-shadow: 6px 6px 12px -1px rgba(0, 0, 0, 0.1),
      20px 14px 16px -6px rgba(0, 0, 0, 0.1);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 2px 6px 6px 2px;
  }
`;

const Effect = styled.div`
  position: absolute;
  width: 20px;
  height: 100%;
  margin-left: 16px;
  top: 0;
  border-left: 4px solid #00000010;
  background-image: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.2) 0%,
    rgba(255, 255, 255, 0) 100%
  );
  transition: all 0.5s ease;
  z-index: 5;

  ${BookImage}:hover & {
    margin-left: 14px;
  }
`;

const Light = styled.div`
  width: 90%;
  height: 100%;
  position: absolute;
  border-radius: 3px;
  background-image: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.2) 100%
  );
  
  top: 0;
  right: 0;
  opacity: 0.1;
  transition: all 0.5s ease;
  z-index: 4;
`;

export default NotePage;