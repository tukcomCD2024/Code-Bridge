import { Plugin } from "prosemirror-state";
import { Decoration, DecorationSet } from "prosemirror-view";

// 노트 페이지에서 블록(노드)가 비어있으면 "내용을 입력하세요..." placeholder를 표시한다.
export function inlinePlaceholderPlugin() {
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
          } else if (node.type.name !== "paragraph") {
            decorations.push(
              Decoration.node(pos, pos + node.nodeSize, {
                class: "placeholder",
                style: "",
                // style: `--placeholder-text: "${node.type.name}_작성자 이름";`,
              })
            );
          }
          return false;
        });

        return DecorationSet.create(doc, decorations);
      },
    },
  });
}
