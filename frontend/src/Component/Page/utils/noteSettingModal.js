import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import styled from "styled-components";
import ImagetoBackend from "../../imageToBackend";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrashCan } from "@fortawesome/free-solid-svg-icons";
import toastr from "toastr";
import "toastr/build/toastr.css";

const NoteSettingModal = ({
    modalOpen,
    handleCloseModal,
    myimage,
    uploadImage,
    note,
  }) => {
    const modalRef = useRef();
    const navigate = useNavigate();
    const location = useLocation();
  
    const [noteNameInput, setNoteNameInput] = useState(""); // 사용자 이메일 입력 상태 관리
    const [isModifyButtonDisabled, setIsModifyButtonDisabled] = useState(false);

    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const organizationId = pathSegments[1];
    const noteId = pathSegments[2];
  
    // 노트 제목 입력 처리 함수
    const handleNoteNameInputChange = (event) => {
      setNoteNameInput(event.target.value);
    };
      
    // 노트 정보 수정 함수
    const handleModify = async () => {
      const title = noteNameInput || note?.name || "노트 정보 없음";
      const noteImageUrl = localStorage.getItem('recentImageUrl') || 'https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/NoneImage2.png';
      const endpoint = "/api/user/note";
      try {
        setIsModifyButtonDisabled(true);
        const response = await fetch(endpoint, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ title, noteImageUrl, organizationId, noteId }),
        });
        if (response.ok) {
            toastr.info("노트 수정 완료!");
            setNoteNameInput("");
        } 
      } catch (error) {
        alert("처리에 실패했습니다.");
        console.error('Error:', error);
      }
      setIsModifyButtonDisabled(false);
    };
  
    const handleKeyPress = (event) => {
      if (event.key === "Enter") {
        handleModify();
      }
    };

    const removeNote = async () => {
      if (note?.name == null) {
        toastr.info("정보를 불러오지 못했습니다.");
        navigate("/main");
        return;
      }
  
      const isConfirmed = window.confirm(`"${note?.name}" 의 모든 노트 데이터를 삭제하시겠습니까?`);
    
      if (isConfirmed) {
        try {
          const response = await fetch("/api/user/note", {
            method: "DELETE",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ organizationId, noteId }),
          });
          const contentType = response.headers.get('content-type');
          // 삭제 성공
          if (response.ok) {
            if (contentType && contentType.includes('text/plain')) {
              navigate(`/organization/${organizationId}`);
              toastr.info("정상적으로 삭제되었습니다.");
            }
          // 비정상적 상황
          } else {
              alert("이미 삭제된 Note 입니다.");
              navigate("/main");
          } 
        } catch (error) {
          console.error("Error: ", error);
          alert("처리 중 오류가 발생했습니다.");
        }
    }
    };
  
    useEffect(() => {
      if (note?.name == null) {
        toastr.options.positionClass = "toast-top-right";
        toastr.info("정보를 불러오지 못했습니다.");
        navigate("/main");
      }
    }, [note, navigate]);
  
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
  
    if (!modalOpen) return null;
  
    return (
      <ModalContainer>
        <ModalContent ref={modalRef}>
          <CloseButton onClick={handleCloseModal} style={{ color: "red" }}>
            X
          </CloseButton>
          <LeftContainer>
            <p>| 노트 정보 |</p>
            <LeftInsideContainer>
              <p style={{ fontWeight: "bold", fontSize: "20px", whiteSpace: "nowrap", overflow: "hidden", textOverflow:"ellipsis", maxWidth: "270px" }}>
              {noteNameInput || note?.name || "노트 정보 없음"}
              </p>
              <img src={ myimage || note?.image || 'https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/NoneImage2.png'} alt="Note" />
            </LeftInsideContainer>
          </LeftContainer>
          <RightContainer>
            <TopContainer>
              <Title>&nbsp;&nbsp;⚒ 노트 정보 수정하기</Title>
              <NoteModifyWrapper>
                <Title>🔸 노트 제목</Title>
                <NoteNameInput
                  type="text"
                  value={noteNameInput}
                  onKeyPress={handleKeyPress}
                  onChange={handleNoteNameInputChange} // 입력 값 변경 처리
                  placeholder={note.name}
                />
                <Title style={{ marginBottom:"-20px" }}>🔸 노트 대표 이미지</Title>
                <ImagetoBackend onImageUpload={uploadImage}/>
                <SendButton 
                onClick={handleModify} 
                disabled={isModifyButtonDisabled} 
                style={{ cursor: isModifyButtonDisabled ? 'not-allowed' : 'pointer'}}>수정하기</SendButton>
              </NoteModifyWrapper>
            </TopContainer>
            <BottomContainer>
              <FontAwesomeIcon icon={faTrashCan} onClick={removeNote} style={{ color: "#707070", marginLeft:"auto", fontSize: "30px", cursor: "pointer"}} title="노트 삭제"/>
            </BottomContainer>
          </RightContainer>
        </ModalContent>
      </ModalContainer>
    );
  };
  
  const ModalContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  width: 100%;
  height: 100%;
  position: fixed;
  top: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.7);
`;

const ModalContent = styled.div`
  display: flex;
  position: relative;
  text-align: center;
  width: 800px;
  padding: 15px;
  border-radius: 10px;
  background: #ffffff;
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
  width: 70%;
  height: 70%;
  padding: 0px 20px;
  padding-bottom: 25px;
  border-radius: 8px;
  background-color: rgba(250, 190, 88, 0.2); 

  img{
    width: 100%;
    height: auto;
    max-height: 180px;
  }
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
  font-size: 16px;
  text-align: left; // 텍스트를 왼쪽으로 정렬
`;

const BottomContainer = styled.div`
  flex: 2; // 하단 컨테이너가 차지하는 공간 비율
  flex-direction: column; // 자식 요소들을 수직 방향으로 나열
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0px 10px; // 내부 여백 추가
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

const NoteModifyWrapper = styled.div`
  display: flex;
  flex-direction: column;  // 요소들을 수직 방향으로 나열
  align-items: center;  // 요소들을 가로축 중앙에 정렬
  justify-content: center;  // 요소들을 세로축 중앙에 정렬

  width: 100%;
  padding: 8px 18px;
  margin-top: 10px;
  border: 1px solid #d0d0d0;
  height: 100%;  // 높이를 100%로 설정 (부모 컨테이너에 따라 달라질 수 있음)
  
  border-radius: 8px;  // 테두리 둥글게 처리
  position: relative;
  box-sizing: border-box;  // 패딩과 보더가 너비와 높이에 포함되도록 설정
  text-align: center;  // 텍스트 중앙 정렬
  line-height: 40px;  // 라인 높이 설정
`;
const NoteNameInput = styled.input`
  flex-grow: 1;
  width: 90%;
  border: none;
  outline: none;
  padding: 10px;
  margin-bottom: 5px;
  background-color: #ffffff;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
`;

const SendButton = styled.button`
  padding: 8px; // 버튼 내부 여백
  margin-left: auto;
  border-radius: 4px; // 테두리 둥글게
  background-color: #007bff; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경

  &:hover {
    background-color: #0056b3; // 마우스 오버 시 버튼 배경색 변경
  }
`;

export default NoteSettingModal;