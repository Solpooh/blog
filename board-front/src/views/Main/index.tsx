import React, {useEffect, useState} from 'react';
import './style.css';
import Top3Item from 'components/Top3Item';
import {BoardListItem} from 'types/interface';
import BoardItem from 'components/BoardItem';
import {useNavigate} from 'react-router-dom';
import {SEARCH_PATH} from '../../constants';
import {
    getLatestBoardListRequest,
    getPopularListRequest,
    getTop3BoardListRequest
} from 'apis';
import {GetLatestBoardListResponseDto, GetTop3BoardListResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {GetPopularListResponseDto} from 'apis/response/search';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';

//  component: 메인 화면 컴포넌트 //
export default function Main() {

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();

    //  component: 메인 화면 상단 컴포넌트 //
    const MainTop = () => {

        //  state: 주간 top3 게시물 리스트 상태 //
        const [top3BoardList, setTop3BoardList] = useState<BoardListItem[]>([]);

        //  function: get top 3 board list response 처리 함수 //
        const getTop3BoardListResponse = (responseBody: GetTop3BoardListResponseDto | ResponseDto | null) => {
            if (!responseBody) return;
            const { code } = responseBody;
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code !== 'SU') return;

            const { top3List } = responseBody as GetTop3BoardListResponseDto;

            setTop3BoardList(top3List);
        };

        //  effect: 첫 마운트 시 실행될 함수 //
        useEffect(() => {
            getTop3BoardListRequest().then(getTop3BoardListResponse);
        }, []);


        //  render: 메인 화면 상단 컴포넌트 렌더링 //
        return (
            <div id='main-top-wrapper'>
                <div className='main-top-container'>
                    <div className='main-top-intro1'>{'평범한 개발자들의 소통의 장'}</div>
                    <div className='main-top-intro2'>{'DevHub'}</div>
                    <div className='main-top-contents-box'>
                        <div className='main-top-contents-title'>{'주간 TOP 3 게시글'}</div>
                        <div className='main-top-contents'>
                            {top3BoardList.map(top3ListItem => <Top3Item key={top3ListItem.boardNumber} top3ListItem={top3ListItem} />)}
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    //  component: 메인 화면 하단 컴포넌트 //
    const MainBottom = () => {
        //  state: 카테고리 상태  //
        const [categories, setCategories] = useState<{ name: string; count: number }[]>([]);
        //  state: 선택한 카테고리 상태  //
        const [selectedCategory, setSelectedCategory] = useState<string>('All');

        //  state: 페이지네이션 상태 //
        const [pagination, setPagination] = useState<Pagination<BoardListItem> | null>(null)
        //  state: 현재 페이지 상태 //
        const [currentPage, setCurrentPage] = useState<number>(1);
        //  state: 최신글 리스트 상태  //
        const [latestBoardList, setLatestBoardList] = useState<BoardListItem[]>([]);
        //  state: 인기 검색어 리스트 상태  //
        const [popularWordList, setPopularWordList] = useState<string[]>([]);

        //  function: get latest board list response 처리 함수 //
        const getLatestBoardListResponse = (responseBody: GetLatestBoardListResponseDto | ResponseDto | null,
                                            categoryName: string) => {
            if (!responseBody) return;
            const { code } = responseBody;
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code !== 'SU') return;

            const { pagination, categoryCounts } = responseBody as GetLatestBoardListResponseDto;
            if (categoryName === 'All' && categoryCounts) {
                const updatedCategories = categoryCounts.map(({ name, count }) => ({name, count}));

                setCategories([
                    { name: 'All', count: pagination.totalElements},
                    ...updatedCategories
                ]);
            }
            setLatestBoardList(pagination.content);
            setPagination(pagination);
        };

        //  function: get popular list response 처리 함수 //
        const getPopularListResponse = (responseBody: GetPopularListResponseDto | ResponseDto | null) => {
            if (!responseBody) return;
            const { code } = responseBody;
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code !== 'SU') return;

            const { popularWordList } = responseBody as GetPopularListResponseDto;
            setPopularWordList(popularWordList);
        }

        //  event handler: 카테고리 클릭 이벤트 처리  //
        const onCategoryClickHandler = (categoryName: string | null) => {
            const targetCategory = categoryName || 'All';
            setSelectedCategory(targetCategory);
            // 카테고리 선택 시 currentPage를 무조건 1페이지로 초기화
            setCurrentPage(1);
            getLatestBoardListRequest(categoryName, currentPage - 1).then((responseBody) =>
                getLatestBoardListResponse(responseBody, targetCategory)
            );
        };
        //  event handler: 인기 검색어 클릭 이벤트 처리  //
        const onPopularWordClickHandler = (word: string) => {
            navigate(SEARCH_PATH(word));
        }

        //  effect: selectedCategory 또는 currentPage가 바뀔 때마다 실행될 함수 //
        useEffect(() => {
            getLatestBoardListRequest(selectedCategory, currentPage - 1).then((responseBody) =>
                getLatestBoardListResponse(responseBody, selectedCategory)
            );
            getPopularListRequest().then(getPopularListResponse);
        }, [selectedCategory, currentPage]);
        
        //  render: 메인 화면 하단 컴포넌트 렌더링 //
        return (
            <div id="main-bottom-wrapper">
                <div className="main-bottom-container">
                    <div className="main-bottom-flex-box">
                        {/* 카테고리 박스 */}
                        <div className="main-bottom-category-popular-box">
                            <div className="main-bottom-category-box">
                                {categories.map((category) => (
                                    <div
                                        className={`category-item ${selectedCategory === category.name ? 'selected' : ''}`}
                                        key={category.name} onClick={() => onCategoryClickHandler(category.name)}>
                                        {`${category.name} (${category.count})`}
                                    </div>
                                ))}
                            </div>

                            {/* 인기 검색어 박스 */}
                            <div className="main-bottom-popular-box">
                                <div className="main-bottom-popular-card-title">{'인기 검색어'}</div>
                                <div className="main-bottom-popular-card-contents">
                                    {popularWordList.map((word) => (
                                        <div className="word-badge" key={word}
                                             onClick={() => onPopularWordClickHandler(word)}>
                                            {word}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* 현재 컨텐츠 */}
                        <div className="main-bottom-current-contents">
                            {latestBoardList.map((boardListItem, index) => (
                                <div key={boardListItem.boardNumber} className="board-item">
                                    <BoardItem boardListItem={boardListItem}/>
                                    {index !== latestBoardList.length - 1 && (
                                        <div className="divider"></div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Pagination */}
                {pagination && (
                    <div className="main-bottom-pagination-box">
                        <Paging
                            currentPage={currentPage}
                            totalPages={pagination.totalPages}
                            onPageChange={setCurrentPage}
                        />
                    </div>
                )}
            </div>
        );
    }


    //  render: 메인 화면 컴포넌트 렌더링 //
    return (
        <>
            <MainTop />
            <MainBottom />
        </>
    )
};