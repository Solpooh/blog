import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import defaultProfileImage from 'assets/image/default-profile-image.png';
import {useNavigate, useParams} from "react-router-dom";
import {BoardListItem} from "../../types/interface";
import {latestBoardListMock} from "../../mocks";
import BoardItem from "../../components/BoardItem";
import {useLoginUserStore} from "../../stores";
import {BOARD_PATH, BOARD_WRITE_PATH, USER_PATH} from "../../constants";

//  component: 유저 화면 컴포넌트 //
export default function User() {
    //  state: userEmail path variable 상태 //
    const { userEmail } = useParams();
    //  state: 로그인 유저 상태 //
    const { loginUser } = useLoginUserStore();
    //  state: 마이페이지 여부 상태 //
    const [isMyPage, setMyPage] = useState<boolean>(true);

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();

    //  component: 유저 화면 상단 컴포넌트 //
    const UserTop = () => {

        //  state: 이미지 파일 인풋 참조 상태 //
        const imageInputRef = useRef<HTMLInputElement | null>(null);
        //  state: 닉네임 변경 여부 상태 //
        const [isNicknameChange, setNicknameChange] = useState<boolean>(false);
        //  state: 닉네임 상태 //
        const [nickname, setNickname] = useState<string>('');
        //  state: 변경 닉네임 상태 //
        const [changeNickname, setChangeNickname] = useState<string>('');
        //  state: 프로필 이미지 상태 //
        const [profileImage, setProfileImage] = useState<string | null>(null);

        //  event handler: 프로필 이미지 박스 클릭 이벤트 처리 //
        const onProfileBoxClickHandler = () => {
            if (!isMyPage) return;
            if (!imageInputRef.current) return;
            imageInputRef.current.click();
        }
        //  event handler: 닉네임 수정 버튼 클릭 이벤트 처리 //
        const onNicknameEditButtonClickHandler = () => {
            setChangeNickname(nickname);
            setNicknameChange(!isNicknameChange);
        }
        //  event handler: 프로필 이미지 변경 이벤트 처리 //
        const onProfileImageChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
            if (!event.target.files || !event.target.files.length) return;

            const file = event.target.files[0];
            const data = new FormData();
            data.append('file', file);
        };
        //  event handler: 닉네임 변경 이벤트 처리 //
        const onNicknameChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
            const { value } = event.target;
            setChangeNickname(value);
        };

        //  effect: userEmail path variable 변경 시 실행할 함수 //
        useEffect(() => {

            if (!userEmail) return;
            setNickname('Solpooh');
            setProfileImage('data:image/jpeg;base64,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');

        }, [userEmail]);

        //  render: 유저 화면 상단 컴포넌트 렌더링 //
        return (
            <div id='user-top-wrapper'>
                <div className='user-top-container'>
                    {isMyPage ?
                    <div className='user-top-my-profile-image-box' onClick={onProfileBoxClickHandler}>
                        {profileImage !== null ?
                        <div className='user-top-profile-image' style={{ backgroundImage: `url(${profileImage})` }}></div> :
                        <div className='icon-box-large'>
                           <div className='icon image-box-white-icon'></div>
                        </div>
                        }
                        <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} onChange={onProfileImageChangeHandler} />
                    </div> :
                    <div className='user-top-profile-image-box' style={{ backgroundImage: `url(${profileImage ? profileImage : defaultProfileImage})` }}></div>
                    }
                    <div className='user-top-info-box'>
                        <div className='user-top-info-nickname-box'>
                            {isMyPage ?
                            <>
                            {isNicknameChange ?
                            <input className='user-top-info-nickname-input' type='text' size={changeNickname.length + 2} value={changeNickname} onChange={onNicknameChangeHandler} /> :
                            <div className='user-top-info-nickname'>{nickname}</div>
                            }
                            <div className='icon-button' onClick={onNicknameEditButtonClickHandler}>
                                <div className='icon edit-icon'></div>
                            </div>
                            </> :
                            <div className='user-top-info-nickname'>{nickname}</div>
                            }
                        </div>
                        <div className='user-top-info-email'>{'email@email.com'}</div>
                    </div>
                </div>
            </div>
        );
    };

    //  component: 유저 화면 하단 컴포넌트 //
    const UserBottom = () => {

        //  state: 게시물 개수 상태 //
        const [count, setCount] = useState<number>(2);
        //  state: 게시물 리스트 상태 (임시) //
        const [userBoardList, setUserBoardList] = useState<BoardListItem[]>([]);

        //  event handler: 사이드 카드 클릭 이벤트 처리 //
        const onSideCardClickHandler = () => {
            if (isMyPage) navigate(BOARD_PATH() + '/' + BOARD_WRITE_PATH());
            else if (loginUser) navigate(USER_PATH(loginUser.email));
        };

        //  effect: userEmail path variable 이 변경될 때마다 실행할 함수 //
        useEffect(() => {
            setUserBoardList(latestBoardListMock);
        }, [userEmail]);

        //  render: 유저 화면 하단 컴포넌트 렌더링 //
        return (
            <div id='user-bottom-wrapper'>
                <div className='user-bottom-container'>
                    <div className='user-bottom-title'>{isMyPage ? '내 게시물 ' : '게시물 '}<span className='emphasis'>{count}</span></div>
                    <div className='user-bottom-contents-box'>
                        {count === 0 ?
                            <div className='user-bottom-contents-nothing'>{'게시물이 없습니다.'}</div> :
                            <div className='user-bottom-contents'>
                                {userBoardList.map(boardListItem => <BoardItem boardListItem={boardListItem} />)}
                            </div>
                        }
                        <div className='user-bottom-side-box'>
                            <div className='user-bottom-side-card' onClick={onSideCardClickHandler}>
                                <div className='user-bottom-side-container'>
                                    {isMyPage ?
                                    <>
                                    <div className='icon-box'>
                                        <div className='icon edit-icon'></div>
                                    </div>
                                    <div className='user-bottom-side-text'>{'글쓰기'}</div>
                                    </> :
                                    <>
                                    <div className='user-bottom-side-text'>{'내 게시물로 가기'}</div>
                                    <div className='icon-box'>
                                        <div className='icon arrow-right-icon'></div>
                                    </div>
                                    </>
                                    }
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className='user-bottom-pagination-box'></div>
                </div>
            </div>
        );
    };


    //  render: 유저 화면 컴포넌트 렌더링 //
    return (
        <>
        <UserTop />
        <UserBottom />
        </>
    );
};