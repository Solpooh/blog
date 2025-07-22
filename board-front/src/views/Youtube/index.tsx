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
import {useNavigate, useParams} from "react-router-dom";
import {YOUTUBE_SEARCH_PATH} from "../../constants";

//  component: Youtube 컴포넌트  //
export default function Youtube() {
    //  state: searchWord path variable 상태 //
    const { searchWord } = useParams();

    //  state: 유튜브 최신 비디오 리스트 상태  //
    const [videoList, setVideoList] = useState<VideoListItem[]>([]);
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<VideoListItem> | null>(null)
    //  state: 현재 페이지 상태 //
    const [currentPage, setCurrentPage] = useState<number>(1);

    //  state: 검색 조건 상태 //
    const [searchType, setSearchType] = useState<'channel' | 'title'>('channel');
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

        const { pagination } = responseBody as GetVideoListResponseDto;
        setVideoList(pagination.content);
        setPagination(pagination);
    }
    //  function: Search videoList response 처리 함수 //
    const getSearchVideoListResponse = (responseBody: GetSearchVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const {code} = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;


        const {pagination} = responseBody as GetSearchVideoListResponseDto;
        setVideoList(pagination.content);
        setPagination(pagination);
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

    //  event handler: 검색 버튼 클릭 이벤트 처리 함수 //
    const onSearchButtonClickHandler = () => {
        if (!word) {
            alert('검색어를 입력해주세요.');
            return;
        }
        navigate(YOUTUBE_SEARCH_PATH(word));
        setCurrentPage(1);
        getSearchVideoListRequest(word, searchType, currentPage - 1).then(getSearchVideoListResponse);
    }

    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        if (searchWord) {
            // 검색 모드
            getSearchVideoListRequest(searchWord, searchType, currentPage - 1).then(getSearchVideoListResponse);
        } else {
            // 최신 모드
            getVideoListRequest(currentPage - 1).then(getVideoListResponse);
        }
    }, [currentPage, searchWord]);

    return (
        <div className="youtube-wrapper">
            <div className="youtube-header">
                <h2>추천 유튜브 영상</h2>
                <div className="search-box">
                    <select value={searchType} onChange={e => setSearchType(e.target.value as 'channel' | 'title')}>
                        <option value="channel">채널명</option>
                        <option value="title">비디오 제목</option>
                    </select>
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
                        currentPage={currentPage}
                        totalPages={pagination.totalPages}
                        onPageChange={setCurrentPage}
                    />
                </div>
            )}
        </div>
    );
}