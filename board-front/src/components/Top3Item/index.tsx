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
        <div className='top-3-list-item' style={{ backgroundImage: `url(${boardTitleImage ? boardTitleImage : defaultTitleImage})`}} onClick={onClickHandler} role='img' aria-label={`${title} 배경 이미지`}>
            <div className='top-3-list-item-main-box'>
                <div className='top-3-list-item-top'>
                    <div className='top-3-list-item-profile-box'>
                        <div className='top-3-list-item-profile-image' style={{ backgroundImage: `url(${writerProfileImage ? writerProfileImage : defaultProfileImage})` }} role='img' aria-label={`${writerNickname} 프로필 이미지`}></div>
                    </div>
                    <div className='top-3-list-item-write-box'>
                        <div className='top-3-list-item-nickname'>{writerNickname}</div>
                        <div className='top-3-list-item-write-date'>{writeDatetime}</div>
                    </div>
                </div>
                <div className='top-3-list-item-middle'>
                    <h3 className='top-3-list-item-title'>{title}</h3>
                    <div className='top-3-list-item-content'>{content}</div>
                </div>
                <div className='top-3-list-item-bottom'>
                    <div className='top-3-list-item-counts'>
                        {`댓글 ${commentCount} · 좋아요 ${favoriteCount} · 조회수 ${viewCount}`}
                    </div>
                </div>
            </div>
        </div>
    )
});

export default Top3Item;
