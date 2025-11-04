import React, {useEffect, useState} from 'react';
import './style.css';
import {useNavigate, useParams} from 'react-router-dom';
import {BoardListItem} from 'types/interface';
import BoardItem from 'components/BoardItem';
import {SEARCH_PATH} from "../../constants";
import {getRelationListRequest, getSearchBoardListRequest} from 'apis';
import {GetSearchBoardListResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {GetRelationListResponseDto} from 'apis/response/search';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';

//  component: 검색 화면 컴포넌트 //
export default function Search() {

    //  state: searchWord path variable 상태 //
    const { searchWord } = useParams();
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<BoardListItem> | null>(null)
    //  state: 검색글 리스트 상태 //
    const [searchBoardList, setSearchBoardList] = useState<BoardListItem[]>([]);
    //  state: 현재 페이지 상태 //
    const [currentPage, setCurrentPage] = useState<number>(1);
    //  state: 이전 검색어 상태 //
    const [preSearchWord, setPreSearchWord] = useState<string | null>(null);
    //  state: 검색 게시물 개수 상태 //
    const [count, setCount] = useState<number>(0);
    //  state: 관련 검색어 리스트 상태 //
    const [relativeWordList, setRelativeWordList] = useState<string[]>([]);

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();
    //  function: get search board list response 처리 함수 //
    const getSearchBoardListResponse = (responseBody: GetSearchBoardListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        if (!searchWord) return;
        const { searchList } = (responseBody as GetSearchBoardListResponseDto).data;

        setPagination(pagination);
        setCount(searchList.totalElements);
        setSearchBoardList(searchList.content);
        setPreSearchWord(searchWord);
    }
    //  function: get relation list response 처리 함수 //
    const getRelationListResponse = (responseBody: GetRelationListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { relativeWordList } = (responseBody as GetRelationListResponseDto).data;
        setRelativeWordList(relativeWordList);
    }

    //  event handler: 연관 검색어 클릭 이벤트 처리 //
    const onRelationWordClickHandler = (word: string) => {
        navigate(SEARCH_PATH(word));
        setCurrentPage(1);
    }

    //  effect: search word 상태 변경 시 실행될 함수 //
    useEffect(() => {
        if (!searchWord) return;
        getSearchBoardListRequest(searchWord, preSearchWord, currentPage - 1).then(getSearchBoardListResponse);
        getRelationListRequest(searchWord).then(getRelationListResponse);
    }, [searchWord, preSearchWord, currentPage]);

    //  render: 검색 화면 컴포넌트 렌더링 //
    if (!searchWord) return (<></>)
    return (
        <div id='search-wrapper'>
            <div className='search-container'>
                <div className='search-title-box'>
                    <div className='search-title'><span className='emphasis'>{searchWord}</span>{'에 대한 검색결과입니다.'}</div>
                    <div className='search-count'>{count}</div>
                </div>
                <div className='search-contents-box'>
                    {count === 0 ?
                    <div className='search-contents-nothing'>{'검색 결과가 없습니다.'}</div> :
                    <div className='search-contents'>
                        {searchBoardList.map(boardListItem => <BoardItem boardListItem={boardListItem} /> )}
                    </div>
                    }
                    <div className='search-relation-box'>
                        <div className='search-relation-card'>
                            <div className='search-relation-card-container'>
                                <div className='search-relation-card-title'>{'관련 검색어'}</div>
                                {relativeWordList.length === 0 ?
                                <div className='search-relation-card-contents-nothing'></div> :
                                <div className='search-relation-card-contents'>
                                {relativeWordList.map(word => <div className='word-badge' onClick={() => onRelationWordClickHandler(word)}>{word}</div>)}
                                </div>
                                }
                            </div>
                        </div>
                    </div>
                </div>
                <div className='search-pagination-box'>
                    {count !== 0 && pagination &&
                     <Paging
                         currentPage={currentPage}
                         totalPages={pagination.totalPages}
                         onPageChange={setCurrentPage}
                     />
                    }
                </div>
            </div>
        </div>
    )
};