import { useImperativeHandle, forwardRef } from 'react';
import { useLocation } from 'react-router-dom';
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLike = forwardRef(({ ydocRef }, ref) => {
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean); 
  const organizationId = pathSegments[1];
  const noteId = pathSegments[2];

  const userId = localStorage.getItem('userId');
  const refresh = localStorage.getItem("refresh");
  const access = localStorage.getItem("access");
  const hoverButton_like = document.querySelector(".hoverButton_like");

  const yLikeList = ydocRef.current.getMap(`yLikeList_${userId}`);
  
  const toggleLike = async (blockId, lover, heartReceiver) => {
    if (lover !== heartReceiver) {
      const currentLikeState = yLikeList.get(blockId);
      const newLikeState = !currentLikeState;
      yLikeList.set(blockId, newLikeState);
    }

    try {
      const response = await fetch("/api/user/note/block/likes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "access": access,
          "refresh": refresh,
        },
        body: JSON.stringify({ organizationId, noteId, lover, blockId, heartReceiver }),
      });

      const responseData = await response.text();
      toastr.remove();

      if (response.ok) {
        if (responseData.includes("좋아요 성공!")) {
          toastr.success(responseData);
          hoverButton_like.classList.replace('hoverButton_like', 'hoverButton_like_fullRedHeart');
        } else {
          toastr.info(responseData);
          hoverButton_like.classList.replace('hoverButton_like_fullRedHeart', 'hoverButton_like');
        }
      } else {
        toastr.error(responseData);
      }
    } catch (error) {
      console.error("Error: ", error);
      alert("처리 중 오류가 발생했습니다.");
    }
  };

  const getLikeList = (guid) => {
    const isLiked = yLikeList.get(guid.toString());
    return !!isLiked;
  };

  useImperativeHandle(ref, () => ({
    toggleLike,
    getLikeList,
  }));

  return null;
});

export default BlockLike;
