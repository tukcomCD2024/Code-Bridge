import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from 'react-router-dom';
import styled from "styled-components";

// prosemirror 라이브러리(리치 텍스트 에디터)
import { Schema, DOMParser } from "prosemirror-model";
import { EditorState, Selection, Plugin } from "prosemirror-state";
import { EditorView } from "prosemirror-view";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { exampleSetup } from "prosemirror-example-setup";
import { keymap } from "prosemirror-keymap";

// yjs 라이브러리(동시편집)
import { WebsocketProvider } from "y-websocket";
import { getYDocInstance } from "./utils/YjsInstance";
import { ySyncPlugin, yCursorPlugin, yUndoPlugin, undo, redo  } from "y-prosemirror";

import { updateImageNode, imagePlugin } from "prosemirror-image-plugin";
import "./ProseMirror_css/prosemirror_image_plugin/common.css";
import "./ProseMirror_css/prosemirror_image_plugin/withResize.css";
import "./ProseMirror_css/prosemirror_image_plugin/sideResize.css";
import "./ProseMirror_css/prosemirror_image_plugin/withoutResize.css";
import "./ProseMirror_css/ProseMirror.css";

import ModalImageComponent from "./utils/ModalImageComponent";
import { imageSettings, imageNodeSpec } from "./utils/pageSettings";
import { inlinePlaceholderPlugin } from "./utils/inlinePlaceholderPlugin";
import { hoverButtonPlugin } from "./utils/hoverButtonPlugin";
import { cursorColors } from "../Utils/cursorColor"
import NoteSettingModal from "./utils/noteSettingModal";
import loadingImage from "../../image/loading.gif";

import toastr from 'toastr';
import 'toastr/build/toastr.css';

import { v4 as uuidv4 } from "uuid";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLeftLong, faRightLong, faSquarePlus, faTrashCan, faList, faGear } from "@fortawesome/free-solid-svg-icons";

function Page() {
  const editorRef = useRef(null);
  let nickname = localStorage.getItem('nickname');
  let userId = localStorage.getItem('userId');

  const location = useLocation();
  const navigate = useNavigate();

  const pathSegments = location.pathname.split('/').filter(Boolean); 
  const organizationId = pathSegments[1];
  const noteId = pathSegments[2];
  const pageId = pathSegments[3];
  
  const [reconnect, setReconnect] = useState(false);
  const [noteinfo, setNoteInfo] = useState(null);
  const [isloaded, setisloaded] = useState(false); // 로딩 상태 관리
  const [pages, setPages] = useState([]); // 페이지 상태 관리
  const [pageIndex, setPageIndex] = useState(0);
  const [pageInputValue, setPageInputValue] = useState(pageIndex !== 0 ? pageIndex + 1 : 1);
  const [isPageHandleButtonDisabled, setIsPageHandleButtonDisabled] = useState(false);
  const [usersAndColors, setUsersAndColors] = useState([]); // 연결된 사용자와 색상 상태
  const [noteSettingModalOpen, setNoteSettingModalOpen] = useState(false);
  const [myimage, setMyImage] = useState(null);
  const [showModalImage, setShowModalImage] = useState(false);
  const [clickedImageSrc, setClickedImageSrc] = useState('');

  const uploadImage = (e) => {
    const selectedFile = e.target.files[0];

    // 파일이 선택되었고, 이미지 파일인 경우에만 처리
    if (selectedFile && isImageFile(selectedFile)) {
      setMyImage(URL.createObjectURL(selectedFile));
    } else {
      // 이미지 파일이 아닌 경우에 대한 처리 (예: 경고 메시지 등)
      alert("올바른 이미지 파일을 선택해주세요.");
    }
  };

  // 이미지 파일 여부를 확인하는 함수
  const isImageFile = (file) => {
    const allowedExtensions = ["jpg", "jpeg", "png", "gif"]; // 허용된 확장자들

    // 파일 이름에서 확장자 추출
    const fileName = file.name;
    const fileExtension = fileName.split(".").pop().toLowerCase();

    // 허용된 확장자들 중에 포함되어 있는지 확인
    return allowedExtensions.includes(fileExtension);
  };

  const handleOpenNoteSettingModal = () => {
    setNoteSettingModalOpen(true);
  };

  const handleCloseNoteSettingModal = () => {
    localStorage.setItem("recentImageUrl", '');
    setMyImage(null);
    setNoteSettingModalOpen(false);
  };

    const handleOpenImageZoomModal = () => {
    setShowModalImage(true);
  };

  const handleCloseImageZoomModal = () => {
    setShowModalImage(false);
  };

  // 네비게이션바에 페이지 이동 함수
  const navigateToPage = (pageID) => {
    navigate(`/organization/${organizationId}/${noteId}/${pageID}`);
  };

  // 이전 페이지
  const prevPage = () => {
    if (pageIndex === 0) {
      return;
    }
    const prevPageID = pages[pageIndex - 1]?.id;
    navigateToPage(prevPageID);
  };

  // 다음 페이지
  const nextPage = () => {
    const nextPageID = pages[pageIndex + 1]?.id;
    if (!nextPageID) {
      return;
    }
    navigateToPage(nextPageID);
  };

  // 특정 페이지
  const pageTarget = () => {
    if (pageIndex + 1 === pageInputValue) {
      return;
    }
    const pageTargetID = pages[pageInputValue - 1]?.id;
    navigateToPage(pageTargetID);
  };

  const handleCreate = async (e) => {
    const createUserId = userId;
    const createPage = (pageId) => {
      const newPage = {
        id: pageId,
      };
      const updatedPages = [...pages, newPage];
      setPages(updatedPages);
    };
    
    try {
      setIsPageHandleButtonDisabled(true);
      const response = await fetch("/api/page", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ organizationId, noteId, createUserId }),
      });
      if (response.ok) {
        const responseData = await response.json();
        const pageId = responseData.pageId;
        navigate(`/organization/${organizationId}/${noteId}/${pageId}`);
        createPage(pageId);
      } else {
        const errorData = await response.json();
        alert(`생성 실패: ${errorData.message}`);
      }
    }
    catch (error) {
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    } finally {
    setIsPageHandleButtonDisabled(false);
    }
  };

  const handleRemove = async (e) => {
    if(pageIndex === 0){
      alert("메인 페이지는 삭제하실 수 없습니다.");
      return;
    }
    const isConfirmed = window.confirm(`현재 위치한 [${pageIndex + 1}] 페이지를 삭제합니다.`);
    if(isConfirmed){
      try {
        setIsPageHandleButtonDisabled(true);
        const response = await fetch("/api/page", {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ organizationId, noteId, pageId }),
        });
        if (response.ok) {
          const prevPageID = pages[pageIndex - 1]?.id;
          navigate(`/organization/${organizationId}/${noteId}/${prevPageID}`);
        }
      } catch (error) {
        console.error("Error: ", error);
        alert("처리 중 오류가 발생했습니다.");
      } finally {
        setIsPageHandleButtonDisabled(false);
      }
    }
  };
  
  const handlePageInputChange  = (event) => {
    const newValue = parseInt(event.target.value, 10);
    if(isNaN(newValue)){
      setPageInputValue(0);
      return;
    }

    if (!isNaN(newValue) && newValue >= 1 && newValue <= pages.length) {
      setPageInputValue(newValue);
    } else {
      setPageInputValue(pages.length);
    }
  };

  useEffect(() => {
    setPageInputValue(pageIndex !== 0 ? pageIndex + 1 : 1);
  }, [pageIndex]);

  useEffect(() => {
    let isCancelled = false;
  
    const fetchPageInfo = async () => {
      try {
        const createUserId = userId;
        const response = await fetch(`/api/page/search`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ organizationId, noteId, createUserId }),
        });
        if (response.ok && !isCancelled) {
          const data = await response.json();
          const fetchedPageData = data.map(page => ({
            id: page.pageId
          }));
          setPages(fetchedPageData);
          const index = fetchedPageData.findIndex(page => page.id === pageId);
          setPageIndex(index);
        } else {
          console.error(`Failed to fetch: HTTP status ${response.status}`);
        }
      } catch (error) {
        if (!isCancelled) {
          console.error('Error fetching', error);
        }
      }
    };
  
    fetchPageInfo();
  
    return () => {
      isCancelled = true;
    };
  }, [location, pageId]);
  
  
  useEffect(() => {
    let isCancelled = false;

    const fetchNoteInfo = async () => {
      try {
        const response = await fetch(`/api/user/note/${organizationId}`);
        if (response.ok && !isCancelled) {
          const data = await response.json();
          const noteData = data.find(note => note.id === noteId);
          if (noteData && !isCancelled) {
            setNoteInfo({
              id: noteData.id,
              name: noteData.title,
              image: noteData.noteImageUrl,
            });
          }
        } else {
          console.error(`Failed to fetch: HTTP status ${response.status}`);
        }
      } catch (error) {
        if (!isCancelled) {
          console.error('Error fetching', error);
        }
      }
    };

    fetchNoteInfo();

    return () => {
      isCancelled = true;
    };
  }, [location, organizationId, noteId]);
  // 네비게이션바에 페이지 컨트롤 코드_마지막

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
      guid: { default: "" }, // Ensure guid attribute is included
      writer: { default: userId },
    },
    parseDOM: [
      {
        tag: "p",
        getAttrs: (dom) => ({guid: dom.getAttribute("data-guid"), writer: dom.getAttribute("data-writer"),}),
      },
    ],
    toDOM(node) {
      return ["p", { class: node.attrs.class, "data-guid": node.attrs.guid, "data-writer": node.attrs.writer}, 0];
    },
  };

  const newParagraphNode = extendedNodes.update(
    "paragraph",
    customParagraphNode
  );

  const defaultNodes = updateImageNode(newParagraphNode, {
    ...imageSettings,
  });

  const mySchema = new Schema({
    nodes: defaultNodes,
    marks: basicSchema.spec.marks,
  });

  useEffect(() => {
    if (!editorRef.current) return;

    const roomId = pageId;
    const ydoc = getYDocInstance(roomId);
    const provider = new WebsocketProvider(
      // "wss://demos.yjs.dev/ws", // 웹소켓 서버 주소(데모용)
      // "ws://localhost:4000", //배포용
      // "ws://nodejs:4000", 
      "wss://sharenote.shop/ws",
      roomId, // 방 이름
      ydoc
    );

    const yXmlFragment = ydoc.getXmlFragment("prosemirror");
    const yConnectedUserList = ydoc.getMap('connectedUsers');
    const yLineLocks = ydoc.getMap('nodeInfo');
    const yUserLocks = ydoc.getMap('yUserLocks');
    const yLikeList = ydoc.getMap(`yLikeList_${userId}`);

    function isWeb() {
      const userAgent = navigator.userAgent.toLowerCase();
      const isAndroidWebView = (userAgent.indexOf('android') > -1 && userAgent.indexOf('mobile') > -1) || userAgent.indexOf('app') > -1;
      return isAndroidWebView
    }
    function checkLocalStorage() {
      return new Promise((resolve, reject) => {
        function getDataFromStorage() {
          return {
            nickname: localStorage.getItem('nickname'),
            userId: localStorage.getItem('userId')
          };
        }
    
        let { nickname, userId } = getDataFromStorage();
    
        if (nickname && userId) {
          resolve();  
        } else {
          setTimeout(() => {
            let { nickname, userId } = getDataFromStorage();
            if (nickname && userId) {
              resolve();
            } else {
              reject(new Error("계정 정보가 로컬 스토리지에 없습니다."));
            }
          }, 3000);
        }
      });
    }
    function handleUserConnection() {
      const nicknameWithSuffix = `${nickname}_다중 접속`;
      const isSingleConnected = yConnectedUserList.has(nickname);
      const isMultiConnected = yConnectedUserList.has(nicknameWithSuffix);
  
      if (isSingleConnected && isMultiConnected) {
        const isConfirmed = window.confirm("동시 접속 가능한 횟수를 초과하셨습니다.\n기존 접속을 종료하고 새로 접속하시겠습니까?");
        if (isConfirmed) {
          yConnectedUserList.set(nicknameWithSuffix, 'kicked');
        } else {
          navigate(`/organization/${pathSegments[1]}`);
          return;
        }
      }
  
      let userColor = yConnectedUserList.get(nickname) || yConnectedUserList.get(nicknameWithSuffix) || getRandomColor();
      
      if (!isSingleConnected) {
        yConnectedUserList.set(nickname, userColor);
        provider.awareness.setLocalStateField('user', { name: nickname, color: userColor });
      } else {
        yConnectedUserList.set(nicknameWithSuffix, userColor);
        provider.awareness.setLocalStateField('user', { name: nicknameWithSuffix, color: userColor });
      }
      updateUsersAndColors(); // UI 업데이트
    }

    provider.on("sync", (isSynced) => {
      if (isWeb()) {
        if (isSynced) {
          handleUserConnection();
        }
        checkLocalStorage().then(() => {
        }).catch(error => {
          toastr.error("계정 정보 확인 불가");
          console.error(error);
        });
      } else {
          if (isSynced) {
            handleUserConnection();   
        }
      }
      // setisloaded(true); // 딜레이 없음
      setTimeout(() => {
        setisloaded(true);
      }, 300); // 딜레이 있음
    });

    provider.on('status', event => {
      if (event.status === 'disconnected') {
        const userState = provider.awareness.getLocalState();
        if (userState && userState.user) {
          yConnectedUserList.delete(userState.user.name);
          setReconnect(true);
        }
      }
      if (event.status === 'connected') {
        const nicknameWithSuffix = `${nickname}_다중 접속`;
        if (reconnect && (!yConnectedUserList.get(nickname) || !yConnectedUserList.get(nicknameWithSuffix))) {
          setReconnect(false);
          handleUserConnection();
        }
      }
    });
    
    function onlineUpdate() {
      const userState = provider.awareness.getLocalState();
    
      if (userState && userState.user && userState.user.name) {
        const nickname = userState.user.name;
    
        if (!editorRef) {
          yConnectedUserList.delete(nickname);
        }
    
        if (yConnectedUserList.get(nickname) === 'kicked') {
          toastr.warning("연결 정보가 없습니다!");
          navigate(`/organization/${pathSegments[1]}`);
          return;
        }
      }
      updateUsersAndColors();
    }
   yConnectedUserList.observe(onlineUpdate);

    window.yjsDisconnect = function() {
      if(!editorRef) {
        return;
      }

      const keysToDelete = [];

      yLineLocks.forEach((value, key) => {
        if (value === nickname) {
          keysToDelete.push(key);
        }
      });
      keysToDelete.forEach(key => yLineLocks.delete(key));
      yUserLocks.delete(nickname);
    
      // Yjs 연결 해제 및 리소스 정리
      const userState = provider.awareness.getLocalState();
      if (userState && userState.user) {
        yConnectedUserList.delete(userState.user.name);
      }
    
      // 연결 해제 및 리소스 정리
      view.destroy();
      provider.destroy();
      provider.disconnect();
    }  

    const generateBlockIdPlugin = (guidGenerator = uuidv4) => {
      return new Plugin({
        appendTransaction: (transactions, prevState, nextState) => {
          const tr = nextState.tr;
          let modified = false;
          const generatedIds = new Set();
        
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
                  tr.setNodeMarkup(pos, undefined, {...node.attrs, 'data-guid': newGuid});
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
                    if (prevNode && !generatedIds.has(prevNode.attrs.guid)) {
                      let newGuid;
                      do {
                        newGuid = guidGenerator();
                      } while (generatedIds.has(newGuid));
                      generatedIds.add(newGuid);
                      tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid});
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
                      tr.setNodeMarkup(pos, undefined, {...node.attrs, guid: newGuid});
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

    // 줄 잠금/해제 함수
    window.toggleLineLock = function(guid, nickname) {
      if(!nickname || !userId) {
        toastr.info(`로그인 정보가 없습니다.`);
        navigate("/login");
        return;
      }
    const currentLock = yLineLocks.get(guid.toString());
    
    // 현재 사용자가 이미 다른 노드를 잠근 경우, 알림창 표시
    const currentLockedNodeByUser = yUserLocks.get(nickname);
    if (!currentLock && currentLockedNodeByUser && currentLockedNodeByUser !== guid.toString()) {
      // 사용자에게 확인을 요청하는 대화 상자 표시
      const isConfirmed = window.confirm("최대 1개까지 잠금이 가능합니다.\n이전에 설정한 잠금을 해제하시겠습니까?");
      if (isConfirmed) {
        yLineLocks.delete(currentLockedNodeByUser);
        yUserLocks.delete(nickname); 
      } else {
        return;
      }
    }
    
      if (currentLock) {
        // 해당 줄이 이미 잠겨 있고, 현재 사용자가 잠근 경우 잠금 해제
        if (currentLock === nickname) {
            yLineLocks.delete(guid.toString());
            yUserLocks.delete(nickname);
            toastr.info(`편집 잠금이 해제되었습니다.`);
        } else {
          toastr.error(`[오류] ${currentLock} 에 의해 잠금 불가합니다.`);
        }
    } else {
      yLineLocks.set(guid.toString(), nickname);
      yUserLocks.set(nickname, guid.toString());
      toastr.success(`블록 편집 잠금이 설정되었습니다.`);
    }
  };

    // 더블클릭 감지를 위한 클릭 시간.
    let lastClickTime = 0;

    // 더블클릭 이벤트를 처리하는 함수
    const handleDoubleClick = (event) => {
      const currentTime = new Date().getTime();
      if (currentTime - lastClickTime < 300) {
          const { target } = event;
          if (target.tagName.toLowerCase() === "img") {
              const imageUrl = target.getAttribute("src");
              setClickedImageSrc(imageUrl)
              setShowModalImage(true);
          }
      }
      lastClickTime = currentTime;
  }

    const handleNodeClick = (nickname, event) => {
      handleDoubleClick(event);

      const { target, clientX, clientY } = event;
      const coords = { left: clientX, top: clientY };
      const posAtCoords = view.posAtCoords(coords);
      if (!posAtCoords) return;

      if (target.tagName === 'P' && target.hasAttribute('data-guid')) {
        const guid = target.getAttribute('data-guid');
        const currentLock = yLineLocks.get(guid.toString());
        if (currentLock) {
          if (currentLock !== nickname) {
            toastr.warning(`[알림] ${currentLock} 에 의해 편집 불가합니다.`);
          }
        }
      }
    };

    const handleEditAttempt = (nickname, event) => {
      // keydown 이벤트의 경우, 커서 위치의 노드를 찾아야 합니다.
      let targetNode;
      if (event.type === 'keydown') {
        const selection = document.getSelection();
        if (selection.rangeCount > 0) {
          const range = selection.getRangeAt(0);
          targetNode = range.startContainer.parentNode; // 커서 위치의 상위 노드를 타겟으로 설정
        }
      } else {
        // mousedown 이벤트의 경우, 이벤트 타겟을 직접 사용
        targetNode = event.target;
      }

      if (!targetNode) return; // 타겟 노드가 없으면 함수 종료

      // 타겟 노드가 'P' 태그이고 'data-guid' 속성을 가지고 있는지 확인
      if (targetNode.tagName === 'P' && targetNode.hasAttribute('data-guid')) {
        const guid = targetNode.getAttribute('data-guid');
        const currentLock = yLineLocks.get(guid);
        if (currentLock && currentLock !== nickname) {
          event.preventDefault(); // 편집 방지
          if (document.activeElement) { // 포커스(커서) 해제
            document.activeElement.blur();
          }
        }
      }
    };

    // 블록 좋아요     
    window.toggleLike = function(blockId, lover, heartReceiver) {
      if (lover !== heartReceiver) {
        const currentLikeState = yLikeList.get(blockId);
        const newLikeState = !currentLikeState;
        yLikeList.set(blockId, newLikeState);
      }

      const handleLike = async () => {
        try {
            const response = await fetch("/api/user/note/block/likes", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ organizationId, noteId, lover, blockId, heartReceiver}),
            });
            if (response.ok) {
                const responseData = await response.text();
                toastr.remove();
                if (responseData.includes("좋아요 성공!")) { 
                  toastr.success(responseData);
                } else {
                  toastr.info(responseData);
                }
            } else {
                const errorData = await response.text();
                toastr.remove();
                toastr.error(errorData);
            }
        } catch (error) {
            console.error("Error: ", error);
            alert("처리 중 오류가 발생했습니다.");
        }
      };

      return handleLike();
    };

    window.getLikeList = function(guid) {
      const isLiked = yLikeList.get(guid.toString());
      return !!isLiked;
    };

    function getAvailableColors() {
      const usedColors = new Set();
      yConnectedUserList.forEach((color, name) => {
        usedColors.add(color);
      });
      const availableColors = cursorColors.filter(color => !usedColors.has(color));
      return availableColors;
    }
    
    function getRandomColor() {
      const availableColors = getAvailableColors();
      const index = Math.floor(Math.random() * availableColors.length);
      return availableColors[index];
    }

    function updateUsersAndColors() {
      const updatedUsersAndColors = [];
      yConnectedUserList.forEach((color, name) => {
        updatedUsersAndColors.push({ name, color });
      });
      setUsersAndColors(updatedUsersAndColors);
    }

    const myCursorBuilder = (user) => {
      const cursor = document.createElement("span");
      cursor.classList.add("ProseMirror-yjs-cursor");
      cursor.setAttribute("style", `border-color: ${user.color}`);
      const userDiv = document.createElement("div");
      userDiv.setAttribute("style", `background-color: ${user.color}`);
      userDiv.innerText = user.name;
      cursor.appendChild(userDiv);

      return cursor;
    };

    yConnectedUserList.observe(updateUsersAndColors);
    window.addEventListener("pagehide", window.yjsDisconnect);
    window.addEventListener("unload", window.yjsDisconnect);
    window.addEventListener("popstate", window.yjsDisconnect);

    editorRef.current.addEventListener('mousedown', (event) => { handleNodeClick(nickname, event); });
    editorRef.current.addEventListener('keydown', (event) => { handleEditAttempt(nickname, event); });
    editorRef.current.addEventListener('mousedown', (event) => { handleEditAttempt(nickname, event); });

    const myDoc = DOMParser.fromSchema(mySchema).parse(
      document.createElement("div")
    );

    const view = new EditorView(editorRef.current, {
      state: EditorState.create({
        doc: myDoc,

        plugins: exampleSetup({ schema: mySchema }).concat(
          ySyncPlugin(yXmlFragment),
          yCursorPlugin(provider.awareness, {
            cursorBuilder: myCursorBuilder,
          }),
          yUndoPlugin(),
          hoverButtonPlugin(),
          inlinePlaceholderPlugin(),
          generateBlockIdPlugin(),
          imagePlugin({
            ...imageSettings,
            resizeCallback: (el, updateCallback) => {
              const observer = new ResizeObserver(entries => {
                window.requestAnimationFrame(() => {
                  updateCallback();
                });
              });
              observer.observe(el);
              return () => observer.unobserve(el);
            },
          }),          
          keymap({
            "Mod-z": undo,
            "Mod-y": redo,
            "Mod-Shift-z": redo,
          })
        ),
        selection: Selection.atStart(myDoc),
      }),
    });
    
    editorRef.current.view = view;

    return () => {
      setisloaded(false);
      yConnectedUserList.unobserve(updateUsersAndColors);
      yConnectedUserList.unobserve(onlineUpdate);
      window.removeEventListener("pagehide", window.yjsDisconnect);
      window.removeEventListener("unload", window.yjsDisconnect);
      window.removeEventListener("popstate", window.yjsDisconnect);
      window.yjsDisconnect();
    };
  }, [pageId]);

  useEffect(() => {
    console.log(clickedImageSrc);
  }, [clickedImageSrc]);

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

  return (
    <div>
      {!isloaded && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(255, 255, 255, 0.7)",
          }}
        >
          <img
            src={loadingImage}
            alt="Loading..."
            style={{
              width: "200px",
              height: "auto",
              borderBottom: "2px solid #bbbbbb",
            }}
          />
        </div>
      )}
        <LayoutContainer>
          <NavigationBar $isloaded={isloaded.toString()}>
          <NoteHeaderContainer>
            <Notename>
              <span>📖&nbsp;</span>
              <span>{noteinfo ? noteinfo.name : "Loading..."}</span>
            </Notename>
              <img src={noteinfo ? noteinfo.image : 'https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/NoneImage2.png'} alt="Note" />
              <NoteBtnContainer>
                <NoteBtn onClick={() => navigate(`/organization/${organizationId}`)}>
                  <FontAwesomeIcon icon={faList} />
                    &nbsp;&nbsp;&nbsp;노트 목록
                  </NoteBtn>
                  <NoteBtn onClick={handleOpenNoteSettingModal}>
                    노트 설정&nbsp;&nbsp;&nbsp;
                    <FontAwesomeIcon icon={faGear} />
                </NoteBtn>
              </NoteBtnContainer>
            </NoteHeaderContainer>
            <PageRemoteContainer>
              <PageCheck>
                <ArrowBox>
                  <FontAwesomeIcon icon={faLeftLong} onClick={prevPage} />
                  </ArrowBox>
                  {pageIndex !== 0 ? pageIndex + 1 : "메인"} 페이지
                  <ArrowBox>
                  <FontAwesomeIcon icon={faRightLong} onClick={nextPage} />
                </ArrowBox>
              </PageCheck>
              <PageRemote>
                 <LeftPageRemote>
                  <CreateRemoveBtn>                  
                    <FontAwesomeIcon icon={faSquarePlus} onClick={handleCreate} style={{ color: '#007bff', cursor: isPageHandleButtonDisabled ? 'not-allowed' : 'pointer' }}             
                    disabled={isPageHandleButtonDisabled} title="페이지 추가"/>
                    <FontAwesomeIcon icon={faTrashCan} onClick={handleRemove} style={{ color: '#707070', cursor: isPageHandleButtonDisabled ? 'not-allowed' : 'pointer' }}             
                    disabled={isPageHandleButtonDisabled} title="현재 페이지 삭제"/>
                  </CreateRemoveBtn>
                 </LeftPageRemote>
                 <RightPageRemote>
                  <InputContainer>
                    <InputPageNumber 
                      type="text"
                      pattern="[0-9]+" 
                      value={pageInputValue}
                      onInput={(e) => e.target.value = e.target.value.slice(0, 3)}
                      onChange={handlePageInputChange}
                    />
                      <PageDisplay>/ {pages ? pages.length : "Loading"} 페이지</PageDisplay>
                  </InputContainer>
                  <GoButton onClick={pageTarget}>이동하기</GoButton>
                 </RightPageRemote>
              </PageRemote>
            </PageRemoteContainer>
            <hr />
            <p style={{ fontWeight: "bold", marginBottom: "0px" }}>접속중인 유저 목록</p>
            <p style={{ marginTop:"0px" }}><small>(커서 색상/닉네임)</small></p>
           <ul>
            {usersAndColors.map(({ name, color }) => (
              <li key={name} style={{ display: 'flex', alignItems: 'center', marginBottom: '10px', marginLeft: '13px' }}>
                <div style={{ width: '20px', height: '20px', backgroundColor: color, marginRight: '13px' }}></div>
                {name} {name === nickname && "(본인)"}
              </li>
            ))}
          </ul>
          </NavigationBar>
          <EditorContainer>
            <div
              ref={editorRef}
              id="editor"
              style={{
                visibility: isloaded ? "visible" : "hidden",
                width: "100%",
                margin: "0 auto",
                paddingLeft: "8%",
                paddingRight: "5%",
              }}
            />
        </EditorContainer>
        </LayoutContainer>

        {showModalImage && (
        <ModalImageComponent 
          src={clickedImageSrc} 
          modalOpen={handleOpenImageZoomModal}
          closeModal={handleCloseImageZoomModal}
         />
      )}

        {noteSettingModalOpen && (
        <NoteSettingModal
          modalOpen={noteSettingModalOpen}
          handleCloseModal={handleCloseNoteSettingModal}
          myimage={myimage}
          uploadImage={uploadImage}
          note={noteinfo}
          setNoteInfo={setNoteInfo}
        />
      )}
    </div>
  );
}

const LayoutContainer = styled.div`
  display: flex;
`;

const EditorContainer = styled.div`
  flex: 1;
  display: flex;
  height: 200vh;
  margin-left: 15%; // 네비게이션 바 너비만큼 왼쪽 여백 추가
`;

const NavigationBar = styled.div`
  width: 13%; // 네비게이션 바 너비
  background-color: #eee; // 네비게이션 바 배경색
  position: fixed;
  height: 93%; // 전체 화면 높이
  padding: 20px; // 여백
  visibility: ${(props) => (props.$isloaded === "true" ? "visible" : "hidden")};
  // border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 5px;
  box-shadow: 6px 8px 4px #ccc;
  
  img {
    width: 200px; /* 너비 설정 */
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
    box-shadow: 1px 2px 1px #ccc;
  }

  & > p:nth-of-type(2) {
    margin-bottom: 0;
  }
  & > p:nth-of-type(3) {
    margin: 0;
  }

  @media screen and (max-width: 1500px) {
    img {
      width: auto;
      max-width: 100%; 
    }
  }

@media (max-width: 768px) {
    visibility: hidden;
  }
`;

const NoteHeaderContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; 
  padding-top: 15px;
  padding-bottom: 1px;
  width: 13vw;
  background-color: rgba(250, 190, 88, 0.1); 
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 5px; 

  img {
    width: 88%; /* 너비 설정 */
    height: auto;
    max-height: 220px;
  }
`;

const Notename = styled.div`
  display: flex;
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 10px;
  padding: 3px 10px;
  font-size: 18px;
  font-weight: bold;
  width: 80%;
  background-color: rgba(255, 253, 208, 0.8);
  border: 2px solid rgba(54, 69, 79, 0.2); 
  border-radius: 7px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  @media screen and (max-width: 1500px) {
      width: auto;
      max-width: 80%; 
  }
`;

const NoteBtnContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px;
  gap: 10px;

  @media (min-width: 1800px) {
    width: 88%; // 화면 너비가 1800px 이상일 때 버튼의 너비를 88%로 설정
  }
`;

const NoteBtn = styled.button`
  width: 100%;
  padding: 8px 10px; // 버튼 내부 여백
  border-radius: 4px; // 테두리 둥글게
  background-color: #6c757d; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경
  font-size: auto;

  &:hover {
    background-color: #555555; // 마우스 오버 시 버튼 배경색 변경
  }

  @media (max-width: 1700px) {
    font-size: 12px;
  }

  @media (max-width: 1600px) {
    font-size: 10px;
  }
`;

const PageRemoteContainer = styled.div`
  width: 100%; // 기본 너비
  margin-top: 15px;

  @media (min-width: 2000px) { // 화면 너비가 2560px 이상일 때
    width: 75%;
    margin: 15px auto;
  }
`;

const PageCheck = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  gap: 25px;
  border-radius: 5px;
  padding: 2px 0px;
  background-color: white;
  border: 2px outset rgba(255, 192, 203, 0.5);

  @media (max-width: 1600px) {
    font-size: 17px;
  }
`;

const PageRemote = styled.div`
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  background: white;
  height: 100px;
  position: relative;
  margin-top: 10px;
  margin-bottom: 20px;
`;

const LeftPageRemote = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; 
  height: 85%;
  border-right: 2px solid #ccc;
`;

const RightPageRemote = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-left: auto; /* 왼쪽에 자동 마진을 줘서 오른쪽 정렬 */
  margin-right: auto;
  width: fit-content; /* 내용에 맞는 너비 */
`;

// 입력 필드와 페이지 표시를 위한 스타일
const InputPageNumber = styled.input`
  width: 35px; // 입력칸의 너비
  text-align: center; // 텍스트 중앙 정렬
`;

const InputContainer = styled.div`
  display: flex; // Flex 컨테이너 설정
  flex-direction: row; // 가로 방향으로 나열
  align-items: center; // 요소들을 세로 방향으로 가운데 정렬
`;

const PageDisplay = styled.span`
  font-size: 16px; // 글씨 크기
  margin-left: 5px;

  @media (max-width: 1600px) {
    font-size: 13px;
  }
`;

const GoButton = styled.button`
  padding: 8px; // 버튼 내부 여백

  width: 100%;
  border-radius: 4px; // 테두리 둥글게
  background-color: #28a745; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경
  margin-top: 5px;

  &:hover {
    background-color: #218838; // 마우스 오버 시 버튼 배경색 변경
  }

  @media (max-width: 1400px) {
    width: 80%;
  }
`;

const CreateRemoveBtn = styled.div`
  width: 25px;
  padding: 20px;
  font-size: 30px;
  cursor: pointer;
`;
const ArrowBox = styled.div`
  width: 25px;
  height: auto;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 25px;
  color: #333333;

  &:hover{
    animation: horizontalBeat 1s infinite;
  }

  @keyframes horizontalBeat {
    0%, 100% {
      transform: translateX(0);
    }
    50% {
      transform: translateX(2px);
    }
  }

  @media (max-width: 1600px) {
    font-size: 20px;
  }
`;

export default Page;