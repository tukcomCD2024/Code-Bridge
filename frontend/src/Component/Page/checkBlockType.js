import { Plugin } from "prosemirror-state";
import toastr from "toastr";
import "toastr/build/toastr.css";

export function checkBlockType() {
  toastr.options.positionClass = "toast-bottom-full-width";

  return new Plugin({
    view(editorView) {
      const { dom } = editorView;

      const displayNodeInfo = (event) => {
        const coords = { left: event.clientX, top: event.clientY };
        const posAtCoords = editorView.posAtCoords(coords);
        if (!posAtCoords) return;

        const pos = posAtCoords.pos;
        const resolvedPos = editorView.state.doc.resolve(pos);
        // 현재 클릭된 노드의 정보를 가져옵니다.
        let node = resolvedPos.nodeAfter || resolvedPos.nodeBefore;
        let nodePos = resolvedPos.pos;
        if (resolvedPos.parent.type.name === "doc" && node) {
          // 'doc' 바로 아래 노드인 경우, 부모는 'doc'이고 자식 노드는 현재 노드입니다.
          toastr.info(
            `부모 노드: doc, 위치: 0, 자식 노드: ${node.type.name}, 위치: ${nodePos}`
          );
        } else if (node) {
          // 클릭된 노드가 이미지 노드인 경우, 그 부모 노드 정보를 표시합니다.
          const parentType = resolvedPos.parent.type.name;
          const parentPos = resolvedPos.start() - 1; // 부모 노드의 시작 위치
          toastr.info(
            `부모 노드: ${parentType}, 위치: ${parentPos}, 자식 노드: ${node.type.name}, 위치: ${nodePos}`
          );
        } else {
          // 클릭된 위치에 노드가 없는 경우
          toastr.info(
            `부모 노드: ${resolvedPos.parent.type.name}, 위치: ${resolvedPos.pos}, 자식 노드 없음`
          );
        }
      };

      // mousedown 이벤트 리스너를 추가합니다.
      dom.addEventListener("mousedown", displayNodeInfo);

      // drop 이벤트 리스너를 추가합니다.
      dom.addEventListener("drop", displayNodeInfo, true);

      return {
        destroy() {
          // 에디터가 파괴될 때 이벤트 리스너를 제거합니다.
          dom.removeEventListener("mousedown", displayNodeInfo);
          dom.removeEventListener("drop", displayNodeInfo, true);
        },
      };
    },
  });
}
