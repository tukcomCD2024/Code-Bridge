import { useImperativeHandle, forwardRef } from 'react';
import { useNavigate } from 'react-router-dom';
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLock = forwardRef(({ ydocRef }, ref) => {
    const navigate = useNavigate();
    const yLineLocks = ydocRef.current.getMap('lineLocks');
    const yUserLocks = ydocRef.current.getMap('userLocks');
  
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
        } else {
          toastr.error(`[오류] ${currentLock} 에 의해 잠금 불가합니다.`);
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
  
    return null;
  });
  
  export default BlockLock;
  