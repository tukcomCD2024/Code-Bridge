import React from 'react';
import ReactDOM from 'react-dom';
import { createGlobalStyle } from "styled-components";

import App from "./App";

const GlobalFontStyle = createGlobalStyle`
  * {
    font-family: "Spoqa Han Sans Neo", "Tossface","sans-serif";
  }
`;

const GlobalStyle = createGlobalStyle`
  .App {
    text-align: center;
  }
`;

localStorage.clear();

ReactDOM.render(
  <>
      <App />
      <GlobalStyle />
      <GlobalFontStyle />
  </>,
  document.getElementById("root")
);