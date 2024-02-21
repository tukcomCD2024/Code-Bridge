import React from 'react';
import ReactDOM from 'react-dom';
import App from "./App";

// 폰트 로드 함수 정의
function loadFontCss(url, onSuccess, onError) {
    const link = document.createElement("link");
    link.href = url;
    link.rel = "stylesheet";
    link.type = "text/css";
  
    link.onload = onSuccess;
    link.onerror = onError;
  
    document.head.appendChild(link);
  }

  // 폰트 로드 실패 시 호출될 함수
function onImportError() {
    const style = document.createElement("style");
    style.innerHTML = `
      @font-face {
        font-family: "Tossface";
        src: url("https://cdn.jsdelivr.net/gh/toss/tossface/dist/TossFaceFontWeb.otf") format("opentype");
        font-display: swap;
      }
    `;
    document.head.appendChild(style);
    console.log(" @font-face loaded.");
  }

localStorage.clear();


// 외부 폰트 CSS 파일 로드 시도
loadFontCss(
    "https://cdn.jsdelivr.net/gh/toss/tossface/dist/tossface.css",
    () => console.log("Font css loaded successfully."),
    onImportError
  );

ReactDOM.render(

    <App />,document.getElementById("root")

);
