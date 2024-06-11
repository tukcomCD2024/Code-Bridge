import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import styled from "styled-components";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHouse, faArrowUpWideShort, faArrowDownWideShort, faIdCard, faCircleQuestion, faHeartCircleCheck } from "@fortawesome/free-solid-svg-icons";
import "../../styles.css"

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ChartDataLabels 
);

function ContributionPage() {
  const { id } = useParams();
  const organizationId = String(id);
  const navigate = useNavigate();
  const nickname = localStorage.getItem("nickname");

  const [displayTotalScores, setDisplayTotalScores] = useState(true); // 종합 점수
  const [displayQuizScores, setDisplayQuizScores] = useState(false); // 퀴즈 점수만 표시
  const [displayLikeScores, setDisplayLikeScores] = useState(false); // 좋아요 점수만 표시
  const [stacked, setStacked] = useState(true);
  const [sortOrder, setSortOrder] = useState('');
  const [loading, setLoading] = useState(false);
  const [chartData, setChartData] = useState({
    labels: [],
    datasets: [
      {
        label: '퀴즈 점수',
        data: [],
        borderColor: 'rgba(54, 162, 235, 0.8)',
        borderWidth: 2,
        borderSkipped: false,
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
      },
      {
        label: '좋아요 개수',
        data: [],
        borderColor: 'rgba(255, 99, 132, 0.8)',
        borderWidth: 2,
        borderSkipped: false,
        backgroundColor: 'rgba(255, 99, 132, 0.5)',
      }
    ]
  });

  useEffect(() => {
    let isCancelled = false;
    const fetchContributionInfo = async () => {
      try {
        const response = await fetch(`/api/contribute/${organizationId}`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });
        if (response.ok && !isCancelled) {
          const data = await response.json();
          const fetchedContributionInfo = data.map(user => ({
            nickname: user.nickname,
            quizScore: user.quizScore,
            likeScore: user.likeScore,
          }));
          updateChartData(fetchedContributionInfo);
        } else {
          console.error(`${organizationId}의 기여도 정보를 불러오는데 실패했습니다. HTTP status ${response.status}`);
        }
      } catch (error) {
        if (!isCancelled) {
          console.error('Error fetching contribution:', error);
        } 
      } finally {
        if (!loading) setLoading(true);
      }
    };

    fetchContributionInfo();

    return () => {
      isCancelled = true;
    };
  }, [organizationId, displayTotalScores, displayQuizScores, displayLikeScores, sortOrder, loading]);

    const updateChartData = (contributions) => {
      if (!contributions || contributions.length === 0) return;
      let sortedContributions = [...contributions];

      // 정렬 로직
      if (sortOrder !== '') {
      sortedContributions.sort((a, b) => {
        const scoreA = displayQuizScores ? a.quizScore : displayLikeScores ? a.likeScore : a.quizScore + a.likeScore;
        const scoreB = displayQuizScores ? b.quizScore : displayLikeScores ? b.likeScore : b.quizScore + b.likeScore;
        return sortOrder === 'asc' ? scoreA - scoreB : scoreB - scoreA;
      });
      } else {
        const userIndex = sortedContributions.findIndex(user => user.nickname === nickname);
        const userContribution = sortedContributions.splice(userIndex, 1)[0];
        sortedContributions = [userContribution, ...sortedContributions];
      }

      const nicknames = sortedContributions.map(user => user.nickname === nickname ? `${user.nickname} (본인) ⭐️` : user.nickname);
      const quizData = sortedContributions.map(user => user.quizScore);
      const likeData = sortedContributions.map(user => user.likeScore);
  
      let datasets = [];
      if (displayQuizScores) {
        setStacked(false);
        datasets.push({
          label: '퀴즈 점수',
          data: quizData,
          borderColor: 'rgba(54, 162, 235, 0.8)',
          borderWidth: 2,
          borderSkipped: false,
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
        });
      }
      if (displayLikeScores) {
        setStacked(false);
        datasets.push({
          label: '좋아요 개수',
          data: likeData,
          borderColor: 'rgba(255, 99, 132, 0.8)',
          borderWidth: 2,
          borderSkipped: false,
          backgroundColor: 'rgba(255, 99, 132, 0.5)',
        });
      }
      
      if (displayLikeScores || displayQuizScores) {
        setChartData({
            labels: nicknames,
            datasets: datasets
          });
      } else {
        setStacked(true);
        setChartData({
            labels: nicknames,
            datasets: [
              {
                label: '퀴즈 점수',
                data: quizData,
                borderColor: 'rgba(54, 162, 235, 0.8)',
                borderWidth: 2,
                borderSkipped: false,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
              },
              {
                label: '좋아요 개수',
                data: likeData,
                borderColor: 'rgba(255, 99, 132, 0.8)',
                borderWidth: 2,
                borderSkipped: false,
                backgroundColor: 'rgba(255, 99, 132, 0.5)',
              }
            ]
          });
      }
    };

    const options = {
        responsive: true,
        plugins: {
          legend: { // 범례
            position: 'top',
            onClick: (e) => {}
          },
          animation: { // 차트 애니메이션
            duration: 0, // 사용 안 함
          },
          title: {
            display: true,
            text: '유저 별 기여도 점수',
            font: {
                size: 20, 
                family: 'Spoqa Han Sans Neo' 
              }
          }, 
          datalabels: { // datalables 플러그인 설정
            formatter: function (value, context) {
              if (value === 0) return '';
              else return value;
            },
            color: 'black', // 데이터 레이블 글자색
            font: {
              size: 17, 
              family: 'Spoqa Han Sans Neo',
            }
          },
        },
        scales: {
            x: {
                stacked: stacked,
                title: {
                    display: false,
                    text: '닉네임',
                    font: {
                        size: 13,
                        family: 'Spoqa Han Sans Neo'
                    }
                },
                ticks: {
                    font: {
                        size: 19,
                        family: 'Spoqa Han Sans Neo',
                        weight: '900'
                    },
                }
            },
          y: {
            stacked: stacked,
            title: {
              display: true,
              text: '획득 점수',
              font: {
                size: 13, 
                family: 'Spoqa Han Sans Neo' 
              }
            },
            ticks: {
              stepSize: 1,
              font: {
                size: 13, 
                family: 'Spoqa Han Sans Neo' 
              },
            }
          }
        }
    };

    const handleTotalScoresChange = () => {
        if (displayLikeScores) setDisplayLikeScores(!displayLikeScores);
        if (displayQuizScores) setDisplayQuizScores(!displayQuizScores);
        if (!displayLikeScores && !displayQuizScores) {
            setDisplayTotalScores(true);
        } else {
            setDisplayTotalScores(!displayTotalScores);
        }
      };

    const handleQuizScoresChange = () => {
        setDisplayQuizScores(!displayQuizScores);
        if (displayLikeScores && sortOrder !== '') setSortOrder('');
        if (displayTotalScores) setDisplayTotalScores(false);
        if (!displayLikeScores) setDisplayQuizScores(true);
      };
    
      const handleLikeScoresChange = () => {
        setDisplayLikeScores(!displayLikeScores);
        if (displayQuizScores && sortOrder !== '') setSortOrder('');
        if (displayTotalScores) setDisplayTotalScores(false);
        if (!displayQuizScores) setDisplayLikeScores(true);
      };
    
      const handleSortOrderChange = (order) => {
        setSortOrder(sortOrder === order ? '' : order);
        if (displayLikeScores && displayQuizScores) {
            setDisplayTotalScores(true);
            setDisplayLikeScores(false);
            setDisplayQuizScores(false);
        }
      };
      
      return (
        <div>
          <DataSelectionBtnContainer>
            <DataSelectionBtn onClick={() => { navigate(`/organization/${organizationId}`); }} style={{ borderRadius: "100%", padding: "5px", margin: "0px" }}>
              <HomeIcon>
                <FontAwesomeIcon icon={faHouse} />
              </HomeIcon>
            </DataSelectionBtn>
            <DataSelectionBtn $active={displayTotalScores} onClick={() => handleTotalScoresChange()}>
              <IconWrapper $active={displayTotalScores}>
                <FontAwesomeIcon icon={faIdCard} />              
              </IconWrapper>
              통합
            </DataSelectionBtn>
            <DataSelectionBtn $active={displayQuizScores} onClick={() => handleQuizScoresChange()}>
              <IconWrapper $active={displayQuizScores}>
                <FontAwesomeIcon icon={faCircleQuestion} />
              </IconWrapper>
              퀴즈 점수
            </DataSelectionBtn>
            <DataSelectionBtn $active={displayLikeScores} onClick={() => handleLikeScoresChange()}>
              <IconWrapper $active={displayLikeScores}>
                <FontAwesomeIcon icon={faHeartCircleCheck} />
              </IconWrapper>
              좋아요 개수
            </DataSelectionBtn>
          </DataSelectionBtnContainer>
          <SortBtnContainer>
            <SortBtn $active={sortOrder === 'asc'} onClick={() => handleSortOrderChange('asc')}>
              <IconWrapper $active={sortOrder === 'asc'}>
                <FontAwesomeIcon icon={faArrowUpWideShort} />
              </IconWrapper>
              오름차순
            </SortBtn>
            <SortBtn $active={sortOrder === 'desc'} onClick={() => handleSortOrderChange('desc')}>
              <IconWrapper $active={sortOrder === 'desc'}>
                <FontAwesomeIcon icon={faArrowDownWideShort} />
              </IconWrapper>
              내림차순
            </SortBtn>
          </SortBtnContainer>
          <Bar style={{ position: 'absolute' }} data={chartData} options={options} />
        </div>
      );
    }

    const DataSelectionBtnContainer = styled.div`
    display: flex;
    position: absolute;
    z-index: 10;
    width: auto;
    top: 18px;
    left: 70px; 
    gap: 15px;
  
    @media (max-width: 1600px) {
      left: 60px; 
      gap: 10px;
    }
  
    @media (max-width: 1200px) {
      left: 30px; 
      gap: 7px;
    }
  
    @media (max-width: 800px) {
      left: 20px; 
      gap: 4px;
    }
  `;
  
  const DataSelectionBtn = styled.button`
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: ${({ $active }) => ( $active ? '#3F51B5' : 'gray')};
    color: white;
    font-size: 19px;
    border: none;
    border-radius: 5px;
    padding: 8px 13px;
    padding-left: 10px;
    cursor: pointer;
  
    &:hover {
      background-color: #5C6BC0;
    }
  
    @media (max-width: 1600px) {
      font-size: 15px;
      padding: 7px 9px;
    }
  
    @media (max-width: 1200px) {
      font-size: 13px;
      padding: 6px 7px;
    }
  
    @media (max-width: 800px) {
      font-size: 11px;
      padding: 5px 5px;
    }
  `;

  const HomeIcon = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 15px;
  height: 15px;
  border-radius: 100%;
  background-color: white;
  color: #6A5ACD;
  font-size: 20px;
  padding: 10px;
  margin: 0px;

  @media (max-width: 1600px) {
    font-size: 16px;
    padding: 7px;
  }

  @media (max-width: 1200px) {
    font-size: 12px;
    padding: 4px;
  }

  @media (max-width: 800px) {
    font-size: 8px;
    padding: 1px;
  }
`;

const SortBtnContainer = styled.div`
  display: flex;
  position: absolute;
  z-index: 10;
  width: auto;
  top: 18px;
  right: 50px; 
  gap: 20px;

  @media (max-width: 1600px) {
    right: 35px; 
    gap: 15px;
  }

  @media (max-width: 1200px) {
    right: 25px; 
    gap: 10px;
  }

  @media (max-width: 800px) {
    right: 15px; 
    gap: 5px;
  }
`;

const SortBtn = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: ${({ $active }) => ( $active ? '#20B2AA' : 'gray')};
  color: white;
  font-size: 19px;
  border: none;
  border-radius: 25px;
  padding: 8px 15px;
  padding-left: 10px;
  cursor: pointer;

  &:hover {
    background-color: rgba(26, 142, 136, 0.9);
  }

  @media (max-width: 1600px) {
    font-size: 15px;
    padding: 7px 11px;
  }

  @media (max-width: 1200px) {
    font-size: 13px;
    padding: 6px 9px;
  }

  @media (max-width: 800px) {
    font-size: 11px;
    padding: 5px 7px;
  }
`;

const IconWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background-color: white;
  color: ${({ $active }) => ( $active ? '#616161' : 'gray')};
  font-size: 16px;
  padding: 8px;
  margin-right: 8px;

  @media (max-width: 1600px) {
    width: 17px;
    height: 18px;
    padding: 6px;
    margin-right: 6px;
    font-size: 13px;
  }

  @media (max-width: 1200px) {
    width: 15px;
    height: 16px;
    padding: 5px;
    margin-right: 5px;
    font-size: 12px;
  }

  @media (max-width: 1000px) {
    display: none;
  }
`;


export default ContributionPage;
