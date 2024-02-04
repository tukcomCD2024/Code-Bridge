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
      formData.append("image", selectedFile);

      $.ajax({
        // 의미?
        url: "/api/upload",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
          console.log("Upload successful:", data);
          alert("Upload successful");
        },
        error: function (error) {
          console.error("Error uploading image", error);
          alert("Upload failed");
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
