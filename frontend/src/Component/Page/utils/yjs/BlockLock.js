import { useImperativeHandle, forwardRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLock = forwardRef(({ ydocRef }, ref) => {
    const navigate = useNavigate();
    const location = useLocation();
    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const pageId = pathSegments[3];

    const yLineLocks = ydocRef.current.getMap('nodeInfo');
    const yUserLocks = ydocRef.current.getMap('yUserLocks');
    const yRequestUnLock = ydocRef.current.getMap('yRequestUnLock');
    const yUnLockInfo = ydocRef.current.getMap('yUnLockInfo');
    const yResultUnLock = ydocRef.current.getMap('yResultUnLock');

    const toggleLineLock = (guid, nickname) => {
      if (!nickname) {
        toastr.info(`로그인 정보가 없습니다.`);
        navigate("/login");
        return;
      }
      const currentLock = yLineLocks.get(guid.toString());
      const currentLockedNodeByUser = yUserLocks.get(nickname);

      if (!currentLock && currentLockedNodeByUser && currentLockedNodeByUser !== guid.toString()) {
        const isConfirmed = window.confirm("최대 1개까지 잠금이 가능합니다.\n\n이전에 설정한 잠금을 해제하시겠습니까?");
        if (isConfirmed) {
          yLineLocks.delete(currentLockedNodeByUser);
          yUserLocks.delete(nickname);
        } else {
          return;
        }
      }
  
      toastr.remove();
      if (currentLock) {
        if (currentLock === nickname) {
          if (yResultUnLock.has(`${yRequestUnLock.get(nickname)?.requestor}`)) yResultUnLock.delete(`${yRequestUnLock.get(nickname).requestor}`);
          if (yRequestUnLock.has(currentLock)) yRequestUnLock.delete(currentLock);
          if (yUnLockInfo.has(currentLock)) yUnLockInfo.delete(currentLock);
          yLineLocks.delete(guid.toString());
          yUserLocks.delete(nickname);
          toastr.info(`편집 잠금이 해제되었습니다.`);
          return;
        }

        if (yRequestUnLock.has(currentLock) && !yUnLockInfo.has(currentLock)) {
          const message = yRequestUnLock.get(currentLock).requestor === nickname ? `응답 대기 중입니다...` : `${yRequestUnLock.get(currentLock).requestor} 이(가) 잠금 해제 요청 중입니다.`;
          toastr.warning(message);
        } else {
          if(checkRequestUnLockTimer(currentLock) && yRequestUnLock.get(nickname)?.requestor !== nickname){
            const isConfirmed = window.confirm(`${currentLock} 에게 블록 잠금 해제를 요청합니다.`);
            if (isConfirmed) yRequestUnLock.set(currentLock, { requestor: nickname });
          }
        }
      } else {
        yLineLocks.set(guid.toString(), nickname);
        yUserLocks.set(nickname, guid.toString());
        toastr.success(`블록 편집 잠금이 설정되었습니다.`);
      }
    };

    const checkRequestUnLock = () => {
      const nickname = localStorage.getItem("nickname");
      const currentLockedNodeByUser = yUserLocks.get(nickname);
      if (yRequestUnLock.has(nickname)) {
        const isConfirmed = window.confirm(`${yRequestUnLock.get(nickname).requestor} 이(가) 블록 잠금 해제를 요청하였습니다. \n\n최근 설정한 블록 잠금을 해제하시겠습니까?`);
          if (isConfirmed) {
            yLineLocks.delete(currentLockedNodeByUser);
            yUserLocks.delete(nickname);
            toastr.info(`편집 잠금이 해제되었습니다.`);
            yResultUnLock.set(`${yRequestUnLock.get(nickname).requestor}`, { responser: nickname, result: "accept" });
          } 
          else {
            yResultUnLock.set(`${yRequestUnLock.get(nickname).requestor}`, { responser: nickname, result: "deny" });
            yUnLockInfo.set(nickname, { responseTime: Date() });
          }
      }
    };

    const checkRequestUnLockTimer = (currentLock) => {
      const nickname = localStorage.getItem("nickname");
      if (yRequestUnLock.has(currentLock)) {
        const responseTime = new Date(yUnLockInfo.get(currentLock).responseTime);
        const currentTime = new Date();

        const timeDifference = currentTime - responseTime;
        
        // 시간 차이를 밀리초 단위로 계산 (1분 = 60,000밀리초)
        if (timeDifference < 60000) {
          alert(`${currentLock} 이(가) 해당 블록의 잠금 해제 요청을 최근에 거절했습니다. \n\n추가적인 요청은 ${Math.floor((60000 - timeDifference) / 1000)}초 후에 가능합니다.`);
          return false;
        } else {
          if (yResultUnLock.has(`${yRequestUnLock.get(nickname)?.requestor}`)) yResultUnLock.delete(`${yRequestUnLock.get(nickname).requestor}`);
          if (yRequestUnLock.has(currentLock)) yRequestUnLock.delete(currentLock);
          if (yUnLockInfo.has(currentLock)) yUnLockInfo.delete(currentLock);
          return true;
        }
      }
      return true;
    };
    
    const checkResultUnLock = () => {
      const nickname = localStorage.getItem("nickname");

      if (yResultUnLock.has(nickname)) {
        if(yResultUnLock.get(nickname).result === "accept") {
          toastr.info("상대방이 요청을 수락하였습니다.");
          yRequestUnLock.delete(yResultUnLock.get(nickname).responser.toString());
          yResultUnLock.delete(nickname);
        } else {
          toastr.error("상대방이 요청을 거절하였습니다.");
        }
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
  