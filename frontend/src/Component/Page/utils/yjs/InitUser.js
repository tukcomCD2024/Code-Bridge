import { useImperativeHandle, forwardRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { cursorColors } from "../../../Utils/cursorColor"
import toastr from 'toastr';
import 'toastr/build/toastr.css';

export const myCursorBuilder = (user) => {
    const cursor = document.createElement("span");
    cursor.classList.add("ProseMirror-yjs-cursor");
    cursor.setAttribute("style", `border-color: ${user.color}`);
    const userDiv = document.createElement("div");
    userDiv.setAttribute("style", `background-color: ${user.color}`);
    userDiv.insertBefore(document.createTextNode(user.name), null)
    const nonbreakingSpace1 = document.createTextNode('\u2060')
    const nonbreakingSpace2 = document.createTextNode('\u2060')
    cursor.insertBefore(nonbreakingSpace1, null)
    cursor.insertBefore(userDiv, null)
    cursor.insertBefore(nonbreakingSpace2, null)

    return cursor;
  };

const InitUser = forwardRef(({ editorRef, ydocRef, ydocProviderRef, usersAndColors, setUsersAndColors }, ref) => {
    const navigate = useNavigate();
    const location = useLocation();
    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const organizationId = pathSegments[1];
    const pageId = pathSegments[3];
    const nickname = localStorage.getItem('nickname');

    const yConnectedUserList = ydocRef.current.getMap('connectedUsers');

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
  
      function handleUserConnection() {
        const nicknameWithSuffix = `${nickname}_다중 접속`;
        const isSingleConnected = yConnectedUserList.has(nickname);
        const isMultiConnected = yConnectedUserList.has(nicknameWithSuffix);
    
        if (isSingleConnected && isMultiConnected) {
          const isConfirmed = window.confirm("동시 접속 가능한 횟수를 초과하셨습니다.\n기존 접속을 종료하고 새로 접속하시겠습니까?");
          if (isConfirmed) {
            yConnectedUserList.set(nicknameWithSuffix, 'kicked');
          } else {
            navigate(`/organization/${organizationId}`);
            return;
          }
        }
    
        let userColor = yConnectedUserList.get(nickname) || yConnectedUserList.get(nicknameWithSuffix) || getRandomColor();
        
        if (!isSingleConnected) {
          yConnectedUserList.set(nickname, userColor);
          ydocProviderRef.current.awareness.setLocalStateField('user', { name: nickname, color: userColor });
        } else {
          yConnectedUserList.set(nicknameWithSuffix, userColor);
          ydocProviderRef.current.awareness.setLocalStateField('user', { name: nicknameWithSuffix, color: userColor });
        }
        updateUsersAndColors(); // UI 업데이트
      }
      
      function onlineUpdate() {
        const userState = ydocProviderRef.current.awareness.getLocalState();
      
        if (userState && userState.user && userState.user.name) {
          const nickname = userState.user.name;
      
          if (!editorRef.current) {
            yConnectedUserList.delete(nickname);
          }
      
          if (yConnectedUserList.get(nickname) === 'kicked') {
            toastr.remove();
            toastr.warning("연결 정보가 없습니다!");
            navigate(`/organization/${organizationId}`);
            return;
          }
        }
        updateUsersAndColors();
      }
  
    useImperativeHandle(ref, () => ({
        handleUserConnection,
    }));
  
    useEffect(() => {
        updateUsersAndColors();
        yConnectedUserList.observe(onlineUpdate);
        yConnectedUserList.observe(updateUsersAndColors);

        return () => {
            yConnectedUserList.unobserve(onlineUpdate);
            yConnectedUserList.unobserve(updateUsersAndColors);
        };
    }, [pageId, usersAndColors]);

    return null;
  });
  
  export default InitUser;
  