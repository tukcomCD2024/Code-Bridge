import React, { useEffect, useRef } from "react";

// prosemirror 라이브러리(리치 텍스트 에디터)
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

// toastr 라이브러리(토스트 메세지)
import toastr from "toastr";
import { checkBlockType } from "./checkBlockType";

import plus from "../../image/plus_icon.svg";
import down from "../../image/down.gif";
import down_arrow from "../../image/down_arrow.svg";
import typing from "../../image/typing.gif";

import "./ProseMirror_css/image/common.css";
import "./ProseMirror_css/image/withResize.css";
import "./ProseMirror_css/image/sideResize.css";
import "./ProseMirror_css/image/withoutResize.css";
import "./ProseMirror_css/ProseMirror.css";
import "toastr/build/toastr.css";

const imageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 30,
  maxSize: 550,
};

const imageNodeSpec = {
  inline: false,
  group: "block",
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

function inlinePlaceholderPlugin() {
  return new Plugin({
    props: {
      decorations(state) {
        const decorations = [];
        const { doc, selection } = state;

        doc.descendants((node, pos) => {
          if (!node.isBlock || !!node.textContent) return;
          if (selection.empty && selection.from === pos + 1) {
            // The selection is inside the node
            if (node.type.name === "paragraph") {
              decorations.push(
                Decoration.node(pos, pos + node.nodeSize, {
                  class: "placeholder",
                  style: "--placeholder-text: '내용을 입력하세요...';",
                })
              );
            }
          }
          //  else if (node.type.name !== "paragraph") {
          //   decorations.push(
          //     Decoration.node(pos, pos + node.nodeSize, {
          //       class: "placeholder",
          //       style: `--placeholder-text: "New ${node.type.name}";`,
          //     })
          //   );
          // }
          return false;
        });

        return DecorationSet.create(doc, decorations);
      },
    },
  });
}

function hoverButtonPlugin() {
  return new Plugin({
    view(editorView) {
      const hoverDiv = document.createElement("div");
      hoverDiv.classList.add("hoverDiv"); // CSS 클래스 적용
      document.body.appendChild(hoverDiv); // 바디에 직접 추가

      let lastPos = null;

      // hoverButton 생성 및 스타일 적용
      const hoverButton_plus = document.createElement("img");
      hoverButton_plus.src = down_arrow;
      hoverButton_plus.title = "새 블록 추가";
      hoverButton_plus.classList.add("hoverButton_plus"); // CSS 클래스 적용
      hoverDiv.appendChild(hoverButton_plus);

      // hoverButton_2 생성 및 스타일 적용
      const hoverButton_writer = document.createElement("img");
      hoverButton_writer.src = typing;
      hoverButton_writer.title = "작성자 확인";
      hoverButton_writer.classList.add("hoverButton_writer"); // CSS 클래스 적용
      hoverDiv.appendChild(hoverButton_writer);

      hoverButton_plus.addEventListener("click", (event) => {
        event.stopPropagation(); // 이벤트 버블링 방지

        const { state, dispatch } = editorView;
        let tr = state.tr; // 현재 문서의 트랜잭션
        const $clickPos = state.doc.resolve(lastPos);
        let insertPos;

        // 이미지 노드 바로 뒤에 새 노드 삽입
        if ($clickPos.nodeAfter && $clickPos.nodeAfter.type.name === "image") {
          // 이미지 노드 바로 뒤의 위치를 삽입 위치로 설정
          insertPos = $clickPos.pos + $clickPos.nodeAfter.nodeSize;
        } else {
          // 클릭한 위치(lastPos)를 기준으로 해당 노드의 끝 위치를 찾음
          const endOfNodePos = $clickPos.end($clickPos.depth);
          // 클릭한 노드의 바로 다음 위치에 새 노드 삽입
          insertPos = endOfNodePos + 1;
        }

        // 새 노드 삽입
        const newNode = state.schema.nodes.paragraph.create();
        tr = tr.insert(insertPos, newNode);

        // 삽입된 노드 내부에 커서 위치시키기
        const newPos = insertPos + 1; // 노드 삽입 후 새로운 위치 조정
        tr = tr.setSelection(Selection.near(tr.doc.resolve(newPos)));

        // 트랜잭션 적용
        dispatch(tr);
        editorView.focus();

        // hoverDiv 위치 업데이트
        updateButton(editorView, newPos, true);
      });

      function updateButton(view, pos, show) {
        try {
          const { doc } = view.state;
          const resolvedPos = doc.resolve(pos);

          if (resolvedPos.depth === 0 && !show) {
            hoverDiv.style.visibility = "hidden";
            return;
          }

          // 마지막 위치 업데이트
          lastPos = pos;

          let coords;

          // 이미지 노드인 경우 해당 노드의 정확한 위치를 사용
          if (
            resolvedPos.nodeAfter &&
            resolvedPos.nodeAfter.type.name === "image"
          ) {
            coords = view.coordsAtPos(resolvedPos.pos);
          } else {
            // 선택된 위치에서 가장 가까운 블록 노드의 경계를 찾습니다.
            let depth = resolvedPos.depth;
            while (depth > 0 && !resolvedPos.node(depth).isBlock) {
              depth--;
            }
            const startPos = resolvedPos.start(depth);
            // 시작 위치에 대한 좌표를 계산합니다.
            coords = view.coordsAtPos(startPos);
          }

          // 스크롤 오프셋을 고려하여 좌표 조정
          const topWithScroll = coords.top + window.scrollY;

          const editorRect = view.dom.getBoundingClientRect();
          hoverDiv.style.left = `${
            editorRect.left - hoverDiv.offsetWidth - 10
          }px`;
          hoverDiv.style.top = `${topWithScroll}px`;
          hoverDiv.style.visibility = "visible";
        } catch (error) {
          console.error("Failed to update button position:", error);
        }
      }

      function handleInteraction(event) {
        const { pos } = editorView.posAtCoords({
          left: event.clientX,
          top: event.clientY,
        });
        if (pos === null || pos === undefined) return;

        const resolvedPos = editorView.state.doc.resolve(pos);
        let node = resolvedPos.nodeAfter || resolvedPos.nodeBefore;

        if (node && node.type.name !== "image") {
          updateButton(editorView, pos, false);
          return;
        }

        updateButton(editorView, pos, true);
      }

      function handleInteractionFromCursor(pos) {
        if (pos === null || pos === undefined) return;

        const resolvedPos = editorView.state.doc.resolve(pos);
        let node = resolvedPos.nodeAfter || resolvedPos.nodeBefore;

        if (node && node.type.name !== "image") {
          updateButton(editorView, pos, false);
          return;
        }

        updateButton(editorView, pos, true);
      }

      editorView.dom.addEventListener("click", handleInteraction);
      editorView.dom.addEventListener("keydown", (event) => {
        if (event.keyCode === 13) {
          const { from } = editorView.state.selection;
          if (from !== null) {
            handleInteractionFromCursor(from);
          }
        }
      });

      function handleResize() {
        try {
          if (lastPos !== null) {
            updateButton(editorView, lastPos, true);
          }
        } catch (error) {
          console.error("Failed to handle resize:", error);
        }
      }

      window.addEventListener("resize", handleResize);

      return {
        destroy() {
          hoverDiv.remove();
          window.removeEventListener("resize", handleResize);
        },
      };
    },
  });
}

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

    // Update the image node with additional settings (this function needs to be defined)
    const updatedImageNodes = updateImageNode(customNodes, {
      ...imageSettings,
    });

    const mySchema = new Schema({
      nodes: updatedImageNodes,
      marks,
    });

    // Alternatively, define the imageSchema here if it should be separate
    // const imageSchema = new Schema({
    //   nodes: updateImageNode(customNodes, {
    //     ...imageSettings, // Ensure this matches your requirements
    //   }),
    //   marks,
    // });

    const view = new EditorView(editorRef.current, {
      state: EditorState.create({
        doc: DOMParser.fromSchema(mySchema).parse(
          document.createElement("div")
        ),
        plugins: exampleSetup({ schema: mySchema }).concat(

          inlinePlaceholderPlugin(),
          imagePlugin({
            ...imageSettings,
            resizeCallback: (el, updateCallback) => {
              const observer = new ResizeObserver(entries => {
                // ResizeObserver 콜백을 비동기적으로 실행
                requestAnimationFrame(() => {
                  // 여기서 updateCallback 또는 다른 DOM 조작 로직을 실행
                  updateCallback();
                });
              });
              observer.observe(el);
              return () => observer.unobserve(el);
            },
          }),
          hoverButtonPlugin(),
          checkBlockType()
        ),
      }),
    });

    editorRef.current.view = view;

    return () => {
      view.destroy();
    };
  }, []);

  return (
    <div
      ref={editorRef}
      id="editor"
      style={{ width: "80%", margin: "0 auto", paddingLeft: "15%" }} // Adjust width and add padding on the left
    />
  );
}
export default Page;
