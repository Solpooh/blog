import React from 'react';
import './style.css';
import defaultProfileImage from 'assets/image/default-profile-image.png';
import defaultTitleImage from 'assets/image/default-title-image.jpg';
import { BoardListItem } from 'types/interface';
import {useNavigate, useParams} from 'react-router-dom';
import {BOARD_DETAIL_PATH, BOARD_PATH} from '../../constants';

interface Props {
    top3ListItem: BoardListItem
}

//  component: Top 3 List Item 컴포넌트 //
const Top3Item = React.memo(({ top3ListItem }: Props) => {

    const { boardNumber, title, category, content, boardTitleImage } = top3ListItem;
    const { favoriteCount, commentCount, viewCount } = top3ListItem;
    const { writeDatetime, writerNickname, writerProfileImage } = top3ListItem;

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();
    const { page } = useParams();

    //  event handler: 게시물 아이템 클릭 이벤트 처리 함수 //
    const onClickHandler = () => {
        navigate(BOARD_PATH() + '/' + BOARD_DETAIL_PATH(category, boardNumber) + '?page=' + page);
    }

    //  render: Top 3 List Item 컴포넌트 렌더링 //
    return (
        <article className='top-3-list-item' onClick={onClickHandler}>
            {/* Badges Row */}
            <div className='top-3-badges'>
                <span className='featured-badge-small'>⭐ Featured</span>
                <span className='category-badge'>{category}</span>
            </div>

            {/* Author Info */}
            <div className='top-3-author'>
                <div className='author-avatar' style={{ backgroundImage: `url(${writerProfileImage || defaultProfileImage})` }} />
                <span className='author-name'>{writerNickname}</span>
            </div>

            {/* Title */}
            <h3 className='top-3-list-item-title'>{title}</h3>

            {/* Content */}
            <p className='top-3-list-item-content'>{content}</p>

            {/* Bottom Row */}
            <div className='top-3-bottom-row'>
                <div className='top-3-date'>
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {writeDatetime}
                </div>
                <button className='read-more-btn'>
                    Read More
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"/>
                        <polyline points="12 5 19 12 12 19"/>
                    </svg>
                </button>
            </div>
        </article>
    )
});

export default Top3Item;
