import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from 'react-router-dom';
import styled from "styled-components";

// prosemirror 라이브러리(리치 텍스트 에디터)
import { Schema, DOMParser } from "prosemirror-model";
import { EditorState, Selection, Plugin } from "prosemirror-state";
import { EditorView } from "prosemirror-view";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { exampleSetup } from "prosemirror-example-setup";
import { keymap } from "prosemirror-keymap";

// yjs 라이브러리(동시편집)
import { WebsocketProvider } from "y-websocket";
import { getYDocInstance } from "./utils/YjsInstance";
import { ySyncPlugin, yCursorPlugin, yUndoPlugin, undo, redo  } from "y-prosemirror";

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

import toastr from 'toastr';
import 'toastr/build/toastr.css';

import { v4 as uuidv4 } from "uuid";

function Page() {
  const editorRef = useRef(null);
  const nickname = localStorage.getItem('nickname');
  const userId = localStorage.getItem('userId');

  const location = useLocation();
  const navigate = useNavigate();
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
      guid: { default: "" }, // Ensure guid attribute is included
      nickname: { default: nickname },
    },
    parseDOM: [
      {
        tag: "p",
        getAttrs: (dom) => ({guid: dom.getAttribute("data-guid"), nickname: dom.getAttribute("data-nickname"),}),
      },
    ],
    toDOM(node) {
      return ["p", { class: node.attrs.class, "data-guid": node.attrs.guid, "data-nickname": node.attrs.nickname}, 0];
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
    marks: basicSchema.spec.marks,
  });

  useEffect(() => {
    if (!editorRef.current) return;

    const roomId = noteId;
    const ydoc = getYDocInstance(roomId);
    const provider = new WebsocketProvider(
      // "wss://demos.yjs.dev/ws", // 웹소켓 서버 주소(데모용)
      //"ws://localhost:4000", //배포용
      //"ws://nodejs:4000", 
      "wss://sharenote.shop/ws",
      roomId, // 방 이름
      ydoc
    );
    const yXmlFragment = ydoc.getXmlFragment("prosemirror");
    const connectedUsersYMap = ydoc.getMap('connectedUsers');
    const lineLocks = ydoc.getMap('nodeInfo');
    const userLocks = ydoc.getMap('userLocks');

    provider.on("sync", (isSynced) => {
      const nicknameWithSuffix = `${nickname}_다중 접속`;
      if (isSynced) {
        const isSingleConnected = connectedUsersYMap.has(nickname);
        const isMultiConnected = connectedUsersYMap.has(nicknameWithSuffix);
    
        if (isSingleConnected && isMultiConnected) {
          const isConfirmed = window.confirm("동시 접속 가능한 횟수를 초과하셨습니다.\n기존 접속을 종료하고 새로 접속하시겠습니까?");
          if (isConfirmed) {
            connectedUsersYMap.set(nicknameWithSuffix, 'kicked');
          } else {
            navigate(`/organization/${pathSegments[1]}`);
            return;
          }
        }
    
        let userColor = connectedUsersYMap.get(nickname) || connectedUsersYMap.get(nicknameWithSuffix) || getRandomColor();
        
        if (!isSingleConnected) {
          connectedUsersYMap.set(nickname, userColor);
          provider.awareness.setLocalStateField('user', { name: nickname, color: userColor });
        } else {
          connectedUsersYMap.set(nicknameWithSuffix, userColor);
          provider.awareness.setLocalStateField('user', { name: nicknameWithSuffix, color: userColor });
        }
        updateUsersAndColors(); // UI 업데이트
        setisloaded(true);
      }
    });
    
    function onlineUpdate(event) {
      updateUsersAndColors(); 
      const userState = provider.awareness.getLocalState();
      if (userState && userState.user && userState.user.name) {
        const nickname = userState.user.name;
        if (connectedUsersYMap.get(nickname) === 'kicked') {
          toastr.warning("연결 정보가 없습니다!");
          navigate(`/organization/${pathSegments[1]}`);
          return;
        }
      }
    }
   connectedUsersYMap.observe(onlineUpdate);

    function yjsDisconnect() {
      const keysToDelete = [];

      lineLocks.forEach((value, key) => {
        if (value === nickname) {
          keysToDelete.push(key);
        }
      });
      keysToDelete.forEach(key => lineLocks.delete(key));
      userLocks.delete(nickname);
    
      // Yjs 연결 해제 및 리소스 정리
      const userState = provider.awareness.getLocalState();
      if (userState && userState.user) {
        connectedUsersYMap.delete(userState.user.name);
      }
    
      // 연결 해제 및 리소스 정리
      view.destroy();
      provider.destroy();
      provider.disconnect();
    }  

    const generateBlockIdPlugin = (guidGenerator = uuidv4) => {
      return new Plugin({
        appendTransaction: (transactions, prevState, nextState) => {
          const tr = nextState.tr;
          let modified = false;
          const generatedIds = new Set();
        
          if (transactions.some(transaction => transaction.docChanged)) {
            const { paragraph } = nextState.schema.nodes;
            let prevNode = null; // 이전 노드를 추적하기 위한 변수
            let prevPos = null; // 이전 노드의 위치를 저장
        
            nextState.doc.descendants((node, pos) => {
              if (node.type === paragraph) {
                const nodeTextContent = node.textContent;
                const selection = nextState.selection;
                const cursorPosition = selection.head || selection.from;
        
                if (cursorPosition >= pos && cursorPosition <= pos + node.nodeSize) {
                  const cursorPositionInNode = cursorPosition - pos;
                  if (cursorPositionInNode === 1 && nodeTextContent !== "") {
                    if (prevNode && !generatedIds.has(prevNode.attrs.guid)) {
                      let newGuid;
                      do {
                        newGuid = guidGenerator();
                      } while (generatedIds.has(newGuid));
                      generatedIds.add(newGuid);
                      tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid});
                      modified = true;
                    }
                  } else {
                    // 일반적인 guid 할당 로직
                    let currentGuid = node.attrs.guid;
                    if (!currentGuid || generatedIds.has(currentGuid)) {
                      let newGuid;
                      do {
                        newGuid = guidGenerator();
                      } while (generatedIds.has(newGuid));
                      generatedIds.add(newGuid);
                      tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid});
                      modified = true;
                    } else {
                      generatedIds.add(currentGuid);
                    }
                  }
                }
                // 현재 노드와 위치를 이전 노드로 업데이트
                prevNode = node;
                prevPos = pos;
              }
            });
          }
          return modified ? tr : null;
        },
      });
    };

    // 줄 잠금/해제 함수
    window.toggleLineLock = function(guid, nickname) {
      if(!nickname || !userId) {
        toastr.info(`로그인 정보가 없습니다.`);
        navigate("/login");
        return;
      }

    const currentLock = lineLocks.get(guid.toString());

    // 현재 사용자가 이미 다른 노드를 잠근 경우, 알림창 표시
    const currentLockedNodeByUser = userLocks.get(nickname);
    if (!currentLock && currentLockedNodeByUser && currentLockedNodeByUser !== guid.toString()) {
      // 사용자에게 확인을 요청하는 대화 상자 표시
      const isConfirmed = window.confirm("최대 1개까지 잠금이 가능합니다.\n이전에 설정한 잠금을 해제하시겠습니까?");
      if (isConfirmed) {
        lineLocks.delete(currentLockedNodeByUser);
        userLocks.delete(nickname); 
      } else {
        return;
      }
    }
    
      if (currentLock) {
        // 해당 줄이 이미 잠겨 있고, 현재 사용자가 잠근 경우 잠금 해제
        if (currentLock === nickname) {
            lineLocks.delete(guid.toString());
            userLocks.delete(nickname);
            toastr.info(`편집 잠금이 해제되었습니다.`);
        } else {
          toastr.error(`[오류] ${currentLock} 에 의해 잠금 불가합니다.`);
        }
    } else {
      lineLocks.set(guid.toString(), nickname);
      userLocks.set(nickname, guid.toString());
      toastr.success(`블록 편집 잠금이 설정되었습니다.`);
    }
  };

    const handleNodeClick = (nickname, event) => {
      const { target, clientX, clientY } = event;
      const coords = { left: clientX, top: clientY };
      const posAtCoords = view.posAtCoords(coords);
      if (!posAtCoords) return;

      if (target.tagName === 'P' && target.hasAttribute('data-guid')) {
        const guid = target.getAttribute('data-guid');
        const currentLock = lineLocks.get(guid.toString());
        if (currentLock) {
          if (currentLock !== nickname) {
            toastr.warning(`[알림] ${currentLock} 에 의해 편집 불가합니다.`);
          }
        }
      }
    };

    const handleEditAttempt = (nickname, event) => {
      // keydown 이벤트의 경우, 커서 위치의 노드를 찾아야 합니다.
      let targetNode;
      if (event.type === 'keydown') {
        const selection = document.getSelection();
        if (selection.rangeCount > 0) {
          const range = selection.getRangeAt(0);
          targetNode = range.startContainer.parentNode; // 커서 위치의 상위 노드를 타겟으로 설정
        }
      } else {
        // mousedown 이벤트의 경우, 이벤트 타겟을 직접 사용
        targetNode = event.target;
      }

      if (!targetNode) return; // 타겟 노드가 없으면 함수 종료

      // 타겟 노드가 'P' 태그이고 'data-guid' 속성을 가지고 있는지 확인
      if (targetNode.tagName === 'P' && targetNode.hasAttribute('data-guid')) {
        const guid = targetNode.getAttribute('data-guid');
        const currentLock = lineLocks.get(guid);
        if (currentLock && currentLock !== nickname) {
          event.preventDefault(); // 편집 방지
          if (document.activeElement) { // 포커스(커서) 해제
            document.activeElement.blur();
          }
        }
      }
    };

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

      return cursor;
    };

    connectedUsersYMap.observe(updateUsersAndColors);
    window.addEventListener("pagehide", yjsDisconnect);
    window.addEventListener("unload", yjsDisconnect);
    window.addEventListener("popstate", yjsDisconnect);

    editorRef.current.addEventListener('mousedown', (event) => { handleNodeClick(nickname, event); });
    editorRef.current.addEventListener('keydown', (event) => { handleEditAttempt(nickname, event); });
    editorRef.current.addEventListener('mousedown', (event) => { handleEditAttempt(nickname, event); });

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
          generateBlockIdPlugin(),
          imagePlugin({
            ...imageSettings,
            resizeCallback: (el, updateCallback) => {
              const observer = new ResizeObserver(entries => {
                window.requestAnimationFrame(() => {
                  updateCallback();
                });
              });
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
      connectedUsersYMap.unobserve(onlineUpdate);
      window.removeEventListener("pagehide", yjsDisconnect);
      window.removeEventListener("unload", yjsDisconnect);
      window.removeEventListener("popstate", yjsDisconnect);
      yjsDisconnect();

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
          <NavigationBar $isloaded={isloaded.toString()}>
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
                width: "100%",
                margin: "0 auto",
                paddingLeft: "8%",
                paddingRight: "5%",
              }}
              
            />

        </EditorContainer>
        </LayoutContainer>
    </div>
  );
}

const LayoutContainer = styled.div`
  display: flex;
`;

const EditorContainer = styled.div`
  flex: 1;
  display: flex;
  height: 200vh;
  margin-left: 15%; // 네비게이션 바 너비만큼 왼쪽 여백 추가
`;

const NavigationBar = styled.div`
  width: 13%; // 네비게이션 바 너비
  background-color: #eee; // 네비게이션 바 배경색
  position: fixed;
  height: 93%; // 전체 화면 높이
  padding: 20px; // 여백
  visibility: ${(props) => (props.$isloaded === "true" ? "visible" : "hidden")};
  // border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 5px;
  box-shadow: 6px 8px 4px #ccc;
  
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

@media (max-width: 768px) {
    visibility: hidden;
  }
`;

const Notename = styled.div`
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap; /* 텍스트를 한 줄로 만들기 */
  overflow: hidden; /* 오버플로우된 텍스트 숨기기 */
  text-overflow: ellipsis; /* 오버플로우된 텍스트를 말줄임표로 표시 */
`;

export default Page;