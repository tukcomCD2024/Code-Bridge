import { useImperativeHandle, forwardRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Selection } from "prosemirror-state";
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLock = forwardRef(({ ydocRef, editorRef }, ref) => {
    const navigate = useNavigate();
    const location = useLocation();
    const nickname = localStorage.getItem("nickname");
    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const pageId = pathSegments[3];

    const yLineLocks = ydocRef.current.getMap('nodeInfo');
    const yUserLocks = ydocRef.current.getMap('yUserLocks');
    const yRequestUnLock = ydocRef.current.getMap('yRequestUnLock');
    const yUnLockInfo = ydocRef.current.getMap('yUnLockInfo');
    const yResultUnLock = ydocRef.current.getMap('yResultUnLock');

    const isLoggedIn = () => {
      if (!nickname) {
        toastr.info(`로그인 정보가 없습니다.`);
        navigate("/login");
        return;
      }
    };

    const selfUnlockBlock = (locker, myLockedBlockId) => {
      const unlockRequestor = yRequestUnLock.get(nickname)?.requestor;
      if (yResultUnLock.has(`${unlockRequestor}`)) yResultUnLock.set(`${unlockRequestor}`, { responser: nickname, result: "lateAccept", unlockedBlockID: myLockedBlockId });
      removeYjsMapUnLockData(locker);
      yLineLocks.delete(myLockedBlockId);
      yUserLocks.delete(nickname);
    };

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

    const removeYjsMapUnLockData = (locker) => {
      const unlockRequestor = yRequestUnLock.get(locker)?.requestor;
      if (yResultUnLock.has(`${unlockRequestor}`)) yResultUnLock.delete(`${unlockRequestor}`);
      if (yRequestUnLock.has(locker)) yRequestUnLock.delete(locker);
      if (yUnLockInfo.has(locker)) yUnLockInfo.delete(locker);
    };

    const checkRequestUnLock = () => {
      const myLockedBlockId = yUserLocks.get(nickname);
      const unlockRequestor = yRequestUnLock.get(nickname)?.requestor;

      if (yRequestUnLock.has(nickname) && unlockRequestor !== nickname) {
        const isConfirmed = window.confirm(`${unlockRequestor} 이(가) 블록 잠금 해제를 요청하였습니다. \n\n최근 설정한 블록 잠금을 해제하시겠습니까?`);
          if (isConfirmed) {
            yLineLocks.delete(myLockedBlockId);
            yUserLocks.delete(nickname);
            toastr.info(`편집 잠금이 해제되었습니다.`);
            yResultUnLock.set(`${unlockRequestor}`, { responser: nickname, result: "accept" });
          } 
          else {
            yResultUnLock.set(`${unlockRequestor}`, { responser: nickname, result: "deny" });
            yUnLockInfo.set(nickname, { responseTime: Date() });
          }
      }
    };

    const checkRequestUnLockTimer = (locker) => {
      if (yRequestUnLock.has(locker)) {
        const responseTime = new Date(yUnLockInfo.get(locker).responseTime);
        const currentTime = new Date();

        const timeDifference = currentTime - responseTime;
        
        // 시간 차이를 밀리초 단위로 계산 (1분 = 60,000밀리초)
        if (timeDifference < 60000) {
          alert(`${locker} 이(가) 해당 블록의 잠금 해제 요청을 최근에 거절했습니다. \n\n추가적인 요청은 ${Math.floor((60000 - timeDifference) / 1000)}초 후에 가능합니다.`);
          return false;
        } else {
          removeYjsMapUnLockData(locker);
          return true;
        }
      }
      return true;
    };
    
    const checkResultUnLock = () => {
      if (yResultUnLock.has(nickname)) {
        if(yResultUnLock.get(nickname).result === "accept") {
          const locker = yResultUnLock.get(nickname).responser.toString();
          removeYjsMapUnLockData(locker);
          toastr.info("상대방이 요청을 수락하였습니다.");
          return;
        } else if (yResultUnLock.get(nickname).result === "lateAccept") {
          const isConfirmed = window.confirm("이전에 요청하였으나 거절된 블록 잠금이 해제되었습니다.\n\n지금 즉시 해당 위치로 이동하시겠습니까?\n※ 다른 사용자가 먼저 잠금을 걸었을 수도 있습니다.");
          if (isConfirmed) moveCursorToNodeWithUUID(yResultUnLock.get(nickname).unlockedBlockID);
          return;
        } else {
          toastr.error("상대방이 요청을 거절하였습니다.");
          return;
        }
      }
    };

    const toggleLineLock = (guid, nickname) => {
      const locker = yLineLocks.get(guid.toString());
      const myLockedBlockId = yUserLocks.get(nickname);

      isLoggedIn();
      toastr.remove();

      if (locker && locker !== nickname && myLockedBlockId && myLockedBlockId !== guid.toString()) {
        const isConfirmed = window.confirm("[알림] 기존에 잠금 설정된 블록이 존재합니다. \n\n이전에 설정한 잠금을 해제 후 요청을 보내시겠습니까?")
        if (isConfirmed) {
          yLineLocks.delete(myLockedBlockId);
          yUserLocks.delete(nickname);
        } else {
          return;
        }
      }

      if (!locker && myLockedBlockId && myLockedBlockId !== guid.toString()) {
        const isConfirmed = window.confirm("최대 1개까지 잠금이 가능합니다.\n\n이전에 설정한 잠금을 해제하시겠습니까?");
        if (isConfirmed) {
          selfUnlockBlock(locker, myLockedBlockId)
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
          const message = requestor === nickname ? `응답 대기 중입니다...` : `${requestor} 이(가) 잠금 해제 요청 중입니다.`;
          toastr.warning(message);
        } else {
          if(checkRequestUnLockTimer(locker)) {
            const isConfirmed = window.confirm(`${locker} 에게 블록 잠금 해제를 요청합니다.`);
            if (isConfirmed) yRequestUnLock.set(locker, { requestor: nickname });
          }
        } 
      } else {
        yLineLocks.set(guid.toString(), nickname);
        yUserLocks.set(nickname, guid.toString());
        toastr.success(`블록 편집 잠금이 설정되었습니다.`);
      }
    };

    useImperativeHandle(ref, () => ({
        toggleLineLock,
    }));

    useEffect(() => {
      const checkRequestUnLockWrapper = (event) => {
        event.changes.keys.forEach((change) => {
          if (change.action === 'add' || change.action === 'update') {
            checkRequestUnLock();
          }
        });
      };
  
      const checkResultUnLockWrapper = (event) => {
        event.changes.keys.forEach((change) => {
          if (change.action === 'add' || change.action === 'update') {
            checkResultUnLock();
          }
        });
      };
  
      yRequestUnLock.observe(checkRequestUnLockWrapper);
      yResultUnLock.observe(checkResultUnLockWrapper);
      return () => {
        yRequestUnLock.unobserve(checkRequestUnLockWrapper);
        yResultUnLock.unobserve(checkResultUnLockWrapper);
      };
    }, [pageId]); 
  
    return null;
  });
  
  export default BlockLock;
  