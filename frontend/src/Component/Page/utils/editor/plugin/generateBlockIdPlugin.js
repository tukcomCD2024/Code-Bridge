import { Plugin } from "prosemirror-state";
import { v4 as uuidv4 } from "uuid";

export const generateBlockIdPlugin = ({ yDocInitialized, guidGenerator = uuidv4 }) => {
  return new Plugin({
      appendTransaction: (transactions, prevState, nextState) => {
        // Yjs 문서가 초기화되지 않은 경우 동작하지 않도록 함
        if (!yDocInitialized.current) {
          return null;        
        }

        const tr = nextState.tr;
        let modified = false;
        const generatedIds = new Set();
        const userId = localStorage.getItem('userId');

        if (transactions.some(transaction => transaction.docChanged)) {
          const { paragraph, image } = nextState.schema.nodes;
          let prevNode = null; // 이전 노드를 추적하기 위한 변수
          let prevPos = null; // 이전 노드의 위치를 저장
      
          nextState.doc.descendants((node, pos) => {
            if (node.type === image) {
              let currentGuid = node.attrs['data-guid'];
              if (!currentGuid || generatedIds.has(currentGuid)) {
                let newGuid;
                do {
                  newGuid = guidGenerator();
                } while (generatedIds.has(newGuid));
                generatedIds.add(newGuid);
                tr.setNodeMarkup(pos, undefined, {...node.attrs, 'data-guid': newGuid, 'data-writer': userId});
                modified = true;
              } else {
                generatedIds.add(currentGuid);
              }
            } else if (node.type === paragraph) {
              const nodeTextContent = node.textContent;
              const selection = nextState.selection;
              const cursorPosition = selection.head || selection.from;
      
              if (cursorPosition >= pos && cursorPosition <= pos + node.nodeSize) {
                const cursorPositionInNode = cursorPosition - pos;
                if (cursorPositionInNode === 1 && nodeTextContent !== "") {
                  if (prevNode && !generatedIds.has(prevNode.attrs.guid) && node?.attrs.guid === prevNode?.attrs.guid) {
                    let newGuid;
                    do {
                      newGuid = guidGenerator();
                    } while (generatedIds.has(newGuid));
                    generatedIds.add(newGuid);
                    tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid, writer: userId});
                    modified = true;
                  }
                } else {
                  // 일반적인 guid 할당 로직
                  let currentGuid = node.attrs.guid;
                  if (!currentGuid || generatedIds.has(currentGuid)) {
                    let newGuid;
                    do {
                      newGuid = guidGenerator();
                    } while (generatedIds.has(newGuid));
                    generatedIds.add(newGuid);
                    tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid, writer: userId});
                    modified = true;
                  } else {
                    generatedIds.add(currentGuid);
                  }
                }
              }
              // 현재 노드와 위치를 이전 노드로 업데이트
              prevNode = node;
              prevPos = pos;
            } 
          });
        }
        return modified ? tr : null;
      },
    });
  };
