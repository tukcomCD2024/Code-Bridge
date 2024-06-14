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
      editorResizing();
    };

    function editorResizing() {
      const hoverDiv = document.querySelector(".hoverDiv");
      const editor = document.querySelector("#editor");
      const prosemirror = document.querySelector(".ProseMirror");
      const initialEditorPaddingLeft = "8%";
      const initialEditorPaddingRight = "5%";   
      const initialProsemirrorMarginLeft = "40px";

      if (getComputedStyle(hoverDiv).visibility !== "visible" && window.matchMedia("(max-width: 768px)").matches) {
        editor.style.paddingLeft = "0%";
        editor.style.paddingRight = "0%";
        prosemirror.style.marginLeft = "0px";
      } else {
        editor.style.paddingLeft = initialEditorPaddingLeft;
        editor.style.paddingRight = initialEditorPaddingRight;
        prosemirror.style.marginLeft = initialProsemirrorMarginLeft;
      }
    }

  const transactionImageAtLine = (imageUrl) => tr => {
    // 이미지 노드 생성
    const imageNode = editorRef.current.view.state.schema.nodes.image.create({ src: imageUrl });

    // 사용자가 클릭한 위치 가져오기
    const selection = editorRef.current.view.state.selection;
    const pos = selection.from;

    // 이미지 노드 삽입
    const insertTr = tr.insert(pos, imageNode);

    // 이미지 삽입 후 커서 위치 설정
    const resolvedPos = insertTr.doc.resolve(pos + imageNode.nodeSize);
    const newSelection = selection.constructor.near(resolvedPos);

    return insertTr.setSelection(newSelection);
  };
  return null;
});

 export default ImageToEditor;
