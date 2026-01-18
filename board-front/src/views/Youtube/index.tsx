//  component: 유튜브 메인 화면 컴포넌트  //
import {VideoListItem} from 'types/interface';
import React, {ChangeEvent, KeyboardEvent, useEffect, useRef, useState} from 'react';
import VideoItem from 'components/VideoItem';
import {getSearchVideoListRequest, getVideoListRequest} from 'apis';
import {GetVideoListResponseDto, GetSearchVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import './style.css';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {YOUTUBE_SEARCH_PATH} from "../../constants";

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
    //  state: 검색 버튼 요소 참조 상태 //
    const searchButtonRef = useRef<HTMLDivElement | null>(null);

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();
    //  function: Latest videoList response 처리 함수 //
    const getVideoListResponse = (responseBody: GetVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { videoList } = (responseBody as GetVideoListResponseDto).data;
        setVideoList(videoList.content);
        setPagination(videoList);
    }
    //  function: Search videoList response 처리 함수 //
    const getSearchVideoListResponse = (responseBody: GetSearchVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const {code} = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;


        const {videoList} = (responseBody as GetSearchVideoListResponseDto).data;
        setVideoList(videoList.content);
        setPagination(videoList);
    }

    //  event handler: 검색어 변경 이벤트 처리 함수 //
    const onSearchWordChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        const value = event.target.value;
        setWord(value);
    };
    //  event handler: 검색어 키 이벤트 처리 함수 //
    const onSearchWordKeyDownHandler = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key !== 'Enter') return;
        if (!searchButtonRef.current) return;
        searchButtonRef.current.click();
    };
    //  event handler: 페이지 변경 함수 //
    const onPageChange = (page: number) => {
        setSearchParams({ page: String(page) });
        setCurrentPage(page);
    }

    //  event handler: 검색 버튼 클릭 이벤트 처리 함수 //
    const onSearchButtonClickHandler = () => {
        if (!word) {
            alert('검색어를 입력해주세요.');
            return;
        }
        navigate(YOUTUBE_SEARCH_PATH(word));
        getSearchVideoListRequest(word,currentPage - 1).then(getSearchVideoListResponse);
    }

    //  effect: page param 변경될 때마다 적용 //
    useEffect(() => {
        const page = Number(searchParams.get("page")) || 1;
        setCurrentPage(page);
    }, [searchParams]);

    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        if (searchWord) {
            // 검색 모드
            getSearchVideoListRequest(searchWord,currentPage - 1).then(getSearchVideoListResponse);
        } else {
            // 최신 모드
            getVideoListRequest(currentPage - 1).then(getVideoListResponse);
        }
    }, [currentPage, searchWord]);

    return (
        <div className="youtube-wrapper">
            <div className="youtube-header">
                <h2>최신 개발 Youtube</h2>

                <div className="search-box">
                    <input type="text" placeholder="검색어를 입력해주세요." value={word} onChange={onSearchWordChangeHandler} onKeyDown={onSearchWordKeyDownHandler} />
                    <div ref={searchButtonRef} className='icon-button' onClick={onSearchButtonClickHandler}>
                        <div className='icon search-light-icon'></div>
                    </div>
                </div>
            </div>
            <div className="video-grid">
                {videoList.map(videoItem => <VideoItem key={videoItem.videoId} videoItem={videoItem} />)}
            </div>

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