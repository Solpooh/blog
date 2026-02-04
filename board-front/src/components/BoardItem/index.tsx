import React from 'react';
import './style.css'
import {BoardListItem} from 'types/interface';
import {useNavigate, useParams} from 'react-router-dom';
import defaultProfileImage from 'assets/image/default-profile-image.png';
import {BOARD_DETAIL_PATH, BOARD_PATH} from '../../constants';

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
                    {writeDatetime.split(' ')[0]}
                </div>
                <a className='board-read-link' onClick={(e) => { e.stopPropagation(); onClickHandler(); }}>
                    Read
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"/>
                        <polyline points="12 5 19 12 12 19"/>
                    </svg>
                </a>
            </div>
        </article>
    )
});

export default BoardItem;