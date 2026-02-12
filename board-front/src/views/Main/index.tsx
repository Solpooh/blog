import React, {useCallback, useEffect, useMemo, useState} from 'react';
import './style.css';
import Top3Item from 'components/Top3Item';
import {BoardListItem} from 'types/interface';
import BoardItem from 'components/BoardItem';
import {useNavigate, useParams, useSearchParams} from 'react-router-dom';
import {BOARD_PATH, BOARD_WRITE_PATH, SEARCH_PATH} from '../../constants';
import {useLoginUserStore} from 'stores';
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
                    <div className='section-header'>
                        <h2 className='section-title'>
                            <span className='highlight'>TOP 3 게시글</span>
                        </h2>
                        <p className='section-description'>
                            주간동안 가장 반응이 뜨거운 게시글입니다.
                        </p>
                    </div>
                    <div className='main-top-contents'>
                        {top3BoardList.map(top3ListItem => <Top3Item key={top3ListItem.boardNumber} top3ListItem={top3ListItem}/>)}
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
        //  state: 로그인 유저 상태 //
        const {loginUser} = useLoginUserStore();

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

        //  event handler: 글쓰기 버튼 클릭 이벤트 처리  //
        const onWriteButtonClickHandler = useCallback(() => {
            navigate(BOARD_PATH() + '/' + BOARD_WRITE_PATH());
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
                    {/* Section Header */}
                    <div className='section-header'>
                        <h2 className='section-title'>
                            최신 <span className='highlight' style={{ background: 'linear-gradient(135deg, #06b6d4 0%, #10b981 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>게시글</span>
                        </h2>
                        <div className='section-description-row'>
                            <p className='section-description'>
                                전체 게시글과 카테고리 별 게시글입니다.
                            </p>
                            {loginUser && (
                                <button className='write-button' onClick={onWriteButtonClickHandler}>
                                    <div className='icon edit-icon'></div>
                                    <span>글쓰기</span>
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Category Pills */}
                    <div className="category-pills-wrapper">
                        <div className="category-pills">
                            {categories.map((category) => (
                                <button
                                    className={`category-pill ${selectedCategory === category.name ? 'selected' : ''}`}
                                    key={category.name}
                                    onClick={() => onCategoryClickHandler(category.name)}
                                >
                                    {category.name}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Board Grid */}
                    <section className="main-bottom-current-contents">
                        {isLoading ? (
                            // 로딩 중일 때 Skeleton UI 표시
                            <>
                                {[1, 2, 3].map((i) => (
                                    <div key={i} className="board-item skeleton">
                                        <div style={{ padding: '28px' }}>
                                            <div className="skeleton skeleton-text" style={{ width: '80px', marginBottom: '16px' }}></div>
                                            <div className="skeleton skeleton-title" style={{ marginBottom: '12px' }}></div>
                                            <div className="skeleton skeleton-text"></div>
                                            <div className="skeleton skeleton-text" style={{ width: '90%' }}></div>
                                            <div className="skeleton skeleton-text" style={{ width: '70%' }}></div>
                                        </div>
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
                            latestBoardList.map((boardListItem) => (
                                <BoardItem key={boardListItem.boardNumber} boardListItem={boardListItem}/>
                            ))
                        )}
                    </section>

                    {/* Pagination */}
                    {pagination && (
                        <div className="main-bottom-pagination-box">
                            <Paging
                                pagination={pagination}
                                onPageChange={onPageChange}
                            />
                        </div>
                    )}

                    {/* Popular Search Section */}
                    <section className="popular-search-section">
                        <div className="popular-search-header">
                            <h3 className="popular-search-title">🔥 인기 검색어</h3>
                            <p className="popular-search-description">지금 가장 많이 검색되는 키워드</p>
                        </div>
                        <div className="popular-search-grid">
                            {popularWordList.map((word, index) => (
                                <div
                                    key={word}
                                    className="popular-word-item"
                                    onClick={() => onPopularWordClickHandler(word)}
                                >
                                    <span className="popular-word-rank">{index + 1}</span>
                                    <span className="popular-word-text">{word}</span>
                                </div>
                            ))}
                        </div>
                    </section>
                </div>
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