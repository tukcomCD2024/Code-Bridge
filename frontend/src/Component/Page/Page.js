import React, { useEffect, useRef } from "react";

// prosemirror 라이브러리(리치 텍스트 에디터)
import { Schema, DOMParser, Node } from "prosemirror-model";
import { EditorState, Selection } from "prosemirror-state";
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
// import { WebsocketProvider } from "y-websocket";
// import { getYDocInstance } from "./utils/YjsInstance";
// import {
//   ySyncPlugin,
//   yCursorPlugin,
//   yUndoPlugin,
//   undo,
//   redo,
// } from "y-prosemirror";

// toastr 라이브러리(토스트 메세지)
import toastr from "toastr";
import "toastr/build/toastr.css";

import { imageSettings, imageNodeSpec } from "./utils/pageSettings";
import { inlinePlaceholderPlugin } from "./utils/inlinePlaceholderPlugin";
import { hoverButtonPlugin } from "./utils/hoverButtonPlugin";
import { checkBlockType } from "./utils/checkBlockType";

function Page() {
  const editorRef = useRef(null);

  // yjs 문서와 웹소켓 프로바이더 초기화
  // const provider = new WebsocketProvider(
  //   "wss://your-websocket-server.com", // 여기에 실제 웹소켓 서버 주소를 입력하세요.
  //   "your-room-name", // 협업을 위한 방 이름
  //   ydoc
  // ); websocket과 webrtc의 차이점(아마? 우리는 websocketprovider를 사용해야함)
  // https://languagefight.tistory.com/112
  //https://velog.io/@sinclebear/WebSocket-%EA%B3%BC-WebRTC%EC%9D%98-%EB%B9%84%EA%B5%90
  // Room ID를 기반으로 Y.Doc와 WebrtcProvider 인스턴스 가져오기

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

    // const roomId = "codebridge";
    // const ydoc = getYDocInstance(roomId);
    //  const provider = new WebrtcProvider(roomId, ydoc);

    // const yXmlFragment = ydoc.getXmlFragment("prosemirror");

    // Update the image node with additional settings (this function needs to be defined)
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
          // ySyncPlugin(yXmlFragment),
          // yCursorPlugin(provider.awareness),
          // yUndoPlugin(),
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
          // keymap({
          //   "Mod-z": undo,
          //   "Mod-y": redo,
          //   "Mod-Shift-z": redo,
          // })
        ),
        selection: Selection.atStart(myDoc),
      }),
    });

    editorRef.current.view = view;

    return () => {
      view.destroy();
      // provider.destroy();
    };
  }, []);

  return (
    <div>
      <div
        ref={editorRef}
        id="editor"
        style={{
          width: "80%",
          margin: "0 auto",
          paddingLeft: "15%",
        }} // Adjust width and add padding on the left.
      />
    </div>
  );
}
export default Page;
