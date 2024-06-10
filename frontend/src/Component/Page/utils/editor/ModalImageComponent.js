import React, { useState, useEffect, useRef } from "react";
import styled from "styled-components";

const ModalImageComponent = ({
  src, 
  modalOpen,
  modalClose
}) => {
  const modalRef = useRef();
  const [isoverflow, setisoverflow] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      if (modalRef.current) {
        setisoverflow(modalRef.current.scrollHeight > modalRef.current.clientHeight);
      }
    };

    handleResize(); // 초기에 한번 호출
    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    if (modalRef.current) {
      setisoverflow(modalRef.current.scrollHeight > modalRef.current.clientHeight ? "true" : "false");
    }

    if (modalOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }

    return () => {
      document.body.style.overflow = "auto";
    };
  }, [modalOpen]);

  if (!modalOpen) return null;
  return (
    <ModalContainer ref={modalRef}>
      <CloseButton isoverflow={isoverflow} onClick={modalClose}>&times;</CloseButton>
      <ModalContent>
        <ModalImage src={src} alt="modal" />
      </ModalContent>
    </ModalContainer>
  );
};

export default ModalImageComponent;

const ModalContainer = styled.div`
  display: flex;
  justify-content: center;
  // align-items: flex-start;
  align-items: center;
  z-index: 10;
  width: 100%;
  height: 100%;
  position: fixed;
  top: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.7);
  overflow: auto; /* 스크롤 가능하도록 설정 */
`;

const ModalContent = styled.div`
  background-color: #fff;
  max-width: 90%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: auto;
`;

const CloseButton = styled.span`
  background-color: #fff;
  padding: 0px 5px;
  border-radius: 5px;
  color: red;
  font-weight: bold;
  font-size: 30px;
  cursor: pointer;
  position: fixed;
  top: 10px;
  right: ${({ isoverflow }) => (isoverflow ? "30px" : "10px")};
  z-index: 20; 
`;

const ModalImage = styled.img`
  max-width: 100%;
  height: auto; /* 이미지 높이를 자동으로 조정하여 비율 유지 */
`;