import React, { useImperativeHandle, forwardRef } from 'react';
import { useLocation } from 'react-router-dom';
import toastr from 'toastr';
import 'toastr/build/toastr.css';

const BlockLike = forwardRef(({ ydocRef }, ref) => {
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean); 
  const organizationId = pathSegments[1];
  const noteId = pathSegments[2];

  const userId = localStorage.getItem('userId');
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
        },
        body: JSON.stringify({ organizationId, noteId, lover, blockId, heartReceiver }),
      });

      const responseData = await response.text();
      toastr.remove();

      if (response.ok) {
        if (responseData.includes("좋아요 성공!")) {
          toastr.success(responseData);
        } else {
          toastr.info(responseData);
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
