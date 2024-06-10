import React, { useState, useRef, useEffect } from "react";
import { useLocation, useNavigate } from 'react-router-dom';
import styled, { keyframes, css } from "styled-components";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faIdBadge, faArrowRightFromBracket, faPlus, faTrashCan, faCircleInfo, faSquarePollVertical } from "@fortawesome/free-solid-svg-icons";
import { defaultEmoji, emojiList } from "../Utils/emojiList";
import logo_person from "../../image/logo_person.gif";

function EmojiPicker({ onSelect }) {
    return (
      <EmojiContainer>
        {emojiList.map((emoji) => (
          <EmojiSelectButton key={emoji} onClick={() => onSelect(emoji)}>
            {emoji}
          </EmojiSelectButton>
        ))}
      </EmojiContainer>
    );
  }

function OrganizationModal({
    modalRef,
    handleCloseModal,
    organizationName,
    setOrganizationName,
    myEmoji,
    setMyEmoji,
    isInvalid,
    handleCreate,
  }) {
    const handleSelectEmoji = (emoji) => {
      setMyEmoji(emoji);
    };
  
    return (
      <ModalContainer>
        <ModalContent ref={modalRef}>
          <CloseButton onClick={handleCloseModal}>X</CloseButton>
          <p style={{ fontWeight: "bold" }}>📚 Organization 생성하기</p>
          <OrganizationInputWrapper>
            <OrganizationInput
              id="OrganizationName"
              type="text"
              placeholder="Organization 이름을 입력해주세요."
              value={organizationName}
              $isInvalid={isInvalid}
              onChange={(e) => setOrganizationName(e.target.value)}
            />
          </OrganizationInputWrapper>
          <Emoji style={{ fontSize: "100px" }}>{myEmoji}</Emoji>
          <div>
            <EmojiPicker onSelect={handleSelectEmoji} />
          </div>
          <hr />
          <CreateButton disabled={isInvalid} onClick={handleCreate}>
            생성하기
          </CreateButton>
        </ModalContent>
      </ModalContainer>
    );
  }

function OrganizationCard({ organization }) {
  const navigate = useNavigate(); 
  const location = useLocation(); 
  const pathSegments = location.pathname.split('/').filter(Boolean); 
  const organizationId = pathSegments[1];

  const handleOrganizationClick = () => {
    if (organizationId !== organization.id) {
        navigate(`/organization/${organization.id}`);
    }
  };

  return (
      <OrgContainer onClick={handleOrganizationClick}>
          <span style={{ fontSize: '50px', padding: '0px 0px' }}>{organization.emoji || defaultEmoji}</span>
          <TextContainer>
              <Name>{organization?.name}</Name>
              <MembersCount>{organization.members?.length ? organization.members?.length : "1"}명의 멤버</MembersCount>
          </TextContainer>
      </OrgContainer>
  );
}

const OrganizationContainer = ({ OrgName, OrgEmoji, OrgId, removeOrganization, handleOpenOrganizationModal }) => {
    const nickname = localStorage.getItem("nickname");
    const userId = localStorage.getItem('userId');

    const navigate = useNavigate();
    const location = useLocation(); // 현재 위치 정보를 가져옴
    const pathSegments = location.pathname.split('/').filter(Boolean); 
    const organizationId = pathSegments[1];

    const modalRef = useRef();
    const [modalOpen, setModalOpen] = useState(false);
    const [myEmoji, setMyEmoji] = useState(defaultEmoji);
    const [organizationName, setOrganizationName] = useState("");
    const [organizations, setOrganizations] = useState([]);
    const [isInvalid, setIsInvalid] = useState(false);
    const [isOrgVisible, setIsOrgVisible] = useState(!!OrgName);

    useEffect(() => {
      setIsOrgVisible(!!OrgName);
    }, [OrgName, organizationId]);

    useEffect(() => {
        if(!userId){
          navigate("/login");
          alert("계정 정보가 없습니다. 로그인 후 접속하세요.");
          return;
        }
      }, [location]);

    useEffect(() => {
        const handleClickOutside = (event) => {
          if (modalRef.current && !modalRef.current.contains(event.target)) {
            setModalOpen(false);
            setMyEmoji(defaultEmoji);
            setOrganizationName("");
          }
        };
    
        if (modalOpen) {
          document.addEventListener("mousedown", handleClickOutside);
        }
    
        return () => {
          document.removeEventListener("mousedown", handleClickOutside);
        };
      }, [modalOpen]);

      useEffect(() => {
        const fetchOrganizations = async () => {
          try {
            const response = await fetch(`/api/user/organization/${userId}`);
              if (response.ok) {
                const data = await response.json();
                  const fetchedOrganizationData = data.map(org => ({
                  id: org.id,
                  name: org.name,
                  emoji: org.emoji,
                  members: org.members,
                }));
                setOrganizations(fetchedOrganizationData);
              } else {
                console.error(`${userId}의 Organization을 불러오는데 실패했습니다.`);
              }
            } catch (error) {
              console.error('Error fetching organizations:', error);
            }
          };
          fetchOrganizations();
        }, [location, userId]);
    
      const handleButtonClick = () => {
        setModalOpen(true);
      };
    
      const handleCloseModal = () => {
        setModalOpen(false);
        setMyEmoji(defaultEmoji);
        setOrganizationName("");
      };
    
      const handleCreate = async (e) => {
        e.preventDefault();
    
        if (organizationName === "") {
          setIsInvalid(true); // 유효성 상태 업데이트
          setTimeout(() => setIsInvalid(false), 800); // 800ms 후 유효성 상태 초기화
          return;
        }
    
        const owner = localStorage.getItem("email");
        const name = organizationName;
        const emoji = myEmoji; // Organization 대표 마크를 이모지로 설정함.
    
        const createOrganization = (organizationId) => {
          const newOrganization = {
            id: organizationId,
            name: organizationName,
            emoji: myEmoji,
          };
      
          const updatedOrganizations = [...organizations, newOrganization];
          setOrganizations(updatedOrganizations);
          handleCloseModal();
        };
      
        try {
          const response = await fetch("/api/user/organization", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ name, owner, emoji }),
          });
    
          if (response.ok) {
            const responseData = await response.json();
            const organizationId = responseData.organizationId;
            createOrganization(organizationId);
            console.log("생성 성공:", responseData);
            navigate(`/organization/${organizationId}`);
          } else {
            const errorData = await response.json();
            alert(`생성 실패: ${errorData.message}`);
          }
        } catch (error) {      
          console.error("Error: ", error);
          alert("처리 중 오류가 발생했습니다.");
        }
      };

    return (
        <LayoutContainer>
            <NavigationBar>
                <IdInfoContainer>
                    <MyName>
                    <span>📌&nbsp;</span>
                    <span>{nickname ? <> {nickname} 님 <small style={{ fontWeight: "normal" }}>환영합니다!</small> </> : "Loading..."}</span>
                    </MyName>
                    <IdInfoBtnContainer>
                        <IdInfoBtn onClick={() => navigate('/')}>
                            <FontAwesomeIcon icon={faArrowRightFromBracket} />
                            &nbsp;&nbsp;&nbsp;로그아웃
                        </IdInfoBtn>
                        <IdInfoBtn onClick={() => navigate('/editProfile')}>
                            회원정보수정&nbsp;&nbsp;&nbsp;
                            <FontAwesomeIcon icon={faIdBadge} />
                        </IdInfoBtn>
                    </IdInfoBtnContainer>
                </IdInfoContainer>
                <hr/>
                {isOrgVisible && (
                    <OrganizationInfo>
                      <div className="content-container">
                          <div className="image">
                          <p>{OrgEmoji ? OrgEmoji : "❓"}</p>
                          </div>
                          <div className="text-content">
                              <h1 className="title">{OrgName ? OrgName : "Loading..."}</h1>
                          </div>
                          <div className="trash-icon">
                              <FontAwesomeIcon icon={faTrashCan} onClick={removeOrganization} style={{ color: "#696969", padding:"5px", fontSize: "21px", cursor: "pointer"}} title="현재 Organization 삭제"/>
                          </div>
                          <div className="statistic-button" onClick={() => { OrgId ? navigate(`/contribution/${OrgId}`) : navigate(`/organization`) }} title="통계">
                              <FontAwesomeIcon icon={faSquarePollVertical} style={{ color: "#5F9EA0"}}/>
                              &nbsp;통계
                          </div>
                          <div className="info-button" onClick={handleOpenOrganizationModal} title="정보">
                              <FontAwesomeIcon icon={faCircleInfo} style={{ color: "#5F9EA0"}}/>
                              &nbsp;정보
                          </div>
                      </div>
                    </OrganizationInfo>
                )}
                <OrganizationList>
                    <AddOrganization onClick={handleButtonClick}>
                        <FontAwesomeIcon style={{ maxWidth: "5%", height: "auto" }} icon={faPlus} />
                    </AddOrganization>
                    {modalOpen && (
                        <OrganizationModal
                        modalRef={modalRef}
                        handleCloseModal={handleCloseModal}
                        organizationName={organizationName}
                        setOrganizationName={setOrganizationName}
                        myEmoji={myEmoji}
                        setMyEmoji={setMyEmoji}
                        isInvalid={isInvalid}
                        setIsInvalid={setIsInvalid}
                        handleCreate={handleCreate}
                        />
                    )}
                    <FetchedOrganizationList $expanded={!isOrgVisible}>
                    <hr/>
                    {organizations?.length > 0 ? (
                        organizations.map((org, index) => (
                        <OrganizationCard organization={org} index={index} key={org.id} />
                        ))
                    ) : (
                        <NoOrganizationMessage>
                        🔎 Organization 목록
                        </NoOrganizationMessage>
                    )}
                    </FetchedOrganizationList>
                </OrganizationList>
            </NavigationBar>
            {(!organizationId || !isOrgVisible) && (
            <ContentArea>
                <NoOrganizationMessage>
                    <img src={logo_person} alt="logo" />
                    {organizations?.length > 0 ? "📢 Organization 목록에서 선택하세요." : "📢 [+] 버튼을 눌러 Organization 을 생성해보세요!"}
                </NoOrganizationMessage>
            </ContentArea>
             )}
        </LayoutContainer>
    );
};

const LayoutContainer = styled.div`
  display: flex;
`;

const NavigationBar = styled.div`
  width: 20vw; // 네비게이션 바 너비
  height: 94vh; // 전체 화면 높이
  position: fixed;
  background-color: #eee; // 네비게이션 바 배경색
  padding: 20px; // 여백
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 5px;
  box-shadow: 6px 8px 4px #ccc;
  z-index: 1;
  
  img {
    width: 200px; /* 너비 설정 */
    object-fit: contain; /* 비율 유지 */
    border-radius: 5px; /* 이미지에 둥근 모서리 추가 */
    box-shadow: 1px 2px 1px #ccc;
  }

  & > p:nth-of-type(2) {
    margin-bottom: 0;
  }
  & > p:nth-of-type(3) {
    margin: 0;
  }

  @media screen and (max-width: 1500px) {
    img {
      width: auto;
      max-width: 100%; 
    }
  }

@media (max-width: 768px) {
    visibility: hidden;
  }
`;

const IdInfoContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; 
  padding: 10px 0px;
  margin-bottom: 15px;
  background-color: rgba(250, 190, 88, 0.1); 
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 5px; 

  img {
    width: 88%; /* 너비 설정 */
    height: auto;
    max-height: 220px;
  }
`;

const MyName = styled.div`
  display: flex;
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 10px;
  padding: 3px 10px;
  font-size: 18px;
  font-weight: 500;
  width: 80%;
  background-color: rgba(255, 253, 208, 0.8);
  border: 2px solid rgba(54, 69, 79, 0.2); 
  border-radius: 7px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  @media screen and (max-width: 1500px) {
      max-width: 72%; 
  }
`;

const IdInfoBtnContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80%;
  margin: 5px;
  gap: 10px;
`;

const IdInfoBtn = styled.button`
  width: 80%;
  padding: 8px 10px; // 버튼 내부 여백
  border-radius: 4px; // 테두리 둥글게
  background-color: #6c757d; // 버튼 배경색
  color: white; // 버튼 글자색
  border: none; // 테두리 제거
  cursor: pointer; // 마우스 오버 시 커서 변경
  font-size: auto;

  &:hover {
    background-color: #555555; // 마우스 오버 시 버튼 배경색 변경
  }

  @media (max-width: 1700px) {
    font-size: 14px;
  }

  @media (max-width: 1600px) {
    font-size: 12px;
  }

  @media (max-width: 1000px) {
    font-size: 10px;
  }
`;

const OrganizationList = styled.div`
  width: 100%; // 기본 너비
  height: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  border-radius: 5px;
  margin-top: 15px;
  padding-bottom: 5px;
  background-color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);

  @media (min-width: 2000px) { // 화면 너비가 2560px 이상일 때
    width: 75%;
    margin: 15px auto;
  }
`;

const FetchedOrganizationList = styled.div`
  overflow-x: hidden;
  overflow-y: auto;
  width: 100%;
  min-height: ${({ $expanded }) => ( $expanded ? '70vh' : '55vh')};
  max-height: calc(97vh - 41vh);

  &::-webkit-scrollbar {
    width: 7px;
  }

  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 10px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #555;
  }

  &::-webkit-scrollbar-corner {
    background: transparent;
  }
`;

const OrgContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: auto;
  margin: auto 5px;
  padding: 0px 10px;
  text-align: center;
  cursor: pointer;
  border-radius: 10px;

  & > span {
    filter: grayscale(100%);
    transition: filter 0.3s ease; 
  }

  &:hover > span {
    filter: grayscale(0%); 
  }

  &:hover {
    background-color: #eee;
  }
`;

const TextContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-end;
`;

const Name = styled.p`
  color: #000000;
  font-size: 19px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 240px; 
  display: block;
  margin: 0;
`;

const MembersCount = styled.p`
  color: #000000;
  font-size: 13px;
  margin: 0;
`;

const AddOrganization = styled.button`
  cursor:pointer;
  display: flex;
  position: relative;
  width: 95%;
  height: 50px;
  margin: 7px;
  margin-bottom: 0px;
  align-items: center;
  justify-content: center;
  color: #2F4F4F;
  background-color: #A9A9A9;
  border: none;
  border-radius:5px;
  top:0;
  box-shadow:inset 0 -8px 0 0 rgba(0,0,0,.2), 1px 1px 0 0 #A9A9A9;
  transition:all 0.06s ease-out;
  
  &:hover {
    background-color: #A0A0A0;
    color: #DCDCDC;
  }

  &:active{
    color: #DCDCDC;
    box-shadow:inset 0 -4px 0 0 rgba(0,0,0,.2), 1px 1px 0 0 #A9A9A9;
  }
`;

const OrganizationInfo = styled.div`
  .content-container {
    display: flex; /* Flex container 설정 */
    align-items: center; /* 가로축 중앙 정렬 */
    flex-wrap: nowrap;
    box-sizing: border-box;
    background-color: #ffe53b;
    background-image: linear-gradient(147deg, #ffe53b 0%, #fd3838 74%);
    width: 93%;
    height: 130px;
    position: relative;
    margin: auto;
    margin-left: 25px;
    background: #fff;
    box-shadow: 0px 14px 80px rgba(34, 35, 58, 0.2);
    padding: 25px;
    padding-top: 5px;
    padding-bottom: 10px;
    border: 1px solid #00FFFF;
    border-radius: 25px;
    transition: all 0.3s;
    margin-top: 15px;

    .image {
      width: 95px;
      height: 95px;
      background-image: linear-gradient(147deg, #E0FFFF 0%, #AFEEEE 74%);
      box-shadow: 4px 13px 30px 1px rgba(190, 237, 237, 0.9);
      border-radius: 20px;
      position: absolute; 
      left: -35px; 
      top: 50%; 
      transform: translateY(-50%);
      display: flex; 
      justify-content: center; 
      align-items: center;

      &:after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border-radius: 20px;
        opacity: 0.8;
      }

      p {
        font-size: 60px;
      }

      @media screen and (max-width: 992px) {
        width: 45%;
      }
      @media screen and (max-width: 768px) {
        width: 90%;
      }
      @media screen and (max-width: 576px) {
        width: 95%;
      }
    }

    .text-content {    
        @media screen and (max-width: 992px) {
          width: 55%;
        }
        @media screen and (max-width: 768px) {
          margin-top: -80px;
          padding: 0 30px;
        }
  
        @media screen and (max-width: 576px) {
          padding: 0;
        }
  
        .title {
          position: absolute;
          top: 0px;
          right: 25px;
          text-align: right;
          font-size: 25px;
          font-weight: 700;
          color: #0d0925;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          width: calc(100% - 125px); /* 아이콘 너비를 고려하여 최대 너비 설정 */
        }
      }
  
      .trash-icon {
        cursor: pointer;
        border-radius: 5px;
        margin: auto;
        margin-bottom: 0px;
        margin-left: 55px;
      }

       .info-button,
       .statistic-button {
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #AFEEEE;
        border-radius: 10px;
        margin-top: auto;
        margin-bottom: 5px;
        margin-right: 4px;
        padding: 1px 7px;
        font-size: 15px;
        font-weight: 500;

        @media (max-width: 1600px) {
          font-size: 13px;
          margin-right: 3px;
        }
      
        @media (max-width: 1200px) {
          font-size: 11px;
          margin-right: 2px;
        }
      
        @media (max-width: 800px) {
          font-size: 9px;
          margin-right: 1px;
        }
      }

      .trash-icon:hover{
        background-color: #dddddd; /* 호버 시 배경 색상 변경 */
      }
  
      .statistic-button:hover,
      .info-button:hover {
        background-color: #B0E0E6; /* 호버 시 배경 색상 변경 */
      }
    }
  `;

const Emoji = styled.p`
  font-size: 120px;
  padding: 0px 0px;

  @media screen and (max-width: 1000px) {
    padding: 0px 0px;
  }
`;

const EmojiContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  height: 80px;
  overflow-y: scroll;
  gap: 6px;
  border: 1px solid #cccccc;
  border-radius: 10px;
  padding: 5px;
  margin-bottom: 10px;

  &::-webkit-scrollbar {
    width: 7px; // 스크롤바의 너비
  }

  &::-webkit-scrollbar-thumb {
    background: #888; // 스크롤바 썸의 배경색
    border-radius: 10px; // 스크롤바 썸에 마우스 호버 시 색상
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #555; // 스크롤바 썸에 마우스 호버 시 색상
  }

  &::-webkit-scrollbar-corner {
    background: transparent; // 스크롤바 코너 배경을 투명하게 설정
  }
`;

const EmojiSelectButton = styled.button`
  font-size: 20px;
  border: none;
  background: none;
  cursor: pointer;

  &:hover {
    background-color: #cccccc;
    border-color: #cccccc;
    border-radius: 10px;
    color: #000000;
  }
`;

const ModalContainer = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
`;

const CloseButton = styled.button`
  position: absolute;
  color: red;
  font-size: 19px;
  font-weight: bold;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
`;

const OrganizationInputWrapper = styled.div`
  display: block;
  text-align: center;
  line-height: 40px;
  margin-bottom: 10px;
`;

const shakeAnimation = keyframes`
  0% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  50% { transform: translateX(5px); }
  75% { transform: translateX(-5px); }
  100% { transform: translateX(0); }
`;

const OrganizationInput = styled.input`
  width: 90%;
  border: none;
  outline: none;
  padding: 10px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #d0d0d0;

  ${(props) =>
    props.$isInvalid &&
    css`
      border: 2px solid red;
      animation: ${shakeAnimation} 0.5s ease-in-out;
    `}
`;

// 모달창_생성하기 버튼
const CreateButton = styled.button`
  display: block;
  width: 100%;
  font-size: 15px;
  text-align: center;
  line-height: 40px;
  border-radius: 10px;
  border-color: #cccccc;
  border-width: 1px; /* Add border-width property */
  border-style: solid; /* Add border-style property */
  background-color: #cccccc;
  cursor: pointer;

  &:hover {
    background: ${(props) => (props.disabled ? "#cccccc" : "#bbbbbb")};
  }

  &:disabled {
    background-color: #e0e0e0;
    color: #a0a0a0;
    cursor: not-allowed;
  }
`;

const ModalContent = styled.div`
  background: #ffffff;
  padding: 20px;
  border-radius: 10px;
  width: 300px;
  text-align: center;
  position: relative;
`;

const NoOrganizationMessage = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 60vh;
  font-weight: bold;
  font-size: 20px;
  color: #666;

  img {
    width: 100px;
    height: auto;
    margin-bottom: 15px;
  }
`;

const ContentArea = styled.div`
  flex: 1;
  height: 85vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 20vw;
`;

export default OrganizationContainer;