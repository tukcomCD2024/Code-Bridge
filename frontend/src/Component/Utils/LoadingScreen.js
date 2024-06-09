import React from 'react';
import loadingImage from "../../image/loading.gif";
import backgroundImage from "../../image/organizationBackgroundImage.png";

const LoadingScreen = () => {
  return (
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
      }}
    >
      <div
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'repeat-y',
          filter: "blur(2px)",
          zIndex: -1, 
        }}
      ></div>
      <img
        src={loadingImage}
        alt="Loading..."
        style={{
          width: "310px",
          height: "auto",
        }}
      />
    </div>
  );
};

export default LoadingScreen;
