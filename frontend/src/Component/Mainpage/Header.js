import React from "react";
import styled from "styled-components";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBars } from "@fortawesome/free-solid-svg-icons";
import logo from "../../image/dragon.png";

//import "./OrganizationTemplate.css";

import { useNavigate } from "react-router-dom";

const Header = ({ toggle, setToggle }) => {
  const navigate = useNavigate();

  const onClickToggleHandler = () => {
    setToggle(!toggle);
  };

  return (
    <StHeader>
      <StNavLogo>
        <img src={logo} alt="logo" onClick={() => navigate("/")} />
      </StNavLogo>

      <StNavMenu>
        <li onClick={() => navigate("/login")}>로그인(삭제)</li>
        <li onClick={() => navigate("/signup")}>회원가입(삭제)</li>
        <li onClick={() => navigate("/")}>로그아웃</li>
        <li onClick={() => navigate("/editProfile")}>회원정보수정</li>
        <li onClick={() => navigate("/about")}>About</li>
      </StNavMenu>

      {toggle ? (
        <StNavMenuNone>
          <li onClick={() => navigate("/login")}>로그인(삭제)</li>
          <li onClick={() => navigate("/signup")}>회원가입(삭제)</li>
          <li onClick={() => navigate("/")}>로그아웃</li>
          <li onClick={() => navigate("/editProfile")}>회원정보수정</li>
          <li onClick={() => navigate("/about")}>About</li>
        </StNavMenuNone>
      ) : null}

      <StNavToggleBtn onClick={onClickToggleHandler}>
        <FontAwesomeIcon icon={faBars} />
      </StNavToggleBtn>
    </StHeader>
  );
};

const StHeader = styled.nav`
  display: flex;
  align-items: center;
  max-width: 1360px;
  margin: 0 auto;
  background-color: #ffff99;
  width: 100%;
  @media screen and (max-width: 768px) {
    flex-direction: column;
    gap: 0px;
    width: 100%;
  }
`;

const StNavLogo = styled.div`
  cursor: pointer; /* Set cursor to pointer */
  img {
    width: 150px;
    height: 100px;
  }
`;

const StNavMenu = styled.div`
  cursor: pointer; /* Set cursor to pointer */
  list-style: none;
  padding-right: 10px;
  margin-left: auto;
  display: flex;
  li {
    padding: 8px 12px;
  }
  li:hover {
    background-color: #48af48;
    border-radius: 4px;
  }
  @media screen and (max-width: 768px) {
    display: none;
  }
`;

const StNavMenuNone = styled.div`
  display: none;
  @media screen and (max-width: 768px) {
    display: flex;
    flex-direction: column;
    list-style: none;
    align-items: center;
    width: 80%;
    padding-bottom: 10px;
    gap: 5px;
    li {
      width: 100%;
      text-align: center;
      padding: 8px 12px;
      border: 1px solid black;
      cursor: pointer; /* Set cursor to pointer */
    }
    li:hover {
      background-color: #48af48;
      border-radius: 4px;
    }
  }
`;

const StNavToggleBtn = styled.span`
  display: none;
  cursor: pointer; /* Set cursor to pointer */
  position: absolute;
  top: 40px;
  right: 32px;
  font-size: 24px;
  color: #48af48;
  @media screen and (max-width: 768px) {
    display: block;
  }
`;

export default Header;
