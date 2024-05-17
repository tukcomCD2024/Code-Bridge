import React from 'react';

const ImageToEditor = React.forwardRef((props, ref) => {
  const editorRef = ref;

  window.uploadImageToEditor = (imageUrl) => {
      const hoverDiv = document.querySelector(".hoverDiv");

      // ProseMirror Transaction 생성
      const transaction = editorRef.current.view.state.tr;
      const transactionWithImage = transactionImageAtLine(imageUrl)(transaction);

      // Transaction 적용하여 에디터에 이미지 삽입
      editorRef.current.view.dispatch(transactionWithImage);
      hoverDiv.style.visibility = "hidden";
    };

  const transactionImageAtLine = (imageUrl) => tr => {
    // 이미지 노드 생성
    const imageNode = editorRef.current.view.state.schema.nodes.image.create({ src: imageUrl });

    // 특정 줄의 시작 노드 위치 찾기
    let pos = 0;
    editorRef.current.view.state.doc.nodesBetween(0, editorRef.current.view.state.doc.content.size, (node, nodePos) => {
        if (node.isBlock && nodePos > pos) {
            pos = nodePos;
        }
        pos = pos === 0 ? 1 : pos; 
    });

    // 이미지 노드 삽입
    const insertTr = tr.insert(pos, imageNode);

    // 이미지 삽입 후 커서 위치 설정
    const resolvedPos = insertTr.doc.resolve(pos + imageNode.nodeSize);
    const selection = editorRef.current.view.state.selection.constructor.near(resolvedPos);

    return insertTr.setSelection(selection);
  };
  return null;
});

 export default ImageToEditor;
