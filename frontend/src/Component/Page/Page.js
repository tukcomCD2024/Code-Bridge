import React, { useEffect, useRef, useState } from "react";
import styled from "styled-components";

// prosemirror 라이브러리(리치 텍스트 에디터)
import { Schema, DOMParser, Node, Fragment } from "prosemirror-model";
import { EditorState, Selection, TextSelection } from "prosemirror-state";
import { EditorView } from "prosemirror-view";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { exampleSetup } from "prosemirror-example-setup";
import { keymap } from "prosemirror-keymap";


import { updateImageNode, imagePlugin } from "prosemirror-image-plugin";
import "./prosemirror_css/prosemirror_image_plugin/common.css";
import "./prosemirror_css/prosemirror_image_plugin/withResize.css";
import "./prosemirror_css/prosemirror_image_plugin/sideResize.css";
import "./prosemirror_css/prosemirror_image_plugin/withoutResize.css";
import "./prosemirror_css/ProseMirror.css";

// yjs 라이브러리(동시편집)
import { WebsocketProvider } from "y-websocket";
import { getYDocInstance } from "./utils/YjsInstance";
import {
  ySyncPlugin,
  yCursorPlugin,
  yUndoPlugin,
  undo,
  redo,
} from "y-prosemirror";

// toastr 라이브러리(토스트 메세지)
import toastr from "toastr";
import "toastr/build/toastr.css";

import { imageSettings, imageNodeSpec } from "./utils/pageSettings";
import { inlinePlaceholderPlugin } from "./utils/inlinePlaceholderPlugin";
import { hoverButtonPlugin } from "./utils/hoverButtonPlugin";
import { checkBlockType } from "./utils/checkBlockType";
import loadingImage from "../../image/loading.gif";

function Page() {
  const editorRef = useRef(null);
  const [isLoaded, setIsLoaded] = useState(false); // 로딩 상태 관리

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

  useEffect(() => {
    if (!editorRef.current) return;

    const roomId = "codebridge_2";
    const ydoc = getYDocInstance(roomId);
    const provider = new WebsocketProvider(
      "wss://demos.yjs.dev/ws", // 웹소켓 서버 주소
      roomId, // 방 이름
      ydoc
    );

    provider.on("sync", (isSynced) => {
      console.log(`동기화 상태: ${isSynced ? "완료" : "미완료"}`);
      if (isSynced) {
        setIsLoaded(true); // 동기화 완료 시 로딩 상태 업데이트
      }
    });

    const yXmlFragment = ydoc.getXmlFragment("prosemirror");

    function cursorAwarenessHandler(awareness, userDiv, hideTimeout) {
      awareness.on("change", () => {
        clearTimeout(hideTimeout);
        userDiv.style.display = ""; // 사용자 이름을 다시 보이게 함
        hideTimeout = setTimeout(() => {
          userDiv.style.display = "none"; // 5초 후에 다시 사용자 이름을 숨김
        }, 5000);
      });
    }

    const myCursorBuilder = (user) => {
      const cursor = document.createElement("span");
      cursor.classList.add("ProseMirror-yjs-cursor");
      cursor.setAttribute("style", `border-color: ${user.color}`);
      const userDiv = document.createElement("div");
      userDiv.setAttribute("style", `background-color: ${user.color}`);
      userDiv.innerText = "닉네임"; // 실제 사용자 이름으로 변경 가능
      cursor.appendChild(userDiv);

      // 일정 시간(예: 5000ms) 후에 사용자 이름을 숨기는 로직
      let hideTimeout = setTimeout(() => {
        userDiv.style.display = "none"; // 사용자 이름을 숨김
      }, 5000); // 5초 후 실행

      // Awareness 상태 변경에 따라 사용자 이름을 다시 표시하는 로직 설정
      cursorAwarenessHandler(provider.awareness, userDiv, hideTimeout);

      return cursor;
    };

    const defaultNodes = updateImageNode(newParagraphNode, {
      ...imageSettings,
    });

    const mySchema = new Schema({
      nodes: defaultNodes,
      marks,
    });

    // Alternatively, define the imageSchema here if it should be separate
    // const imageSchema = new Schema({
    //   nodes: updateImageNode(customNodes, {
    //     ...imageSettings, // Ensure this matches your requirements
    //   }),
    //   marks,
    // });

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
          checkBlockType(),
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
      view.destroy();
      provider.destroy();
    };
  }, []);

  return (
    <div>
      {!isLoaded && (
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
      <div
        ref={editorRef}
        id="editor"
        style={{
          visibility: isLoaded ? "visible" : "hidden",
          width: "80%",
          margin: "0 auto",
          paddingLeft: "15%",
        }} // Adjust width and add padding on the left.
      />
    </div>
  );
}

// 토글 스위치 컨테이너
const ToggleSwitch = styled.div`
  position: absolute;
  top: 10px;
  right: 10px;
  width: 50px;
  height: 24px;
  border-radius: 12px;
  background-color: ${(props) =>
    props.active ? "#007bff" : "#ccc"}; // active 상태에 따라 배경색 변경
  display: flex;
  align-items: center;
  cursor: pointer;
  justify-content: ${(props) => (props.active ? "flex-end" : "flex-start")};
`;

// 토글 버튼
const ToggleButton = styled.div`
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background-color: white;
  transition: all 0.3s ease;
`;

export default Page;
