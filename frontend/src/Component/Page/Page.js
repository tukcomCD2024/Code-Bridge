import React, { useEffect, useRef } from "react";
import { Schema, DOMParser } from "prosemirror-model";
import { EditorState, Plugin, Selection } from "prosemirror-state";
import { EditorView, Decoration, DecorationSet } from "prosemirror-view";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { exampleSetup } from "prosemirror-example-setup";
import {
  defaultSettings,
  updateImageNode,
  imagePlugin,
} from "prosemirror-image-plugin";
import toastr from "toastr";

import "./ProseMirror_css/image/common.css";
import "./ProseMirror_css/image/withResize.css";
import "./ProseMirror_css/image/sideResize.css";
import "./ProseMirror_css/image/withoutResize.css";
import "./ProseMirror_css/ProseMirror.css";
import "toastr/build/toastr.css";

const ImageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 50,
  maxSize: 550,
};

function inlinePlaceholderPlugin(placeholderText = "내용을 입력하세요...") {
  return new Plugin({
    props: {
      decorations(state) {
        const decorations = [];
        const doc = state.doc;
        const { from, to } = state.selection;

        if (from !== to) return DecorationSet.empty;

        doc.descendants((node, pos) => {
          if (
            node.isBlock &&
            node.content.size === 0 &&
            !node.type.spec.isCode &&
            from >= pos &&
            from <= pos + node.nodeSize
          ) {
            const placeholder = document.createElement("span");
            placeholder.textContent = placeholderText;
            placeholder.className = "editor-inline-placeholder";
            const deco = Decoration.widget(pos + 1, placeholder, { side: 1 });
            decorations.push(deco);
          }
        });
        return DecorationSet.create(doc, decorations);
      },
    },
  });
}

// Prosemirror 에디터 내부에서 드래그 앤 드롭방식으로 이미지 노드 이동 플러그인
function imageDragDropPlugin() {
  return new Plugin({
    view(editorView) {
      let draggingImage = null; // 드래그 중인 이미지 정보
      let dragStartPos = null; // 드래그 시작 위치
      let dragEndPos = null; // 드래그 종료 위치

      const handleDragStart = (e) => {
        // 이미지 노드를 드래그 시작할 때의 처리
        const img = e.target.closest("img");
        if (img) {
          const pos = editorView.posAtCoords({
            left: e.clientX,
            top: e.clientY,
          });
          if (pos) {
            dragStartPos = pos.pos;
            draggingImage = {
              src: img.getAttribute("src"),
              alt: img.getAttribute("alt"),
              title: img.getAttribute("title"),
            };
            e.dataTransfer.effectAllowed = "move";
          }
        }
      };

      const handleDragOver = (e) => {
        e.preventDefault(); // 기본 동작 방지
        e.dataTransfer.dropEffect = "move";
        const pos = editorView.posAtCoords({ left: e.clientX, top: e.clientY });
        if (pos) {
          dragEndPos = pos.pos;
        }
      };

      const handleDrop = (e) => {
        e.preventDefault();
        if (draggingImage && dragStartPos !== null && dragEndPos !== null) {
          const { state, dispatch } = editorView;
          let tr = state.tr;
          // 이미지 삭제
          tr.delete(dragStartPos, dragStartPos + 1);
          // 드랍 위치에 이미지 삽입
          const imageNode = state.schema.nodes.image.create(draggingImage);
          tr.insert(dragEndPos, imageNode);
          dispatch(tr);
          draggingImage = null; // 드래그 중인 이미지 정보 초기화
        }
      };

      const handleDragEnd = (e) => {
        // 드래그 종료 처리
        draggingImage = null;
      };

      editorView.dom.addEventListener("dragstart", handleDragStart);
      editorView.dom.addEventListener("dragover", handleDragOver);
      editorView.dom.addEventListener("drop", handleDrop);
      editorView.dom.addEventListener("dragend", handleDragEnd);

      return {
        destroy() {
          editorView.dom.removeEventListener("dragstart", handleDragStart);
          editorView.dom.removeEventListener("dragover", handleDragOver);
          editorView.dom.removeEventListener("drop", handleDrop);
          editorView.dom.removeEventListener("dragend", handleDragEnd);
        },
      };
    },
  });
}

// checkContentUnderCursor() 노트 블록에 텍스트나 이미지가 입력되어 있을 경우 이미지 드래그 앤 드롭을 막는다.
function checkContentUnderCursor() {
  return new Plugin({
    view(editorView) {
      let dragOverPosition = null;

      const { dom } = editorView;
      const checkContentUnderCursor = (event) => {
        const selection = editorView.state.selection;
        let containsTextOrImage = false;

        if (selection.empty) {
          const coords = { left: event.clientX, top: event.clientY };

          // 화면상의 좌표를 문서 내의 포지션으로 변환
          const pos = editorView.posAtCoords(coords);
          if (pos) {
            const $pos = editorView.state.doc.resolve(pos.pos);
            const parentNode = $pos.node($pos.depth);

            // 노드가 비어 있는지 먼저 확인
            if (parentNode.content.size === 0) {
              console.log(parentNode.type.name + "false");
              containsTextOrImage = false;
            } else {
              // 노드가 비어 있지 않다면, 각 자식 노드를 검사
              parentNode.forEach((childNode) => {
                if (
                  childNode.type.name === "text" ||
                  childNode.type.name === "image"
                ) {
                  containsTextOrImage = true;
                }
              });
            }
          }
        }
        return containsTextOrImage;
      };

      const captureDrop = (event) => {
        if (checkContentUnderCursor(event)) {
          event.preventDefault();
          toastr.info("이미지는 비어있는 블록에 입력하세요.");

          const { state, dispatch } = editorView;
          let { tr } = state;

          if (dragOverPosition !== null) {
            const $dropPos = state.doc.resolve(dragOverPosition);
            // 현재 위치에서 다음 줄(노드) 존재 여부 확인
            let nextPos = $dropPos.pos + 1;
            let $nextPos = state.doc.resolve(nextPos);
            let nextNode = $nextPos.nodeAfter;

            // 다음 줄에 노트 블록이 존재하지 않거나 다음 줄이 비어있는 노트 블록이 아니면 새 줄 삽입
            if (!nextNode || nextNode.content.size !== 0) {
              const position = dragOverPosition;
              const newNode = state.schema.nodes.paragraph.create();
              tr = tr.insert(position, newNode);

              const newPos = position + 1; // 새 노트 블록 내부의 커서 위치
              tr = tr.setSelection(Selection.near(tr.doc.resolve(newPos)));
            } else {
              // 다음 줄에 노트 블록이 존재하는 경우, 커서만 해당 위치로 이동
              const newPos = $dropPos.pos + (nextNode ? nextNode.nodeSize : 1);
              tr = tr.setSelection(Selection.near(tr.doc.resolve(newPos)));
            }

            // 트랜잭션을 적용하여 문서 상태를 업데이트하고 커서를 이동
            dispatch(tr);
            editorView.focus();

            // 위치 사용 후 초기화
            dragOverPosition = null;
          }
        }
      };

      const captureDragOver = (event) => {
        if (checkContentUnderCursor(event)) {
          event.preventDefault();
          const coords = { left: event.clientX, top: event.clientY };
          const pos = editorView.posAtCoords(coords);
          if (pos) {
            dragOverPosition = pos.pos; // 드래그 중인 커서의 문서 내 위치를 업데이트합니다.
          }
        }
      };

      // Attach event listeners for drop and dragover events with capturing phase
      dom.addEventListener("drop", captureDrop, true);
      dom.addEventListener("dragover", captureDragOver, true);

      return {
        destroy() {
          // Remove event listeners when the editor is destroyed
          dom.removeEventListener("drop", captureDrop, true);
          dom.removeEventListener("dragover", captureDragOver, true);
        },
      };
    },
  });
}

// checkImageNoteBlock()은 이미지 블록에 텍스트 입력을 막음
function checkImageNoteBlock() {
  return new Plugin({
    props: {
      handleTextInput(view, from, to, text) {
        const { doc, selection } = view.state;
        const { $from } = selection;
        // 현재 선택 영역의 부모 노드를 확인하여 이미지 노드를 포함하고 있는지 검사
        let containsImage = false;
        $from.parent.content.forEach((node) => {
          if (node.type.name === "image") {
            containsImage = true;
          }
        });
        // 이미지를 포함하는 블록에서는 텍스트 입력을 방지
        if (containsImage) {
          const tr = view.state.tr;
          const position = $from.pos;
          const newNode = view.state.schema.nodes.paragraph.create();
          tr.insert(position + 1, newNode); // 이미지 다음에 새로운 노트 블록 삽입
          const newPos = position + 2; // 새로운 노트 블록 내부의 커서 위치

          // 커서 위치가 문서 범위를 벗어나지 않도록 조정
          const resolvedPos = tr.doc.resolve(
            Math.min(newPos, tr.doc.content.size)
          );
          view.dispatch(tr.setSelection(Selection.near(resolvedPos)));
          toastr.warning("이미지 블록에 텍스트 입력불가");

          return true; // 이벤트 처리 중단
        }
        // 기본 입력 처리를 계속 진행
        return false;
      },
    },
  });
}

const imageNodeSpec = {
  inline: true,
  group: "inline",
  attrs: {
    src: {},
    alt: { default: null },
    title: { default: null },
  },
  parseDOM: [
    {
      tag: "img[src]",
      getAttrs: (dom) => ({
        src: dom.getAttribute("src"),
        alt: dom.getAttribute("alt"),
        title: dom.getAttribute("title"),
      }),
    },
  ],
  toDOM: (node) => ["img", node.attrs],
};

function Page() {
  const editorRef = useRef(null);

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
  const customNodes = extendedNodes.update("paragraph", customParagraphNode);

  useEffect(() => {
    if (!editorRef.current) return;

    const mySchema = new Schema({
      nodes: customNodes,
      marks,
    });

    const view = new EditorView(editorRef.current, {
      state: EditorState.create({
        doc: DOMParser.fromSchema(mySchema).parse(
          document.createElement("div")
        ),
        plugins: exampleSetup({ schema: mySchema }).concat(
          checkContentUnderCursor(),
          checkImageNoteBlock(),
          imageDragDropPlugin(),
          inlinePlaceholderPlugin(),
          imagePlugin(ImageSettings)
        ),
      }),
    });

    editorRef.current.view = view;

    return () => {
      view.destroy();
    };
  }, []);

  return <div ref={editorRef} id="editor" />;
}

export default Page;
