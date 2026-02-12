import React from 'react';
import './style.css'
import {BoardListItem} from 'types/interface';
import {useNavigate, useParams} from 'react-router-dom';
import defaultProfileImage from 'assets/image/default-profile-image.png';
import {BOARD_DETAIL_PATH, BOARD_PATH} from '../../constants';
import dayjs from 'dayjs';

interface Props {
    boardListItem: BoardListItem
}

// component: Board Item 컴포넌트 //
const BoardItem = React.memo(({ boardListItem }: Props) => {
    // properties //
    const { boardNumber, title, category, content, boardTitleImage } = boardListItem;
    const { favoriteCount, commentCount, viewCount } = boardListItem;
    const { writeDatetime, writerNickname, writerProfileImage } = boardListItem;

    // function: 네비게이트 함수 //
    const navigate = useNavigate();

    // event handler: 게시물 아이템 클릭 이벤트 처리 함수
    const onClickHandler = () => {
        navigate(BOARD_PATH() + '/' + BOARD_DETAIL_PATH(category, boardNumber));
    }

    // function: 게시글이 최근 3일 이내인지 확인
    const isNew = () => {
        const postDate = new Date(writeDatetime);
        const now = new Date();
        const diffTime = Math.abs(now.getTime() - postDate.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 3;
    };

    // function: 날짜 포맷 함수 //
    const formatDate = (datetime: string) => {
        const date = dayjs(datetime);
        const today = dayjs();
        if (date.isSame(today, 'day')) {
            return `오늘 ${date.format('HH:mm')}`;
        }
        return date.format('YYYY-MM-DD HH:mm');
    };

    // function: 태그 추출 (간단 구현: 카테고리와 제목 키워드 사용)
    const getTags = () => {
        const tags = [];
        if (category && category !== 'All') {
            tags.push(`#${category.toLowerCase()}`);
        }
        // 제목에서 간단한 키워드 추출 (첫 2단어)
        const words = title.split(' ').slice(0, 2);
        words.forEach(word => {
            if (word.length > 2) {
                tags.push(`#${word.toLowerCase()}`);
            }
        });
        return tags.slice(0, 3); // 최대 3개
    };

    // render: Board List Item 컴포넌트 렌더링 //
    return (
        <article className='board-list-item' onClick={onClickHandler}>
            {/* Featured Image */}
            {boardTitleImage && (
                <div className='board-featured-image' style={{ backgroundImage: `url(${boardTitleImage})` }} />
            )}

            {/* Top Meta Row */}
            <div className='board-item-meta'>
                <span className='board-category-badge'>{category}</span>
                {isNew() && <span className='new-badge'>• New</span>}
            </div>

            {/* Author Info */}
            <div className='board-author'>
                <div className='board-author-avatar' style={{ backgroundImage: `url(${writerProfileImage || defaultProfileImage})` }} />
                <span className='board-author-name'>{writerNickname}</span>
            </div>

            {/* Title */}
            <h3 className='board-list-item-title'>{title}</h3>

            {/* Content */}
            <p className='board-list-item-content'>{content}</p>

            {/* Tags */}
            <div className='board-tags'>
                {getTags().map((tag, index) => (
                    <span key={index} className='board-tag'>{tag}</span>
                ))}
            </div>

            {/* Bottom Row */}
            <div className='board-bottom-row'>
                <div className='board-date'>
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {formatDate(writeDatetime)}
                </div>
                <div className='board-stats'>
                    <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                        {favoriteCount}
                    </span>
                    <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                        {commentCount}
                    </span>
                    <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                        {viewCount}
                    </span>
                </div>
            </div>
        </article>
    )
});

export default BoardItem;