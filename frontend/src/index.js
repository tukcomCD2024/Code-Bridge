import React from 'react';
import ReactDOM from 'react-dom';
import { createGlobalStyle } from "styled-components";

import App from "./App";
import "./styles.css";

const GlobalFontStyle = createGlobalStyle`
    @import url("https://spoqa.github.io/spoqa-han-sans/css/SpoqaHanSansNeo.css");
    @import url("https://cdn.jsdelivr.net/gh/toss/tossface/dist/tossface.css");

  * {
    font-family: "Spoqa Han Sans Neo", "Tossface","sans-serif";
  }
`;

const GlobalStyle = createGlobalStyle`
  .App {
    text-align: center;
  }
`;

ReactDOM.render(
  <>
      <App />
      <GlobalStyle />
      <GlobalFontStyle />
  </>,
  document.getElementById("root")
);