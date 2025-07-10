import React, {useEffect} from 'react';
import './style.css'

//  interface: 페이지네이션 컴포넌트 properties //
interface Props {
    currentPage: number;  // 현재 페이지 (1부터 시작)
    totalPages: number;    // 전체 페이지 수
    onPageChange: (page: number) => void;  // 페이지 변경 이벤트 핸들러
}

//  component: 페이지네이션 컴포넌트 //
export default function Paging({ currentPage, totalPages, onPageChange }: Props) {
    // 한 섹션당 보여줄 페이지 수 (1~10, 11~20 등)
    const PAGES_PER_SECTION = 10;

    const currentSection = Math.ceil(currentPage / PAGES_PER_SECTION);
    const totalSections = Math.ceil(totalPages / PAGES_PER_SECTION);

    const startPage = (currentSection - 1) * PAGES_PER_SECTION + 1;
    const endPage = Math.min(startPage + PAGES_PER_SECTION - 1, totalPages);
    const viewPageList = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

    //  function: 페이지 변경 핸들러 //
    const onPreviousClickHandler = () => {
        if (currentSection === 1) return;
        const prevPage = (currentSection - 1) * PAGES_PER_SECTION;
        onPageChange(prevPage);
    };

    const onNextClickHandler = () => {
        if (currentSection === totalSections) return;
        const nextPage = currentSection * PAGES_PER_SECTION + 1;
        onPageChange(nextPage);
    };

    //  effect: 페이지 변경 시 화면 상단으로 스크롤 이동 //
    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [currentPage]);

    //  render: 페이지네이션 컴포넌트 렌더링 //
    return (
        <div id='pagination-wrapper'>
            <div className='pagination-change-link-box'>
                <div className='icon-box-small'>
                    <div className='icon expand-left-icon'></div>
                </div>
                <div className='pagination-change-link-text' onClick={onPreviousClickHandler}>{'이전'}</div>
            </div>
            {/*<div className='pagination-divider'>{'\|'}</div>*/}

            {viewPageList.map(page =>
            page === currentPage ?
                <div key={page} className='pagination-text-active'>{page}</div> :
                <div key={page} className='pagination-text' onClick={() => onPageChange(page)}>{page}</div>
            )}

            {/*<div className='pagination-divider'>{'\|'}</div>*/}
            <div className='pagination-change-link-box'>
                <div className='pagination-change-link-text' onClick={onNextClickHandler}>{'다음'}</div>
                <div className='icon-box-small'>
                    <div className='icon expand-right-icon'></div>
                </div>
            </div>
        </div>
    )
}