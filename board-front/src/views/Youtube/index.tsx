//  component: 유튜브 메인 화면 컴포넌트  //
import {VideoListItem} from 'types/interface';
import React, {ChangeEvent, KeyboardEvent, useCallback, useEffect, useRef, useState} from 'react';
import VideoItem from 'components/VideoItem';
import {getSearchVideoListRequest, getVideoListRequest, getCategoryVideoListRequest, getSubCategoryVideoListRequest, getCategorySearchRequest, getSubCategorySearchRequest} from 'apis';
import {GetVideoListResponseDto, GetSearchVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import './style.css';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {YOUTUBE_SEARCH_PATH, YOUTUBE_PATH, YOUTUBE_CATEGORY_PATH, YOUTUBE_CATEGORY_SEARCH_PATH, YOUTUBE_SUBCATEGORY_SEARCH_PATH} from "../../constants";
import SearchAutocomplete from 'components/SearchAutocomplete';
import SortDropdown, { SortType } from 'components/SortDropdown';

//  component: Youtube 컴포넌트  //
export default function Youtube() {
    //  state: path variable 상태 //
    const { searchWord, mainCategory, subCategory } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();

    const pageParam = Number(searchParams.get("page")) || 1;
    const sortParam = (searchParams.get("sort") as SortType) || null;
    const [currentPage, setCurrentPage] = useState(pageParam);

    //  state: 유튜브 최신 비디오 리스트 상태  //
    const [videoList, setVideoList] = useState<VideoListItem[]>([]);
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<VideoListItem> | null>(null)
    //  state: 총 채널 수 상태 //
    const [totalChannelCount, setTotalChannelCount] = useState<number | null>(null)

    //  state: 검색어 저장 상태 //
    const [word, setWord] = useState<string>('');
    //  state: 로딩 상태 //
    const [isLoading, setIsLoading] = useState<boolean>(true);

    //  state: 정렬 옵션 상태 //
    const isSearchMode = !!searchWord;
    const defaultSort: SortType = isSearchMode ? 'RELEVANCE' : 'LATEST';
    const [sortType, setSortType] = useState<SortType>(sortParam || defaultSort);

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();
    //  function: Latest videoList response 처리 함수 //
    const getVideoListResponse = (responseBody: GetVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) {
            setIsLoading(false);
            alert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
            return;
        }
        const { code } = responseBody;
        if (code === 'DBE') {
            setIsLoading(false);
            alert('일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.');
            console.error('Database error while fetching video list');
            return;
        }
        if (code !== 'SU') {
            setIsLoading(false);
            return;
        }

        const { videoList, totalChannelCount } = (responseBody as GetVideoListResponseDto).data;
        setVideoList(videoList.content);
        setPagination(videoList);
        setTotalChannelCount(totalChannelCount);
        setIsLoading(false);
    }
    //  function: Search videoList response 처리 함수 //
    const getSearchVideoListResponse = (responseBody: GetSearchVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) {
            setIsLoading(false);
            alert('검색 중 오류가 발생했습니다.\n다시 시도해주세요.');
            return;
        }
        const {code} = responseBody;
        if (code === 'DBE') {
            setIsLoading(false);
            alert('일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.');
            console.error('Database error while searching videos');
            return;
        }
        if (code !== 'SU') {
            setIsLoading(false);
            return;
        }

        const {videoList} = (responseBody as GetSearchVideoListResponseDto).data;
        setVideoList(videoList.content);
        setPagination(videoList);
        setIsLoading(false);
    }

    //  event handler: 검색어 변경 이벤트 처리 함수 //
    const onSearchWordChange = useCallback((value: string) => {
        setWord(value);
    }, []);

    //  event handler: 검색 실행 처리 함수 //
    const onSearch = useCallback((value: string) => {
        if (!value.trim()) {
            alert('검색어를 입력해주세요.');
            return;
        }
        // 카테고리 컨텍스트가 있으면 카테고리 내 검색
        if (mainCategory && subCategory) {
            navigate(YOUTUBE_SUBCATEGORY_SEARCH_PATH(mainCategory, subCategory, value));
        } else if (mainCategory) {
            navigate(YOUTUBE_CATEGORY_SEARCH_PATH(mainCategory, value));
        } else {
            navigate(YOUTUBE_SEARCH_PATH(value));
        }
    }, [navigate, mainCategory, subCategory]);

    //  event handler: 페이지 변경 함수 //
    const onPageChange = useCallback((page: number) => {
        const newParams: Record<string, string> = { page: String(page) };
        if (sortType !== defaultSort) {
            newParams.sort = sortType;
        }
        setSearchParams(newParams);
        setCurrentPage(page);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [setSearchParams, sortType, defaultSort]);

    //  event handler: 정렬 변경 함수 //
    const onSortChange = useCallback((newSort: SortType) => {
        setSortType(newSort);
        const newParams: Record<string, string> = { page: '1' };
        if (newSort !== defaultSort) {
            newParams.sort = newSort;
        }
        setSearchParams(newParams);
        setCurrentPage(1);
    }, [setSearchParams, defaultSort]);

    //  effect: page/sort param 변경될 때마다 적용 //
    useEffect(() => {
        const page = Number(searchParams.get("page")) || 1;
        const sort = searchParams.get("sort") as SortType;
        setCurrentPage(page);
        if (sort) {
            setSortType(sort);
        }
    }, [searchParams]);

    //  effect: 검색 모드 변경 시 정렬 초기화 //
    useEffect(() => {
        setSortType(isSearchMode ? 'RELEVANCE' : 'LATEST');
    }, [isSearchMode]);

    //  effect: 데이터 로드 //
    useEffect(() => {
        setIsLoading(true);

        if (mainCategory && subCategory && searchWord) {
            // 소분류 카테고리 내 검색
            setWord(searchWord);
            getSubCategorySearchRequest(mainCategory, subCategory, searchWord, currentPage - 1, sortType)
                .then(getSearchVideoListResponse);
        } else if (mainCategory && searchWord) {
            // 대분류 카테고리 내 검색
            setWord(searchWord);
            getCategorySearchRequest(mainCategory, searchWord, currentPage - 1, sortType)
                .then(getSearchVideoListResponse);
        } else if (mainCategory && subCategory) {
            // 소분류 카테고리 조회
            getSubCategoryVideoListRequest(mainCategory, subCategory, currentPage - 1, sortType)
                .then(getVideoListResponse);
        } else if (mainCategory) {
            // 대분류 카테고리 조회
            getCategoryVideoListRequest(mainCategory, currentPage - 1, sortType)
                .then(getVideoListResponse);
        } else if (searchWord) {
            // 일반 검색 모드
            setWord(searchWord);
            getSearchVideoListRequest(searchWord, currentPage - 1, sortType)
                .then(getSearchVideoListResponse);
        } else {
            // 메인 페이지 (최신 영상)
            getVideoListRequest(currentPage - 1, sortType)
                .then(getVideoListResponse);
        }
    }, [currentPage, searchWord, mainCategory, subCategory, sortType]);

    //  function: 페이지 제목 생성 //
    const getPageTitle = () => {
        if (mainCategory && subCategory) {
            return `${subCategory.replace(/_/g, ' ')} 영상`;
        }
        if (mainCategory) {
            return `${mainCategory.replace(/_/g, ' ')} 영상`;
        }
        return '개발 트렌드 영상 플랫폼';
    };

    //  function: 페이지 설명 생성 //
    const getPageDescription = () => {
        if (mainCategory) {
            return `${mainCategory.replace(/_/g, ' ')} 관련 최신 개발 영상을 확인하세요.`;
        }
        return '개발 트렌드와 최신 기술 영상을 불필요한 노이즈 없이 빠르게 탐색하고 시청하세요!';
    };

    return (
        <div className="youtube-wrapper">
            <header className="youtube-hero">
                <div className="hero-content">
                    <h1 className="hero-title">{getPageTitle()}</h1>
                    <p className="hero-description">{getPageDescription()}</p>

                    {/* 검색/카테고리 상태에 따라 다른 UI 표시 */}
                    {searchWord ? (
                        <div className="search-result-info">
                            <p className="search-result-text">
                                {mainCategory && (
                                    <span className="search-category">[{mainCategory.replace(/_/g, ' ')}] </span>
                                )}
                                <span className="search-keyword">"{searchWord}"</span>로 검색된 영상
                                <span className="search-count"> {pagination ? pagination.totalElements.toLocaleString() : '0'}개</span>
                            </p>
                            <button className="view-all-btn" onClick={() => navigate(mainCategory ? `/youtube/category/${mainCategory}` : YOUTUBE_PATH())}>
                                {mainCategory ? '카테고리 전체 보기' : '전체 목록 보기'}
                            </button>
                        </div>
                    ) : mainCategory ? (
                        <div className="category-result-info">
                            <p className="category-result-text">
                                <span className="category-count">{pagination ? pagination.totalElements.toLocaleString() : '0'}개</span>의 영상
                            </p>
                            <button className="view-all-btn" onClick={() => navigate(YOUTUBE_PATH())}>
                                전체 목록 보기
                            </button>
                        </div>
                    ) : (
                        <div className="hero-stats">
                            <div className="stat-badge">
                                <span className="stat-number">{pagination ? pagination.totalElements.toLocaleString() : '...'}</span>
                                <span className="stat-label">개발 영상</span>
                            </div>
                            <div className="stat-badge">
                                <span className="stat-number">{totalChannelCount ? totalChannelCount.toLocaleString() : '...'}</span>
                                <span className="stat-label">채널</span>
                            </div>
                        </div>
                    )}

                    <div className="hero-search-box">
                        <SearchAutocomplete
                            value={word}
                            onChange={onSearchWordChange}
                            onSearch={onSearch}
                            placeholder="영상 키워드로 검색하세요. 가장 관련성이 높은 결과를 제공합니다."
                        />
                    </div>
                </div>
            </header>

            {/* 정렬 옵션 */}
            <div className="video-controls">
                <div className="video-controls-left">
                    <span className="video-count">
                        {pagination ? `${pagination.totalElements.toLocaleString()}개의 영상` : ''}
                    </span>
                </div>
                <div className="video-controls-right">
                    <SortDropdown
                        value={sortType}
                        onChange={onSortChange}
                        includeRelevance={isSearchMode}
                    />
                </div>
            </div>

            <section className="video-grid">
                {isLoading ? (
                    // 로딩 중일 때 Skeleton UI 표시
                    <>
                        {[1, 2, 3, 4, 5, 6].map((i) => (
                            <div key={i} style={{ padding: '16px', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                                <div className="skeleton skeleton-avatar" style={{ width: '48px', height: '48px', marginBottom: '12px' }}></div>
                                <div className="skeleton skeleton-image" style={{ height: '180px', marginBottom: '12px' }}></div>
                                <div className="skeleton skeleton-title" style={{ width: '100%' }}></div>
                                <div className="skeleton skeleton-text"></div>
                                <div className="skeleton skeleton-text" style={{ width: '70%' }}></div>
                            </div>
                        ))}
                    </>
                ) : videoList.length === 0 ? (
                    // 데이터가 없을 때 Empty State 표시
                    <div className="empty-state" style={{ gridColumn: '1 / -1' }}>
                        <div className="empty-state-icon">📹</div>
                        <div className="empty-state-title">영상이 없습니다</div>
                        <div className="empty-state-description">
                            검색 결과가 없거나 아직 등록된 영상이 없습니다.
                        </div>
                    </div>
                ) : (
                    // 정상적으로 데이터 표시
                    videoList.map(videoItem => <VideoItem key={videoItem.videoId} videoItem={videoItem} />)
                )}
            </section>

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