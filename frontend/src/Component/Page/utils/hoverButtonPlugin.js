import { Plugin, Selection, NodeSelection } from "prosemirror-state";
import down_arrow from "../../../image/down_arrow.svg";
import lock from "../../../image/lock2.gif";
import { library, icon } from '@fortawesome/fontawesome-svg-core';
import { faHeart } from '@fortawesome/free-solid-svg-icons';

// 문서 내 블록(노드)의 총 수를 계산하는 함수
function countDocBlocks(doc) {
  let count = 0;
  doc.descendants(node => {
    if (node.type.name === "paragraph" || node.isBlock) {
      count++;
    }
  });
  return count;
}

// 노트 페이지에서 블록(노드)마다 작은 메뉴창이 뜨게 한다.
export function hoverButtonPlugin() {
  const userId = localStorage.getItem("userId");
  const hoverDiv = document.createElement("div");

  return new Plugin({
    view(editorView) {
      hoverDiv.classList.add("hoverDiv"); // CSS 클래스 적용
      document.body.appendChild(hoverDiv); // 바디에 직접 추가

      let lastPos = null;

      // hoverButton 생성(블록 추가)
      const hoverButton_plus = document.createElement("img");
      hoverButton_plus.src = down_arrow;
      hoverButton_plus.title = "새 블록 추가";
      hoverButton_plus.classList.add("hoverButton_plus"); // CSS 클래스 적용
      hoverDiv.appendChild(hoverButton_plus);

      // hoverButton 생성(노드 잠금)
      const hoverButton_lock = document.createElement("img");
      hoverButton_lock.src = lock;
      hoverButton_lock.title = "노드 편집 잠금";
      hoverButton_lock.classList.add("hoverButton_lock"); // CSS 클래스 적용
      hoverDiv.appendChild(hoverButton_lock);
      
      // hoverButton 생성(좋아요)
      const hoverButton_like = document.createElement("div");
      library.add(faHeart);
      const heartIcon = icon(faHeart).node[0];
      hoverButton_like.appendChild(heartIcon);
      hoverButton_like.classList.add("hoverButton_like");
      hoverButton_like.title = "좋아요";
      hoverDiv.appendChild(hoverButton_like);

      // hoverButton_like 요소에 클릭 이벤트 리스너 추가
      hoverButton_like.addEventListener("click", function() {
        this.classList.toggle("hoverButton_like");
        this.classList.toggle("hoverButton_like_fullRedHeart");
      });

      hoverButton_lock.addEventListener("click", (event) => {
        event.stopPropagation(); // 이벤트 버블링 방지
      
        if (lastPos !== null) {
         const resolvedPos = editorView.state.doc.resolve(lastPos);
          const node = resolvedPos.node();
      
          // 노드가 uuid를 가지고 있는지 확인
          if (node && node.attrs.guid) {
            const nickname = localStorage.getItem("nickname");
            const guid = node.attrs.guid
            window.toggleLineLock(guid, nickname);
            } else {
            console.log('No UUID found for this node.');
          }
        } else {
          console.error('No last position recorded.');
        }
      });

      hoverButton_plus.addEventListener("click", (event) => {
        const { state, dispatch } = editorView;
        const { selection } = state;
        let tr = state.tr; // 현재 문서의 트랜잭션
        let insertPos;
        let $clickPos = state.doc.resolve(lastPos);
        const isImageNode = selection instanceof NodeSelection && selection.node.type.name === "image";

        if ($clickPos.nodeBefore == null && isImageNode) {
          // 문서 시작 부분에 이미지가 있는 경우
          insertPos = 1;
        } else if (isImageNode) {
          // 문서 중간 부분에 위치한 이미지 노드 바로 직후를 삽입 위치로 설정
          $clickPos = selection.$anchor;
          insertPos = $clickPos.pos + 1;
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
        increaseEditorHeightForScroll();
        updateButton(editorView, newPos, true);
      });
      
      function updateButton(view, pos, show) {
        try {
          const { doc, selection } = view.state;
          let resolvedPos = doc.resolve(pos);
          const isImageNode = selection instanceof NodeSelection && selection.node.type.name === "image";

          // 버튼을 숨기는 경우는 바로 이전 노드가 없거나 작성 불가능한 노드(doc)를 클릭할 때
          if ((resolvedPos.depth === 0 && !resolvedPos.nodeBefore && !isImageNode) || (resolvedPos.depth === 0 && !show)) {
            hoverDiv.style.visibility = "hidden";
            return;
          }

          // 마지막 위치 업데이트
          lastPos = pos;
      
          let coords;

          // 이미지 노드가 문서의 시작에 있을 때
          if (pos === 0 && isImageNode) {
            coords = view.coordsAtPos(0);
            hoverButton_lock.style.display = "none";
          } else if (isImageNode) {
            // 이미지 노드가 문서의 중간에 있을 때
            resolvedPos = selection.$anchor;
            coords = view.coordsAtPos(resolvedPos.pos);
            hoverButton_lock.style.display = "none";
          } else {
            // 선택된 위치에서 가장 가까운 블록 노드의 경계를 찾습니다.
            let depth = resolvedPos.depth;
            while (depth > 0 && !resolvedPos.node(depth).isBlock) {
              depth--;
            }
            const startPos = resolvedPos.start(depth);
            // 시작 위치에 대한 좌표를 계산합니다.
            coords = view.coordsAtPos(startPos);
            hoverButton_lock.style.display = "block";
          }
          
          // 스크롤 오프셋을 고려하여 좌표 조정
          const topWithScroll = coords.top + window.scrollY;
      
          const editorRect = view.dom.getBoundingClientRect();
          hoverDiv.style.left = `${editorRect.left - hoverDiv.offsetWidth - 5}px`;
          hoverDiv.style.top = `${topWithScroll-4}px`;
          hoverDiv.style.visibility = "visible";
      
        } catch (error) {
          console.error("Failed to update button position:", error);
        }
      }

      function increaseEditorHeightForScroll() {
        const paragraphNodeHeight = 48; // 추가할 높이 값
        const editorElement = document.querySelector('.ProseMirror');
      
        if (editorElement) {
          // 에디터 내부의 현재 높이를 계산합니다.
          const currentEditorHeight = editorElement.scrollHeight;
          // 에디터의 높이를 조정합니다.
          editorElement.style.height = `${currentEditorHeight + paragraphNodeHeight}px`;
        }
      }
      
      function handleInteraction(event) {
        const { pos } = editorView.posAtCoords({
          left: event.clientX,
          top: event.clientY,
        });
        if (pos === null || pos === undefined) return;

        const isImageNode = editorView.state.selection.node;

        // 이미지 노드가 아닌 경우(텍스트 노드 또는 doc 일 때)
        if (!isImageNode) {
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
      editorView.dom.addEventListener("click", handleInteraction);
      editorView.dom.addEventListener("keyup", (event) => {
        const { from } = editorView.state.selection;
        if (from !== null) {
          if (event.key === "ArrowUp" || event.key === "ArrowDown") {
            handleInteractionFromCursor(from);
          }
      }
      });
      
      editorView.dom.addEventListener("keydown", (event) => {
        if (event.keyCode === 13 || event.key === "ArrowUp" || event.key === "ArrowDown") {
          const { from } = editorView.state.selection;
          if (from !== null) {
            hoverDiv.style.visibility = "hidden";
          }
        }
      });
      editorView.dom.addEventListener("keyup", (event) => {
        if (event.keyCode === 13) {
          const { from } = editorView.state.selection;
          if (from !== null) {
            handleInteractionFromCursor(from);
            increaseEditorHeightForScroll();
          }
        }
      });

      return {
        destroy() {
          hoverDiv.remove();
          window.removeEventListener("resize", handleResize);
        },
      };
    },
    
    appendTransaction(transactions, oldState, newState) {
      // 변화 전후의 블록(노드) 수를 계산
      const oldDocBlocks = countDocBlocks(oldState.doc);
      const newDocBlocks = countDocBlocks(newState.doc);
    
      if (newDocBlocks < oldDocBlocks) {
        hoverDiv.style.visibility = "hidden";
      }
    
      return null; // 추가적인 트랜잭션을 반환하지 않음
    },
  });
}
