import React, {useEffect} from 'react';
import './style.css'

//  interface: 페이지네이션 컴포넌트 properties //
interface Props {
    pagination: {
        page: number;            // 0-based index
        size: number;
        totalPages: number;
        totalElements: number;
        first: boolean;
        last: boolean;
        numberOfElements: number;
    };
    onPageChange: (page: number) => void; // page는 1 기반으로 전달
}

//  component: 페이지네이션 컴포넌트 - 전자정부 프레임웤 기준  //
export default function Paging({ pagination, onPageChange }: Props) {
    const { page, totalPages } = pagination;

    if (totalPages <= 1) return null;

    const currentPage = page + 1;  // UI 표시용
    const lastPage = totalPages;

    const pages = [];
    // 항상 포함: 첫 페이지
    pages.push(1);

    // 현재 페이지 -2 부터 +2까지
    const start = Math.max(2, currentPage - 2);
    const end = Math.min(lastPage - 1, currentPage + 2);

    if (start > 2) pages.push('...');
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < lastPage - 1) pages.push('...');

    // 항상 포함: 마지막 페이지
    if (lastPage > 1) pages.push(lastPage);


    //  render: 페이지네이션 컴포넌트 렌더링 //
    return (
        <div className="pagination-container">
            <button
                className="page-btn"
                disabled={currentPage === 1}
                onClick={() => onPageChange(currentPage - 1)}
            >
                〈
            </button>

            {pages.map((p, idx) =>
                typeof p === 'number' ? (
                    <button key={idx} className={`page-btn ${p === currentPage ? 'active' : ''}`} onClick={() => onPageChange(p)}>
                        {p}
                    </button>
                ) : (
                    <span key={idx} className="ellipsis">...</span>
                )
            )}

            <button
                className="page-btn"
                disabled={currentPage === lastPage}
                onClick={() => onPageChange(currentPage)}
            >
                〉
            </button>
        </div>
    )
}