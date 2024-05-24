import { Plugin } from 'prosemirror-state';
import { Decoration, DecorationSet } from 'prosemirror-view';

// 노트 페이지에서 블록(노드)가 비어있으면 "내용을 입력하세요..." placeholder를 표시한다.
export function inlinePlaceholderPlugin() {
  return new Plugin({
    state: {
      init: (_, { doc }) => {
        return DecorationSet.create(doc, []);
      },
      apply: (tr, old) => {
        const decorations = findPlaceholders(tr.doc, tr.selection);
        return DecorationSet.create(tr.doc, decorations);
      }
    },
    props: {
      decorations(state) {
        return this.getState(state);
      }
    }
  });
}

function findPlaceholders(doc, selection) {
  const decorations = [];
  const { from } = selection;

  doc.descendants((node, pos) => {
    if (!node.isBlock || node.content.size > 0) return false;

    const isCursorInsideNode = selection.empty && (from >= pos && from <= pos + node.nodeSize);
    
    // Only add placeholder if the node is empty and the cursor is inside the node
    if (node.type.name === "paragraph" && isCursorInsideNode) {
      decorations.push(
        Decoration.node(pos, pos + node.nodeSize, {
          class: "placeholder",
          'data-placeholder': '내용을 입력하세요...'
        })
      );
    }
    
    return false;
  });

  return decorations;
}
