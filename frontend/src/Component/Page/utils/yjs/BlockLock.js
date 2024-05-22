import { useImperativeHandle, forwardRef, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLock = forwardRef(({ ydocRef }, ref) => {
    const navigate = useNavigate();
    const yLineLocks = ydocRef.current.getMap('nodeInfo');
    const yUserLocks = ydocRef.current.getMap('yUserLocks');
    const yRequestUnLock = ydocRef.current.getMap('yRequestUnLock');
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
        const isConfirmed = window.confirm("최대 1개까지 잠금이 가능합니다.\n이전에 설정한 잠금을 해제하시겠습니까?");
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
          yLineLocks.delete(guid.toString());
          yUserLocks.delete(nickname);
          toastr.info(`편집 잠금이 해제되었습니다.`);
          return;
        }
        const requestor = yRequestUnLock.get(currentLock);
        if (requestor) {
          const message = requestor === nickname ? `응답 대기 중입니다...` : `${requestor} 님이 이미 잠금 해제 요청을 했습니다.`;
          toastr.warning(message);
        } else {
          const isConfirmed = window.confirm(`${currentLock}님에게 블록 잠금 해제를 요청합니다.`);
          if (isConfirmed) yRequestUnLock.set(currentLock, nickname);
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
        const isConfirmed = window.confirm(`${yRequestUnLock.get(nickname)}님이 블록 잠금 해제를 요청하셨습니다. \n\n블록 잠금을 해제하시겠습니까?`);
          if (isConfirmed) {
            yLineLocks.delete(currentLockedNodeByUser);
            yUserLocks.delete(nickname);
            toastr.info(`편집 잠금이 해제되었습니다.`);
            yResultUnLock.set(yRequestUnLock.get(nickname).toString(), { who: nickname, result: "accept"});
          } 
          else {
            yResultUnLock.set(yRequestUnLock.get(nickname).toString(), { who: nickname, result: "deny"});
          }
      }
    };

    const checkResultUnLock = () => {
      const nickname = localStorage.getItem("nickname");

      if (yResultUnLock.has(nickname)) {
        if(yResultUnLock.get(nickname).result === "accept") {
          toastr.info("상대방이 요청을 수락하였습니다.");
          yRequestUnLock.delete(yResultUnLock.get(nickname).who.toString());
          yResultUnLock.delete(nickname);

        } else {
          toastr.error("상대방이 요청을 거절하였습니다.");
          yRequestUnLock.delete(yResultUnLock.get(nickname).who.toString());
          yResultUnLock.delete(nickname);
        }
      }
    };
  
    useImperativeHandle(ref, () => ({
        toggleLineLock,
    }));

    useEffect(() => {
      yRequestUnLock.observe(checkRequestUnLock)
      yResultUnLock.observe(checkResultUnLock)
      return () => {
        yRequestUnLock.unobserve(checkRequestUnLock); 
        yResultUnLock.unobserve(checkResultUnLock)
      };
    }, []); 
  
    return null;
  });
  
  export default BlockLock;
  