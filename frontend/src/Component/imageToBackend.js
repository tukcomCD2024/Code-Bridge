import { useState, useEffect } from "react";
import styled from "styled-components";
import $ from "jquery";

const ImageUploadContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px;
`;

function ImagetoBackend({ onImageUpload }) {
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    // useEffect를 사용하여 selectedFile이 변경될 때마다 handleUpload 호출
    if (selectedFile) {
      handleUpload();
    }
  }, [selectedFile]);

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    setSelectedFile(file);
    onImageUpload(event);
  };

  const isImageFile = (file) => {
    const allowedExtensions = ["jpg", "jpeg", "png", "gif"]; // 허용된 확장자들

    // 파일 이름에서 확장자 추출
    const fileName = file.name;
    const fileExtension = fileName.split(".").pop().toLowerCase();

    // 허용된 확장자들 중에 포함되어 있는지 확인
    return allowedExtensions.includes(fileExtension);
  };

  const handleUpload = () => {
    if (selectedFile && isImageFile(selectedFile)) {
      const formData = new FormData();
      formData.append("multipartFile", selectedFile);

      $.ajax({
        //url: "http://localhost:8080/api/image",
        url: "http://sharenote.shop/api/image",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
          console.log("Upload successful: " + response.image_url);
          localStorage.setItem('recentImageUrl',response.image_url)
        },
        error: function (error) {
          console.error("Error uploading image: " + error);
          // HTTP 상태 코드와 함께 오류 메시지를 표시합니다.
          alert("이미지 업로드 실패. 에러 코드: " + error.status);
        },
      });
    }
  };

  return (
    <ImageUploadContainer>
      <ImageUploadWrapper htmlFor="ImagetoBackend">
        <ImageUploadButton>이미지 찾기</ImageUploadButton>
      </ImageUploadWrapper>
      <input
        id="ImagetoBackend"
        type="file"
        onChange={handleFileChange}
        style={{ display: "none" }}
        accept="image/jpeg, image/png, image/gif"
      />
    </ImageUploadContainer>
  );
}

const ImageUploadWrapper = styled.label`
  display: block;
  margin: 0;
  margin-top: 10px;
  cursor: pointer;
  padding: 0 80px;
`;

const ImageUploadButton = styled.span`
  display: block;
  line-height: 40px;
  width: 200px;
  border-radius: 10px;
  border-color: #cccccc;
  border-width: 1px; /* Add border-width property */
  border-style: solid; /* Add border-style property */
  background-color: #ffffff;
`;

export default ImagetoBackend;
