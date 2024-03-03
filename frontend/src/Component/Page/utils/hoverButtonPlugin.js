import { Plugin, Selection } from "prosemirror-state";
import down_arrow from "../../../image/down_arrow.svg";
import typing from "../../../image/typing.gif";

// 노트 페이지에서 블록(노드)마다 작은 메뉴창이 뜨게 한다.
export function hoverButtonPlugin() {
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
        increaseBrowserHeightForScroll();
        updateButton(editorView, newPos, true);
      });

      function increaseBrowserHeightForScroll() {
        const paragraphNodeHeight = 48;
        // 현재 문서(body)의 높이
        const currentBodyHeight = document.body.scrollHeight;

        document.body.style.height = `${
          currentBodyHeight + paragraphNodeHeight
        }px`;
      }

      function updateButton(view, pos, show) {
        try {
          const { doc } = view.state;
          const resolvedPos = doc.resolve(pos);
          if (
            (resolvedPos.depth === 0 &&
              resolvedPos.nodeBefore.type.name !== "paragraph") ||
            (resolvedPos.depth === 0 && !show)
          ) {
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
            increaseBrowserHeightForScroll();
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

      function handleClick(event) {
        const { pos } = editorView.posAtCoords({
          left: event.clientX,
          top: event.clientY,
        });
        if (pos !== null) {
          const resolvedPos = editorView.state.doc.resolve(pos);
          let lineNumber = 1;
          editorView.state.doc.nodesBetween(
            0,
            resolvedPos.pos,
            (node, start) => {
              if (node.isBlock) {
                // 클릭된 위치가 현재 노드의 범위 내에 있는지 확인합니다.
                if (
                  start < resolvedPos.pos &&
                  resolvedPos.pos <= start + node.nodeSize
                ) {
                  console.log(
                    `Clicked line number: ${lineNumber}, Node type: ${node.type.name}`
                  );
                  return false; // 순회 중단
                }
                lineNumber++;
              }
            }
          );
        }
      }

      // 클릭 이벤트 리스너 등록
      editorView.dom.addEventListener("click", handleClick);

      //여기까지

      window.addEventListener("resize", handleResize);

      return {
        destroy() {
          hoverDiv.remove();
          window.removeEventListener("resize", handleResize);
          editorView.dom.removeEventListener("click", handleClick);
        },
      };
    },
  });
}
