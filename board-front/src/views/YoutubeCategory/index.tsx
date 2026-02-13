//  component: 유튜브 카테고리별 영상 목록 컴포넌트  //
import {VideoListItem} from 'types/interface';
import React, {useCallback, useEffect, useRef, useState} from 'react';
import VideoItem from 'components/VideoItem';
import {getCategoryVideoListRequest, getSubCategoryVideoListRequest, getCategorySearchRequest, getSubCategorySearchRequest} from 'apis';
import {GetVideoListResponseDto, GetSearchVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import './style.css';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {YOUTUBE_CATEGORY_SEARCH_PATH, YOUTUBE_SUBCATEGORY_SEARCH_PATH, YOUTUBE_PATH} from "../../constants";
import SearchAutocomplete from 'components/SearchAutocomplete';
import SortDropdown, { SortType } from 'components/SortDropdown';
import { Search, ChevronRight, Layers, Video } from 'lucide-react';

//  component: YoutubeCategory 컴포넌트  //
export default function YoutubeCategory() {
    //  ref: 비디오 섹션 스크롤 타겟 //
    const videoSectionRef = useRef<HTMLDivElement>(null);
    //  state: path variable 상태 //
    const { searchWord, mainCategory, subCategory } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();

    const pageParam = Number(searchParams.get("page")) || 1;
    const sortParam = (searchParams.get("sort") as SortType) || null;
    const [currentPage, setCurrentPage] = useState(pageParam);

    //  state: 비디오 리스트 상태  //
    const [videoList, setVideoList] = useState<VideoListItem[]>([]);
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<VideoListItem> | null>(null);

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

    //  function: videoList response 처리 함수 //
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
            return;
        }
        if (code !== 'SU') {
            setIsLoading(false);
            return;
        }

        const { videoList } = (responseBody as GetVideoListResponseDto).data;
        setVideoList(videoList.content);
        setPagination(videoList);
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
        if (mainCategory && subCategory) {
            navigate(YOUTUBE_SUBCATEGORY_SEARCH_PATH(mainCategory, subCategory, value));
        } else if (mainCategory) {
            navigate(YOUTUBE_CATEGORY_SEARCH_PATH(mainCategory, value));
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
        videoSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
        if (!mainCategory) return;
        setIsLoading(true);

        if (searchWord || currentPage > 1) {
            videoSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        if (subCategory && searchWord) {
            setWord(searchWord);
            getSubCategorySearchRequest(mainCategory, subCategory, searchWord, currentPage - 1, sortType)
                .then(getSearchVideoListResponse);
        } else if (searchWord) {
            setWord(searchWord);
            getCategorySearchRequest(mainCategory, searchWord, currentPage - 1, sortType)
                .then(getSearchVideoListResponse);
        } else if (subCategory) {
            getSubCategoryVideoListRequest(mainCategory, subCategory, currentPage - 1, sortType)
                .then(getVideoListResponse);
        } else {
            getCategoryVideoListRequest(mainCategory, currentPage - 1, sortType)
                .then(getVideoListResponse);
        }
    }, [currentPage, searchWord, mainCategory, subCategory, sortType]);

    //  render: 카테고리 표시 이름 //
    const categoryDisplayName = subCategory || mainCategory || '';

    return (
        <div className="youtube-category-wrapper">
            {/* ===== Section 1: Category Banner ===== */}
            <section className="category-banner">
                <div className="category-banner-inner">
                    <div className="category-banner-content">
                        <div className="category-breadcrumb">
                            <Layers size={16} className="category-breadcrumb-icon" />
                            <span className="category-breadcrumb-item" onClick={() => navigate(YOUTUBE_PATH())}>DevTube</span>
                            <ChevronRight size={14} className="category-breadcrumb-sep" />
                            {subCategory ? (
                                <>
                                    <span className="category-breadcrumb-item"
                                          onClick={() => mainCategory && navigate(`/youtube/category/${mainCategory}`)}>
                                        {mainCategory}
                                    </span>
                                    <ChevronRight size={14} className="category-breadcrumb-sep" />
                                    <span className="category-breadcrumb-current">{subCategory}</span>
                                </>
                            ) : (
                                <span className="category-breadcrumb-current">{mainCategory}</span>
                            )}
                        </div>
                        <h1 className="category-banner-title">{categoryDisplayName}</h1>
                        <p className="category-banner-desc">
                            <strong>{categoryDisplayName}</strong> 관련 개발 영상을 탐색하세요
                        </p>
                        <div className="category-banner-stats">
                            <div className="category-stat-badge">
                                <Video size={16} />
                                <span>{pagination ? pagination.totalElements.toLocaleString() : '...'}개의 영상</span>
                            </div>
                        </div>
                    </div>
                    <div className="category-banner-decoration">
                        <div className="category-deco-circle category-deco-1"></div>
                        <div className="category-deco-circle category-deco-2"></div>
                        <div className="category-deco-circle category-deco-3"></div>
                    </div>
                </div>
            </section>

            {/* ===== Section 2: Search + Video List ===== */}
            <div className="category-videos" ref={videoSectionRef}>
                {/* Search Area */}
                <div className="videos-search-area">
                    <div className="videos-search-inner">
                        <h2 className="videos-search-title">Search Videos</h2>
                        <div className="videos-search-row">
                            <div className="videos-search-box">
                                <Search size={20} className="videos-search-icon" />
                                <SearchAutocomplete
                                    value={word}
                                    onChange={onSearchWordChange}
                                    onSearch={onSearch}
                                    placeholder={`${categoryDisplayName} 카테고리에서 검색...`}
                                />
                            </div>
                            <button className="videos-view-all-btn" onClick={() => navigate(YOUTUBE_PATH())}>
                                전체 목록 보기
                            </button>
                        </div>
                        {searchWord && (
                            <div className="videos-search-result">
                                <span className="videos-search-keyword">"{searchWord}"</span>
                                <span className="videos-search-result-count">
                                    {pagination ? pagination.totalElements.toLocaleString() : '0'}건
                                </span>
                            </div>
                        )}
                    </div>
                </div>

                {/* Section Header + Controls */}
                <div className="videos-header">
                    <div className="videos-header-left">
                        <span className="videos-section-count">
                            {pagination ? pagination.totalElements.toLocaleString() : '0'}개의 영상
                        </span>
                    </div>
                    <div className="videos-header-right">
                        <SortDropdown
                            value={sortType}
                            onChange={onSortChange}
                            includeRelevance={isSearchMode}
                        />
                    </div>
                </div>

                {/* Video Grid */}
                <section className="video-grid">
                    {isLoading ? (
                        <>
                            {[1, 2, 3, 4, 5, 6].map((i) => (
                                <div key={i} className="skeleton-card">
                                    <div className="skeleton skeleton-image"></div>
                                    <div className="skeleton-card-body">
                                        <div className="skeleton skeleton-avatar"></div>
                                        <div className="skeleton-card-text">
                                            <div className="skeleton skeleton-title"></div>
                                            <div className="skeleton skeleton-text"></div>
                                            <div className="skeleton skeleton-text-short"></div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </>
                    ) : videoList.length === 0 ? (
                        <div className="empty-state" style={{ gridColumn: '1 / -1' }}>
                            <div className="empty-state-icon">📹</div>
                            <div className="empty-state-title">영상이 없습니다</div>
                            <div className="empty-state-description">
                                검색 결과가 없거나 아직 등록된 영상이 없습니다.
                            </div>
                        </div>
                    ) : (
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
        </div>
    );
}
