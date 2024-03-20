import React, { useEffect, useRef, useState } from "react";
import { useLocation } from 'react-router-dom';
import styled from "styled-components";

// prosemirror 라이브러리(리치 텍스트 에디터)
import { Schema, DOMParser } from "prosemirror-model";
import { EditorState, Selection } from "prosemirror-state";
import { EditorView } from "prosemirror-view";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { exampleSetup } from "prosemirror-example-setup";
import { keymap } from "prosemirror-keymap";

// yjs 라이브러리(동시편집)
import { WebsocketProvider } from "y-websocket";
import { getYDocInstance } from "./utils/YjsInstance";
import { ySyncPlugin, yCursorPlugin, yUndoPlugin, undo, redo } from "y-prosemirror";

import { updateImageNode, imagePlugin } from "prosemirror-image-plugin";
import "./ProseMirror_css/prosemirror_image_plugin/common.css";
import "./ProseMirror_css/prosemirror_image_plugin/withResize.css";
import "./ProseMirror_css/prosemirror_image_plugin/sideResize.css";
import "./ProseMirror_css/prosemirror_image_plugin/withoutResize.css";
import "./ProseMirror_css/ProseMirror.css";

import { imageSettings, imageNodeSpec } from "./utils/pageSettings";
import { inlinePlaceholderPlugin } from "./utils/inlinePlaceholderPlugin";
import { hoverButtonPlugin } from "./utils/hoverButtonPlugin";
import { checkBlockType } from "./utils/checkBlockType";
import { cursorColors } from "../Utils/cursorColor"
import loadingImage from "../../image/loading.gif";

function Page() {
  const editorRef = useRef(null);
  const nickname = localStorage.getItem('nickname');

  const location = useLocation();
  const note = location.state || { name: "노트 목록에서 접속바랍니다.", image: "null" };

  const pathSegments = location.pathname.split('/').filter(Boolean); 
  const noteId = pathSegments[2];
  
  const [isloaded, setisloaded] = useState(false); // 로딩 상태 관리
  const [usersAndColors, setUsersAndColors] = useState([]); // 연결된 사용자와 색상 상태

  const { nodes, marks } = basicSchema.spec;
  const extendedNodes = addListNodes(
    nodes.append({ image: imageNodeSpec }),
    "paragraph block*",
    "block"
  );

  const customParagraphNode = {
    ...nodes.get("paragraph"),
    attrs: {
      ...nodes.get("paragraph").attrs,
      class: { default: "custom-paragraph" },
    },
    toDOM(node) {
      return ["p", { class: node.attrs.class }, 0];
    },
  };

  const newParagraphNode = extendedNodes.update(
    "paragraph",
    customParagraphNode
  );

  const defaultNodes = updateImageNode(newParagraphNode, {
    ...imageSettings,
  });

  const mySchema = new Schema({
    nodes: defaultNodes,
    marks,
  });

  useEffect(() => {
    if (!editorRef.current) return;

    const roomId = noteId;
    const ydoc = getYDocInstance(roomId);
    const provider = new WebsocketProvider(
      //"wss://demos.yjs.dev/ws", // 웹소켓 서버 주소(데모용)
      //"ws://localhost:4000", //배포용
      //"ws://nodejs:4000", 
      "wss://sharenote.shop/ws",
      roomId, // 방 이름
      ydoc
    );
    const yXmlFragment = ydoc.getXmlFragment("prosemirror");
    const connectedUsersYMap = ydoc.getMap('connectedUsers');

    function yjsDisconnect() {
      connectedUsersYMap.delete(nickname);
    }    

    function cursorAwarenessHandler(awareness, userDiv, hideTimeout) {
      awareness.on("change", () => {
        clearTimeout(hideTimeout);
        userDiv.style.display = ""; // 사용자 이름을 다시 보이게 함
        hideTimeout = setTimeout(() => {
          userDiv.style.display = "none"; // 5초 후에 다시 사용자 이름을 숨김
        }, 5000);
      });
    }

    function getAvailableColors() {
      const usedColors = new Set();
      connectedUsersYMap.forEach((color, name) => {
        usedColors.add(color);
      });
      const availableColors = cursorColors.filter(color => !usedColors.has(color));
      return availableColors;
    }
    
    function getRandomColor() {
      const availableColors = getAvailableColors();
      const index = Math.floor(Math.random() * availableColors.length);
      return availableColors[index];
    }

    function updateUsersAndColors() {
      const updatedUsersAndColors = [];
      connectedUsersYMap.forEach((color, name) => {
        updatedUsersAndColors.push({ name, color });
      });
      setUsersAndColors(updatedUsersAndColors);
    }

    provider.on("sync", (isSynced) => {
      console.log(`동기화 상태: ${isSynced ? "완료" : "미완료"}`);
      if (isSynced) {
        setisloaded(true);
      }
    });
    
    provider.on('status', (event) => {
      if (event.status === 'connected') {
        if (connectedUsersYMap.has(nickname)) {
          alert(`${nickname}는(은) 이미 연결되어 있어 게스트로 진입합니다.`);
          return;
        }
        let userColor = connectedUsersYMap.get(nickname);
        if (!userColor) {
          userColor = getRandomColor();
          connectedUsersYMap.set(nickname, userColor);
          updateUsersAndColors();
        }
        provider.awareness.setLocalStateField('user', { name: nickname, color: userColor });
      } else if (event.status === 'disconnected') {
        yjsDisconnect();
      }
    });

    const myCursorBuilder = (user) => {
      const cursor = document.createElement("span");
      cursor.classList.add("ProseMirror-yjs-cursor");
      cursor.setAttribute("style", `border-color: ${user.color}`);
      const userDiv = document.createElement("div");
      userDiv.setAttribute("style", `background-color: ${user.color}`);
      userDiv.innerText = user.name;
      cursor.appendChild(userDiv);
      
      // 커서 색상 확인
      const usersAndColors = [];
      connectedUsersYMap.forEach((color, name) => {
        usersAndColors.push({ name, color });
      });
      console.log('연결된 사용자와 커서 색상:', usersAndColors);

      // // 일정 시간(예: 5000ms) 후에 사용자 이름을 숨기는 로직
      // let hideTimeout = setTimeout(() => {
      //   userDiv.style.display = "none"; // 사용자 이름을 숨김
      // }, 5000); // 5초 후 실행

      // // Awareness 상태 변경에 따라 사용자 이름을 다시 표시하는 로직 설정
      // cursorAwarenessHandler(provider.awareness, userDiv, hideTimeout);

      return cursor;
    };

    connectedUsersYMap.observe(updateUsersAndColors);
    window.addEventListener("beforeunload", yjsDisconnect);
    window.addEventListener("popstate", yjsDisconnect);

    const myDoc = DOMParser.fromSchema(mySchema).parse(
      document.createElement("div")
    );

    const view = new EditorView(editorRef.current, {
      state: EditorState.create({
        doc: myDoc,

        plugins: exampleSetup({ schema: mySchema }).concat(
          ySyncPlugin(yXmlFragment),
          yCursorPlugin(provider.awareness, {
            cursorBuilder: myCursorBuilder,
          }),
          yUndoPlugin(),
          hoverButtonPlugin(),
          inlinePlaceholderPlugin(),
          imagePlugin({
            ...imageSettings,
            resizeCallback: (el, updateCallback) => {
              const observer = new ResizeObserver(updateCallback);
              observer.observe(el);
              return () => observer.unobserve(el);
            },
          }),
          // checkBlockType(),
          keymap({
            "Mod-z": undo,
            "Mod-y": redo,
            "Mod-Shift-z": redo,
          })
        ),
        selection: Selection.atStart(myDoc),
      }),
    });

    editorRef.current.view = view;

    return () => {
      connectedUsersYMap.unobserve(updateUsersAndColors);
      window.removeEventListener("beforeunload", yjsDisconnect);
      window.removeEventListener("popstate", yjsDisconnect);
      view.destroy();
      provider.destroy();
    };
  }, []);

  return (
    <div>
      {!isloaded && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(255, 255, 255, 0.7)",
          }}
        >
          <img
            src={loadingImage}
            alt="Loading..."
            style={{
              width: "200px",
              height: "auto",
              borderBottom: "2px solid #bbbbbb",
            }}
          />
        </div>
      )}
        <LayoutContainer>
          <NavigationBar isloaded={isloaded}>
            <Notename>📖&nbsp;&nbsp;&nbsp;{note.name}&nbsp;&nbsp;&nbsp;📖</Notename>
            <br/>
            <img src={note.image} alt="Note" />
            <p />
            <hr />
            <p style={{ fontWeight: "bold" }}>접속중인 유저 목록</p>
            <p><small>(커서 색상/닉네임)</small></p>
           <ul>
            {usersAndColors.map(({ name, color }) => (
              <li key={name} style={{ display: 'flex', alignItems: 'center', marginBottom: '10px' }}>
                <div style={{ width: '20px', height: '20px', backgroundColor: color, marginRight: '10px' }}></div>
                {name} {name === nickname && "(본인)"}
              </li>
            ))}
          </ul>
          </NavigationBar>
          <EditorContainer>
            <div
              ref={editorRef}
              id="editor"
              style={{
                visibility: isloaded ? "visible" : "hidden",
                width: "90%",
                margin: "0 auto",
                paddingLeft: "5%",
              }}
            />
          </EditorContainer>
        </LayoutContainer>
    </div>
  );
}

const LayoutContainer = styled.div`
  display: flex;
  height: 100vh; // 전체 화면 높이
`;

const NavigationBar = styled.div`
  width: 15%; // 네비게이션 바 너비
  background-color: #eee; // 네비게이션 바 배경색
  padding: 20px; // 여백
  visibility: ${(props) => (props.isloaded ? "visible" : "hidden")};
  
  img {
    width: 200px; /* 너비 설정 */
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
  }

  & > p:nth-of-type(2) {
    margin-bottom: 0;
  }
  & > p:nth-of-type(3) {
    margin: 0;
  }

  @media screen and (max-width: 1500px) {
    img {
      width: auto; // 이미지 너비 자동 조정
      max-width: 100%; // 이미지가 부모 너비를 넘지 않도록
    }
  }
`;

const Notename = styled.div`
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap; /* 텍스트를 한 줄로 만들기 */
  overflow: hidden; /* 오버플로우된 텍스트 숨기기 */
  text-overflow: ellipsis; /* 오버플로우된 텍스트를 말줄임표로 표시 */
`;

const EditorContainer = styled.div`
  flex: 1;
  display: flex;
`;

// 토글 스위치 컨테이너
// const ToggleSwitch = styled.div`
//   position: absolute;
//   top: 10px;
//   right: 10px;
//   width: 50px;
//   height: 24px;
//   border-radius: 12px;
//   background-color: ${(props) =>
//     props.active ? "#007bff" : "#ccc"}; // active 상태에 따라 배경색 변경
//   display: flex;
//   align-items: center;
//   cursor: pointer;
//   justify-content: ${(props) => (props.active ? "flex-end" : "flex-start")};
// `;

// 토글 버튼
// const ToggleButton = styled.div`
//   width: 22px;
//   height: 22px;
//   border-radius: 50%;
//   background-color: white;
//   transition: all 0.3s ease;
// `;

export default Page;