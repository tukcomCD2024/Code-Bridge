import { Plugin } from "prosemirror-state";
import toastr from "toastr";
import "toastr/build/toastr.css";

export function checkBlockType() {
  toastr.options.positionClass = "toast-bottom-full-width";

  return new Plugin({
    view(editorView) {
      const { dom } = editorView;

      function handleClick(event) {
        const coords = { left: event.clientX, top: event.clientY };
        const posAtCoords = editorView.posAtCoords(coords);
        if (!posAtCoords) return;

        toastr.remove(); // 이전 toastr 메시지를 제거

        const pos = posAtCoords.pos;
        const resolvedPos = editorView.state.doc.resolve(pos);
        // 현재 클릭된 노드의 정보와 부모 노드의 정보를 가져옵니다.
        let node = resolvedPos.nodeAfter || resolvedPos.nodeBefore;
        let parentType = resolvedPos.parent.type.name;
        let parentPos = resolvedPos.start() - 1; // 부모 노드의 시작 위치
        let lineNumber = 1;

        // 라인 번호 계산
        editorView.state.doc.nodesBetween(0, pos, (node, start) => {
          if (node.isBlock && start < pos) {
            lineNumber++;
          }
        });

        if (node) {
          // 노드와 부모 노드 정보를 표시
          toastr.info(
            `블록 번호: ${lineNumber} || 부모 노드: ${parentType}, 자식 노드: ${node.type.name} || 부모 시작 위치: ${parentPos} `
          );
        } else {
          // 클릭된 위치에 노드가 없는 경우
          toastr.info(
            `블록 번호: ${lineNumber} || 부모 노드: ${parentType}, 자식 없음 || 부모 시작 위치: ${parentPos}`
          );
        }
      }

      // mousedown 이벤트 리스너를 추가합니다.
      dom.addEventListener("mousedown", handleClick);

      // drop 이벤트 리스너를 추가합니다.
      dom.addEventListener("drop", handleClick, true);

      return {
        destroy() {
          // 에디터가 파괴될 때 이벤트 리스너를 제거합니다.
          dom.removeEventListener("mousedown", handleClick);
          dom.removeEventListener("drop", handleClick, true);
        },
      };
    },
  });
}
