import React, {useCallback, useEffect, useMemo, useState} from 'react';
import './style.css';
import Top3Item from 'components/Top3Item';
import {BoardListItem} from 'types/interface';
import BoardItem from 'components/BoardItem';
import {useNavigate, useParams, useSearchParams} from 'react-router-dom';
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
            const { code, data} = responseBody;
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code !== 'SU') return;

            const {top3List} = (responseBody as GetTop3BoardListResponseDto).data;

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
                    <p className='main-top-intro1'>{'평범한 개발자들의 소통의 장'}</p>
                    <h1 className='main-top-intro2'>{'DevHub'}</h1>
                    <div className='main-top-contents-box'>
                        <h2 className='main-top-contents-title'>{'주간 TOP 3 게시글'}</h2>
                        <div className='main-top-contents'>
                            {top3BoardList.map(top3ListItem => <Top3Item key={top3ListItem.boardNumber} top3ListItem={top3ListItem}/>)}
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    //  component: 메인 화면 하단 컴포넌트 //
    const MainBottom = () => {
        const {category = 'All', page = '1'} = useParams();
        const [searchParams] = useSearchParams();
        const pageParam = parseInt(searchParams.get('page') || '1');

        //  state: 카테고리 상태  //
        const [categories, setCategories] = useState<{ name: string; count: number }[]>([]);
        //  state: 선택한 카테고리 상태  //
        const [selectedCategory, setSelectedCategory] = useState<string>(category);

        const [totalBoardCount, setTotalBoardCount] = useState<number>(0);

        //  state: 페이지네이션 상태 //
        const [pagination, setPagination] = useState<Pagination<BoardListItem> | null>(null)
        //  state: 현재 페이지 상태 //
        const [currentPage, setCurrentPage] = useState<number>(pageParam);
        //  state: 최신글 리스트 상태  //
        const [latestBoardList, setLatestBoardList] = useState<BoardListItem[]>([]);
        //  state: 인기 검색어 리스트 상태  //
        const [popularWordList, setPopularWordList] = useState<string[]>([]);
        //  state: 로딩 상태  //
        const [isLoading, setIsLoading] = useState<boolean>(true);

        //  function: get latest board list response 처리 함수 //
        const getLatestBoardListResponse = (responseBody: GetLatestBoardListResponseDto | null,
                                            categoryName: string) => {
            if (!responseBody) {
                setIsLoading(false);
                alert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
                return;
            }
            const { code, data } = responseBody;
            if (code === 'DBE') {
                setIsLoading(false);
                alert('일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.');
                console.error('Database error while fetching board list');
                return;
            }
            if (code !== 'SU') {
                setIsLoading(false);
                return;
            }

            const { boardList, categoryList } = (responseBody as GetLatestBoardListResponseDto).data;

            // ✅ All일 때는 응답의 totalElements를 직접 사용
            const allCount = categoryName === 'All' ? boardList.totalElements : totalBoardCount;

            // ✅ state 업데이트 (다음 렌더링을 위해)
            if (categoryName === 'All') {
                setTotalBoardCount(boardList.totalElements);
            }

            // ✅ 'All' 카테고리는 현재 응답의 totalElements 사용
            const allCategory = { name: 'All', count: allCount };

            // ✅ 서버 응답에서 나머지 카테고리
            const otherCategories = categoryList
                .filter(({ name }) => name !== 'All')
                .map(({ name, count }) => ({ name, count }));

            setCategories([allCategory, ...otherCategories]);
            setLatestBoardList(boardList.content);
            setPagination(boardList);
            setIsLoading(false);
        };

        //  function: get popular list response 처리 함수 //
        const getPopularListResponse = (responseBody: GetPopularListResponseDto | ResponseDto | null) => {
            if (!responseBody) {
                console.error('Failed to fetch popular word list');
                return;
            }
            const {code, data} = responseBody;
            if (code === 'DBE') {
                console.error('Database error while fetching popular word list');
                return;
            }
            if (code !== 'SU') return;

            const {popularWordList} = (responseBody as GetPopularListResponseDto).data;
            setPopularWordList(popularWordList);
        }

        //  event handler: 카테고리 클릭 이벤트 처리  //
        const onCategoryClickHandler = useCallback((categoryName: string) => {
            navigate(`/${categoryName}?page=1`);
        }, [navigate]);

        //  event handler: 인기 검색어 클릭 이벤트 처리  //
        const onPopularWordClickHandler = useCallback((word: string) => {
            navigate(SEARCH_PATH(word));
        }, [navigate]);

        //  effect: URL이 바뀔 때마다 카테고리/페이지 설정
        useEffect(() => {
            setSelectedCategory(category);
            setCurrentPage(pageParam);
        }, [category, pageParam]);

        //  effect: 최초 데이터 요청 //
        useEffect(() => {
            setIsLoading(true);
            getLatestBoardListRequest(selectedCategory, currentPage - 1).then((responseBody) =>
                getLatestBoardListResponse(responseBody, selectedCategory)
            );
            getPopularListRequest().then(getPopularListResponse);
        }, [selectedCategory, currentPage]);

        // 페이지 변경
        const onPageChange = useCallback((page: number) => {
            navigate(`/${selectedCategory}?page=${page}`);
        }, [navigate, selectedCategory]);

        //  render: 메인 화면 하단 컴포넌트 렌더링 //
        return (
            <div id="main-bottom-wrapper">
                <div className="main-bottom-container">
                    <div className="main-bottom-flex-box">
                        {/* 카테고리 박스 */}
                        <aside className="main-bottom-category-popular-box">
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
                                <h3 className="main-bottom-popular-card-title">{'인기 검색어'}</h3>
                                <div className="main-bottom-popular-card-contents">
                                    {popularWordList.map((word) => (
                                        <div className="word-badge" key={word}
                                             onClick={() => onPopularWordClickHandler(word)}>
                                            {word}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </aside>

                        {/* 현재 컨텐츠 */}
                        <section className="main-bottom-current-contents">
                            {isLoading ? (
                                // 로딩 중일 때 Skeleton UI 표시
                                <>
                                    {[1, 2, 3, 4, 5].map((i) => (
                                        <div key={i} className="board-item">
                                            <div style={{ padding: '20px' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '12px' }}>
                                                    <div className="skeleton skeleton-avatar" style={{ marginRight: '12px' }}></div>
                                                    <div style={{ flex: 1 }}>
                                                        <div className="skeleton skeleton-text" style={{ width: '120px', marginBottom: '4px' }}></div>
                                                        <div className="skeleton skeleton-text" style={{ width: '80px' }}></div>
                                                    </div>
                                                </div>
                                                <div className="skeleton skeleton-title"></div>
                                                <div className="skeleton skeleton-text"></div>
                                                <div className="skeleton skeleton-text" style={{ width: '90%' }}></div>
                                                <div className="skeleton skeleton-text" style={{ width: '150px', marginTop: '12px' }}></div>
                                            </div>
                                            {i !== 5 && <div className="divider"></div>}
                                        </div>
                                    ))}
                                </>
                            ) : latestBoardList.length === 0 ? (
                                // 데이터가 없을 때 Empty State 표시
                                <div className="empty-state">
                                    <div className="empty-state-icon">📝</div>
                                    <div className="empty-state-title">게시글이 없습니다</div>
                                    <div className="empty-state-description">
                                        첫 번째 게시글을 작성해보세요!
                                    </div>
                                </div>
                            ) : (
                                // 정상적으로 데이터 표시
                                latestBoardList.map((boardListItem, index) => (
                                    <div key={boardListItem.boardNumber} className="board-item">
                                        <BoardItem boardListItem={boardListItem}/>
                                        {index !== latestBoardList.length - 1 && (
                                            <div className="divider"></div>
                                        )}
                                    </div>
                                ))
                            )}
                        </section>
                    </div>
                </div>

                {/* Pagination */}
                {pagination && (
                    <div className="main-bottom-pagination-box">
                        <Paging
                            pagination={pagination}
                            onPageChange={onPageChange}
                        />
                    </div>
                )}
            </div>
        );
    }


    //  render: 메인 화면 컴포넌트 렌더링 //
    return (
        <>
            <MainTop/>
            <MainBottom/>
        </>
    )
};