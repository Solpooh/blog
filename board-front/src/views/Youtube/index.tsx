//  component: 유튜브 메인 화면 컴포넌트  //
import {VideoListItem} from 'types/interface';
import React, {ChangeEvent, KeyboardEvent, useCallback, useEffect, useRef, useState} from 'react';
import VideoItem from 'components/VideoItem';
import {getSearchVideoListRequest, getVideoListRequest} from 'apis';
import {GetVideoListResponseDto, GetSearchVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import './style.css';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {YOUTUBE_SEARCH_PATH} from "../../constants";
import SearchAutocomplete from 'components/SearchAutocomplete';

//  component: Youtube 컴포넌트  //
export default function Youtube() {
    //  state: searchWord path variable 상태 //
    const { searchWord } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();

    const pageParam = Number(searchParams.get("page")) || 1;
    const [currentPage, setCurrentPage] = useState(pageParam);

    //  state: 유튜브 최신 비디오 리스트 상태  //
    const [videoList, setVideoList] = useState<VideoListItem[]>([]);
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<VideoListItem> | null>(null)

    //  state: 검색어 저장 상태 //
    const [word, setWord] = useState<string>('');
    //  state: 로딩 상태 //
    const [isLoading, setIsLoading] = useState<boolean>(true);

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
        navigate(YOUTUBE_SEARCH_PATH(value));
        getSearchVideoListRequest(value, 0).then(getSearchVideoListResponse);
    }, [navigate]);

    //  event handler: 페이지 변경 함수 //
    const onPageChange = useCallback((page: number) => {
        setSearchParams({ page: String(page) });
        setCurrentPage(page);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [setSearchParams]);

    //  effect: page param 변경될 때마다 적용 //
    useEffect(() => {
        const page = Number(searchParams.get("page")) || 1;
        setCurrentPage(page);
    }, [searchParams]);

    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        setIsLoading(true);
        if (searchWord) {
            // 검색 모드
            setWord(searchWord);
            getSearchVideoListRequest(searchWord, currentPage - 1).then(getSearchVideoListResponse);
        } else {
            // 최신 모드
            getVideoListRequest(currentPage - 1).then(getVideoListResponse);
        }
    }, [currentPage, searchWord]);

    return (
        <div className="youtube-wrapper">
            <header className="youtube-header">
                <h1>최신 개발 Youtube</h1>

                <div className="youtube-search-box">
                    <SearchAutocomplete
                        value={word}
                        onChange={onSearchWordChange}
                        onSearch={onSearch}
                        placeholder="검색어를 입력해주세요."
                    />
                </div>
            </header>
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