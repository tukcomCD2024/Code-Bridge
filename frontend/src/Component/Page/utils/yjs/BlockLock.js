import { useImperativeHandle, forwardRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Selection } from "prosemirror-state";
import Swal from "sweetalert2";
import toastr from 'toastr';
import 'toastr/build/toastr.css';
import "../../ProseMirror_css/ProseMirror.css";

const BlockLock = forwardRef(({ ydocRef, editorRef }, ref) => {
    const navigate = useNavigate();
    const location = useLocation();
    const nickname = localStorage.getItem("nickname");
    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const pageId = pathSegments[3];

    const yLineLocks = ydocRef.current.getMap('nodeInfo');
    const yUserLocks = ydocRef.current.getMap('yUserLocks');
    const yRequestUnLock = ydocRef.current.getMap('yRequestUnLock');
    const yConnectedUserList = ydocRef.current.getMap('connectedUsers');
    const yUnLockInfo = ydocRef.current.getMap('yUnLockInfo');
    const yRecentUnLockBlock = ydocRef.current.getArray('yRecentUnLockBlock');
    const yResultUnLock = ydocRef.current.getMap('yResultUnLock');
    const yReceivedMessage = ydocRef.current.getMap(`${nickname}_message`);

    const baseSwal = Swal.mixin({
      showCancelButton: true,
      confirmButtonColor: "#28a745",
      cancelButtonColor: "#6c757d",
      confirmButtonText: '확인',
      cancelButtonText: '취소'
    });

    const handlePopState = () => {
      if (Swal.isVisible()) Swal.close();
      if (baseSwal.isVisible()) baseSwal.close();
    };
  
    const isLoggedIn = () => {
      if (!nickname) {
        toastr.info(`로그인 정보가 없습니다.`);
        navigate("/login");
        return;
      }
    };

    function updateHoverDivPosition(hoverDiv, change) {
      const hoverDivcurrentTop = parseFloat(window.getComputedStyle(hoverDiv)?.top || "0");
      const newTop = window.matchMedia("(max-width: 768px)").matches ? hoverDivcurrentTop + change : hoverDivcurrentTop + (change * 2);
      hoverDiv.style.top = `${newTop}px`;
    }
    
    function addIdToParagraph(uuid) {
      const hoverDiv = document.querySelector(".hoverDiv");
      const view = editorRef.current.view;
      const { state, dispatch } = view;
      const { tr } = state;
    
      let paragraphNode = null;
      state.doc.descendants((node, pos) => {
        if (node.attrs.guid === uuid) {
          paragraphNode = { node, pos };
          return false; // 찾았으니 순회 중단
        }
        return true;
      });
    
      if (paragraphNode) {
        const { node, pos } = paragraphNode;
        const paragraphWithId = node.type.create({ ...node.attrs, id: 'locked' }, node.content, node.marks);
        tr.replaceWith(pos, pos + node.nodeSize, paragraphWithId);
        dispatch(tr);
        updateHoverDivPosition(hoverDiv, 2);
      }
    }
    
    function removeIdFromParagraph(uuid) {
      const hoverDiv = document.querySelector(".hoverDiv");
      const view = editorRef.current.view;
      const { state, dispatch } = view;
      const { tr } = state;
    
      let paragraphNode = null;
      state.doc.descendants((node, pos) => {
        if (node.attrs.guid === uuid) {
          paragraphNode = { node, pos };
          return false; // 찾았으니 순회 중단
        }
        return true;
      });
    
      if (paragraphNode) {
        const { node, pos } = paragraphNode;
        if (!node.isText && !node.isInline) {
          const { id, ...attrsWithoutId } = node.attrs;
          const newAttrs = { ...attrsWithoutId, id: "non-locked" };
          tr.setNodeMarkup(pos, null, newAttrs);
          dispatch(tr);
          updateHoverDivPosition(hoverDiv, -2);
        }
      }
    }    

    // 특정 UUID로 노드를 찾아 해당 노드의 위치로 커서를 이동시키는 함수
    function moveCursorToNodeWithUUID(uuid) {
      const view = editorRef.current.view;
      if (!view || !view.state) {
        console.error('올바른 에디터를 찾지 못했습니다.');
        return;
      }
    
      const { state } = view;
      const { doc } = state;
      const hoverDiv = document.querySelector(".hoverDiv");

      doc.descendants((node, pos) => {
        if (node.attrs.guid === uuid) {
          hoverDiv.style.visibility = "hidden";
          const resolvedPos = state.doc.resolve(pos);
          const transaction = view.state.tr.setSelection(Selection.near(resolvedPos));
          view.dispatch(transaction);
          view.focus();
          return true; 
        }
        return false; 
      });
    }

    const selfUnlockBlock = (locker, myLockedBlockId) => {
      const unlockRequestor = yRequestUnLock.get(locker)?.requestor;
      if (yResultUnLock.get(`${unlockRequestor}`)?.result === "deny") { 
        yResultUnLock.set(`${unlockRequestor}`, { responser: locker, result: "lateAccept", unlockedBlockID: myLockedBlockId }); 
        yRecentUnLockBlock.push([`${myLockedBlockId}`]);
      }
      removeYjsMapUnLockData(nickname);
      removeIdFromParagraph(myLockedBlockId);
      yLineLocks.delete(myLockedBlockId);
      yUserLocks.delete(nickname);
    };

    const removeYjsMapUnLockData = (locker) => {
      const unlockRequestor = yRequestUnLock.get(locker)?.requestor;
      const unlockedBlockID = yResultUnLock.get(unlockRequestor)?.unlockedBlockID;
      const yRecentUnLockBlockIndex = yRecentUnLockBlock.toArray().indexOf(unlockedBlockID?.toString())
      if (yResultUnLock.has(`${unlockRequestor}`)) yResultUnLock.delete(`${unlockRequestor}`);
      if (yRequestUnLock.has(locker)) yRequestUnLock.delete(locker);
      if (yUnLockInfo.has(locker)) yUnLockInfo.delete(locker);
      if (yRecentUnLockBlockIndex !== -1) yRecentUnLockBlock.delete(`${yRecentUnLockBlockIndex}`, 1);
    };

    const checkRequestUnLockTimer = async (locker) => {
      if (yUnLockInfo.has(locker)) {
        const responseTime = new Date(yUnLockInfo.get(locker).responseTime);
        const currentTime = new Date();

        const timeDifference = currentTime - responseTime;
        const expirationTime = yUnLockInfo.get(locker).nextResponseTime * 1000;
        let timerInterval;
        // 시간 차이를 밀리초 단위로 계산 (1분 = 60,000밀리초)
        if (timeDifference < expirationTime) {
          const result = await baseSwal.fire({ html: `<strong style="font-size: 1.1em; font-weight: bold;">${locker}</strong> <strong style="font-size: 1.0em;">이(가) 해당 블록의 잠금 해제 요청을 거절했습니다.</strong>`,
                                              footer: `추가적인 요청은 <moreRequest></moreRequest>초 후에 가능합니다.`,
                                              icon: "error",
                                              timer: expirationTime - timeDifference,
                                              timerProgressBar: true,
                                              showConfirmButton: false,
                                              cancelButtonText: '확인',
                                              didOpen: () => {
                                                const timer = baseSwal.getPopup().querySelector("moreRequest");
                                                timerInterval = setInterval(() => {
                                                  const timeLeft = (Swal.getTimerLeft() / 1000).toFixed(0);
                                                  timer.textContent = timeLeft;
                                                }, 100);
                                                window.addEventListener('popstate', handlePopState);
                                              },
                                              willClose: () => {
                                                clearInterval(timerInterval);
                                                window.removeEventListener('popstate', handlePopState);
                                              }
                                          });
          if (result.isDismissed) {
            return false;
          }
        } else {
          removeYjsMapUnLockData(locker);
          return true;
        }
      }
      return true;
    };

    const checkRequestUnLock = async () => {
      const myLockedBlockId = yUserLocks.get(nickname);
      const unlockRequestor = yRequestUnLock.has(nickname) ? yRequestUnLock.get(nickname).requestor : null;

      if (unlockRequestor && myLockedBlockId && unlockRequestor !== nickname) {
        let timerInterval;
        let forcedModalClose = false;
        const expirationTime = 30000; // 요청 만료 시간(30초)
        const result = await baseSwal.fire({ html: `<strong>${unlockRequestor} 이(가) 블록 잠금 해제를 요청하였습니다.</strong>
                                                    <br/>
                                                    <small style="color: #008080; font-weight: bold;">최근 설정한 블록 잠금을 해제하시겠습니까?</small>
                                                    <br/><br/>
                                                    <small>거절 시, 다음 요청 가능 시간(초)을 설정해주세요.</small>`,
                                            footer: `<expiration></expiration> 초 안에 응답이 없을 경우 잠금이 해제됩니다.`,
                                            icon: "warning",
                                            timer: expirationTime,
                                            timerProgressBar: true,
                                            allowOutsideClick: false,
                                            allowEscapeKey: false,
                                            allowEnterKey: false,
                                            confirmButtonText: '수락',
                                            showDenyButton: true,
                                            showCancelButton: false,
                                            denyButtonText: '거절',
                                            input: "range",
                                            inputAttributes: {
                                              min: "30",
                                              max: "180",
                                              step: "10"
                                            },
                                            inputValue: 30,
                                            didOpen: () => {
                                              const swalRange = document.querySelector('.swal2-range');
                                              swalRange.style.marginTop = '-15px';
                                              const timer = baseSwal.getPopup().querySelector("expiration");
                                              timerInterval = setInterval(() => {
                                                if (yResultUnLock.has(`${unlockRequestor}`)) {
                                                  baseSwal.close();
                                                  forcedModalClose = true;
                                                  toastr.info("동일 계정에서 응답하였습니다.");
                                                  return;
                                                }
                                                const timeLeft = (Swal.getTimerLeft() / 1000).toFixed(1);
                                                timer.textContent = timeLeft;
                                                if (timeLeft <= 10 && timeLeft > 6) {
                                                  timer.parentElement.classList.add('pulsate_orange');
                                                } else if (timeLeft <= 6) {
                                                  timer.parentElement.classList.remove('pulsate_orange');
                                                  timer.parentElement.classList.add('pulsate_red');
                                                } else {
                                                  timer.parentElement.classList.remove('pulsate_red');
                                                }
                                              }, 100);
                                            },
                                            willClose: () => {
                                              clearInterval(timerInterval);
                                            },
                                            preDeny: () => {
                                              const inputRangeValue = Swal.getInput().value;
                                              return inputRangeValue;
                                            }
                                          });
        if (result.isConfirmed || result.dismiss === baseSwal.DismissReason.timer) {
          yLineLocks.delete(myLockedBlockId);
          yUserLocks.delete(nickname);
          removeIdFromParagraph(myLockedBlockId);
          toastr.info(`편집 잠금이 해제되었습니다.`);
          yRecentUnLockBlock.push([`${myLockedBlockId}`]);
          yResultUnLock.set(`${unlockRequestor}`, { responser: nickname, result: "accept", unlockedBlockID: myLockedBlockId });
        } else if (forcedModalClose === false) {
          const yReceivedMessage = ydocRef.current.getMap(`${unlockRequestor}_message`);
          yReceivedMessage.set(`${unlockRequestor}`, { message: "상대방이 요청을 거절하였습니다." });
          yResultUnLock.set(`${unlockRequestor}`, { responser: nickname, result: "deny" });
          yUnLockInfo.set(nickname, { responseTime: Date(), nextResponseTime: result.value });
        }
      }
    };
    
    const checkResultUnLock = async () => {
      if (yResultUnLock.has(nickname)) {
        const unlockResponser = yResultUnLock.get(nickname)?.responser;
        const unlockedBlockID = yResultUnLock.get(nickname)?.unlockedBlockID;
        const isOnline = yConnectedUserList.has(unlockResponser);
        if (isOnline && yResultUnLock.get(nickname).result === "accept") {
         const result = await Swal.fire({
            toast: true,
            icon: "success",
            title: "상대방이 요청을 수락하였습니다.",
            position: "top-end",
            showConfirmButton: true,
            confirmButtonText: '요청한 블록으로 이동하기',
            confirmButtonColor: "rgba(60, 200, 130)",
            timer: 3000,
            timerProgressBar: true,
            didOpen: (toast) => {
              toast.onmouseenter = Swal.stopTimer;
              toast.onmouseleave = Swal.resumeTimer;
            },
            willClose: () => {
              removeYjsMapUnLockData(unlockResponser);
            }
          });
          if (result.isConfirmed) moveCursorToNodeWithUUID(unlockedBlockID);
        } else if (isOnline && yResultUnLock.get(nickname).result === "lateAccept") {
          const unlockedBlockID = yResultUnLock.get(nickname)?.unlockedBlockID;
          const result = await baseSwal.fire({ html: `<strong style="font-size: 1.0em; font-weight: bold;">이전에 요청하였으나 거절된 블록 잠금이 해제 상태입니다.</strong>
                                                <br/>
                                                <small>지금 즉시 해당 위치로 이동하시겠습니까?</small>`,
                                         icon: "info",
                                       });
          if (result.isConfirmed) moveCursorToNodeWithUUID(unlockedBlockID);
          removeYjsMapUnLockData(unlockResponser);
        } else if (isOnline && yResultUnLock.get(nickname).result === "deny") {
          if (yReceivedMessage.has(nickname)) { 
            toastr.error(yReceivedMessage.get(nickname).message.toString());
            yReceivedMessage.delete(nickname);
          };
        } else {
          removeYjsMapUnLockData(unlockResponser);
        }
      }
    };

    const toggleLineLock = async (guid) => {
      try {
        const locker = yLineLocks.get(guid.toString());
        const myLockedBlockId = yUserLocks.get(nickname);

        isLoggedIn();
        toastr.remove();

        if (yRecentUnLockBlock.toArray().indexOf(guid.toString()) !== -1) { toastr.warning(`<strong>잠시 후 시도하세요</strong>. <br/> 사유: 블록 잠금 정보가 남아있음`); return; }

        if (locker && locker !== nickname && myLockedBlockId && myLockedBlockId !== guid.toString()) {
          const result = await baseSwal.fire({
            title: "🔓",
            html: `<strong>기존에 설정한 잠금을 해제 후 요청을 보내시겠습니까?<strong/>`,
          });
      
          if (result.isConfirmed) {
            removeIdFromParagraph(myLockedBlockId);
            yLineLocks.delete(myLockedBlockId);
            yUserLocks.delete(nickname);
          } else {
            return;
          }
        }

        if (!locker && myLockedBlockId && myLockedBlockId !== guid.toString()) {
          const result = await baseSwal.fire({
            title: "최대 1개의 블록 잠금이 허용됩니다.",
            text: "이전에 설정한 잠금을 해제하시겠습니까?",
            icon: "warning",
          });
      
          if (result.isConfirmed) {
            selfUnlockBlock(nickname, myLockedBlockId)
          } else {
            return;
          }
        }
    
        if (locker) {
          if (locker === nickname) {
            selfUnlockBlock(locker, myLockedBlockId)
            toastr.info(`편집 잠금이 해제되었습니다.`);
            return;
          }
          if (yRequestUnLock.has(locker) && !yUnLockInfo.has(locker)) {
            const requestor = yRequestUnLock.get(locker)?.requestor
            const message = requestor === nickname ? `이전 요청을 처리 중입니다...` : `<strong>${requestor}</strong> 이(가) 잠금 해제 요청 중입니다.`;
            toastr.warning(message);
          } else {
            const beforeRequest = await checkRequestUnLockTimer(locker);
            if(beforeRequest) {
              const result = await baseSwal.fire({
                title: "✉️",
                html: `<strong style="font-size: 1.2em; font-weight: bold;">${locker}</strong> <strong style="font-size: 1.1em;">에게 블록 잠금 해제를 요청합니다.</strong>`,
              });
              if (result.isConfirmed) {
                if (!yUserLocks.has(locker) || guid?.toString() !== yUserLocks.get(locker)?.toString()) {
                  toastr.remove();
                  toastr.warning(`<strong>다시 시도하세요</strong>. <br/> 사유: 블록 잠금 정보가 변경됨`);
                } else if (yRequestUnLock.has(locker)) {
                  const requestor = yRequestUnLock.get(locker)?.requestor;
                  toastr.remove();
                  toastr.warning(`${requestor} 이(가) 이미 요청했습니다.`);
                } else {
                  yRequestUnLock.set(locker, { requestor: nickname });
                }
              }
            }
          } 
        } else {
          removeYjsMapUnLockData(nickname);
          ydocRef.current.transact(() => {
            yLineLocks.set(guid.toString(), nickname);
            yUserLocks.set(nickname, guid.toString());
            addIdToParagraph(guid.toString());
            toastr.success(`블록 편집 잠금이 설정되었습니다.`);
          });
        }
      } catch (error) {
        const hoverDiv = document.querySelector(".hoverDiv");
        hoverDiv.style.visibility = "hidden";
        toastr.remove();
        toastr.warning(`다시 시도하세요.`);
        console.error(error);
      }
    };

    useImperativeHandle(ref, () => ({
        toggleLineLock,
        removeIdFromParagraph,
    }));

    useEffect(() => {
      const checkRequestUnLockWrapper = (event) => {
        event.changes.keys.forEach((change) => {
          if (change.action === 'add' || change.action === 'update' ) {
            checkRequestUnLock();
          }
        });
      };
    
      const checkResultUnLockWrapper = (event) => {
        event.changes.keys.forEach((change) => {
          if (change.action === 'add' || change.action === 'update' ) {
            checkResultUnLock();
          }
        });
      };
        window.addEventListener('popstate', handlePopState);
        yRequestUnLock.observe(checkRequestUnLockWrapper);
        yResultUnLock.observe(checkResultUnLockWrapper);
      return () => {
        removeYjsMapUnLockData(nickname);
        window.removeEventListener('popstate', handlePopState);
        yRequestUnLock.unobserve(checkRequestUnLockWrapper);
        yResultUnLock.unobserve(checkResultUnLockWrapper);
      };
    }, [pageId, ydocRef.current]); 
  
    return null;
  });
  
  export default BlockLock;
  