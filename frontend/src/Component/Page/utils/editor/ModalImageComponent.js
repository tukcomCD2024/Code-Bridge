import React, { useState, useEffect, useRef } from "react";
import styled from "styled-components";

const ModalImageComponent = ({
  src, 
  modalOpen,
  modalClose
}) => {
  const modalRef = useRef();
  const [isOverflow, setisOverflow] = useState(false);
  const [imageSize, setImageSize] = useState(40);

  const handleImageSizeChange = (increment) => {
    setImageSize((prevSize) => Math.max(10, Math.min(100, prevSize + increment)));
  };

  const handleDropdownChange = (event) => {
    const selectedSize = parseInt(event.target.value, 10);
    setImageSize(selectedSize);
  };

  useEffect(() => {
    const handleResizeCheck = () => {
      if (modalRef.current) {
        setisOverflow(modalRef.current.scrollHeight > modalRef.current.clientHeight);
      }
    };

    handleResizeCheck(); // 초기에 한번 호출
    window.addEventListener("resize", handleResizeCheck);
    return () => {
      window.removeEventListener("resize", handleResizeCheck);
    };
  }, []);

  useEffect(() => {
    if (modalRef.current) {
      setisOverflow(modalRef.current.scrollHeight > modalRef.current.clientHeight ? "true" : "false");
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
      <CloseButton isOverflow={isOverflow} onClick={modalClose}>&times;</CloseButton>
      <ModalContent imageSize={imageSize}>
        <ModalImage src={src} alt="modal" />
      </ModalContent>
      <ButtonContainer>
        <SizeButton onClick={() => handleImageSizeChange(10)}>+</SizeButton>
        <SizeButton onClick={() => handleImageSizeChange(-10)}>-</SizeButton>
      </ButtonContainer>
      <DropdownContainer>
        <SizeDropdown value={imageSize} onChange={handleDropdownChange}>
          {[10, 20, 30, 40, 50, 60, 70, 80, 90, 100].map(size => (
            <option key={size} value={size}>
              {size}%
            </option>
          ))}
        </SizeDropdown>
      </DropdownContainer>
    </ModalContainer>
  );
};


export default ModalImageComponent;

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
  overflow: auto;
`;

const ModalContent = styled.div`
  background-color: #fff;
  max-width: ${({ imageSize }) => imageSize}%;
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
  right: ${({ isOverflow }) => (isOverflow ? "30px" : "10px")};
  z-index: 20; 
`;

const ModalImage = styled.img`
  max-width: 100%;
  height: auto;
`;

const ButtonContainer = styled.div`
  position: fixed;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: row;
`;

const SizeButton = styled.button`
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  padding: 0px 10px;
  margin: 5px;
  cursor: pointer;
  font-size: 29px;
  font-weight: bold;

  &:hover {
    background-color: #0056b3;
  }
`;

const DropdownContainer = styled.div`
  position: fixed;
  top: 70px; /* Adjust as needed */
  left: 15px;
`;

const SizeDropdown = styled.select`
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 4px;
  font-size: 16px;
  cursor: pointer;
`;