import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {CommentListItem} from 'types/interface';
import defaultProfileImage from 'assets/image/default-profile-image.png';
import dayjs from 'dayjs';
import {useLoginUserStore} from 'stores';
import {useCookies} from 'react-cookie';
import {PatchCommentRequestDto} from 'apis/request/board';
import {useNavigate, useParams} from 'react-router-dom';
import {deleteCommentRequest, patchCommentRequest} from 'apis';
import {PatchCommentResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {DeleteCommentResponseDto} from '../../apis/response/board';

interface Props {
    commentListItem: CommentListItem;
}

//  component: Comment List Item 컴포넌트 //
export default function CommentItem({ commentListItem }: Props) {
    //  function: 작성일 경과시간 함수  //
    const getElapsedTime = () => {
        const now = dayjs().add(9, 'hour');   // 한국과의 시차
        const writeTime = dayjs(writeDatetime);

        const gap = now.diff(writeTime, 's');
        if (gap < 60) return `${gap}초 전`;
        if (gap < 3600) return `${Math.floor(gap / 60)}분 전`;
        if (gap < 86400) return `${Math.floor(gap / 3600)}시간 전`;

        return `${Math.floor(gap / 86400)}일 전`;
    }

    //  state: 로그인 상태  //
    const { loginUser } = useLoginUserStore();
    //  state: properties  //
    const { commentNumber, nickname, profileImage, writeDatetime, content, userEmail } = commentListItem;
    //  state: 댓글 textarea 참조 상태  //
    const commentRef = useRef<HTMLTextAreaElement | null>(null);
    //  state: 댓글 수정내용 상태  //
    const [editContent, setEditContent] = useState<string>(content);
    //  state: 댓글 수정시간 상태  //
    const [elapsedTime, setElapsedTime] = useState<string>(getElapsedTime());
    //  state: 댓글 수정모드 상태  //
    const [isEdit, setEdit] = useState<boolean>(false);
    //  state: 댓글 삭제 상태  //
    const [isDelete, setDelete] = useState<boolean>(false);
    //  state: cookie 상태  //
    const [cookies, setCookie] = useCookies();
    //  state: 게시물 번호  //
    const { boardNumber } = useParams();

    const accessToken = cookies.accessToken;

    //  function: patch comment response 처리 함수  //
    const patchCommentResponse = (responseBody: PatchCommentResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'AF' || code === 'NU' || code == 'NB' || code == 'NC' || code == 'NP') alert("잘못된 요청 시도입니다.");
        if (code === 'VF') alert('내용을 입력해주세요.');
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        if (!boardNumber) return;
        commentListItem.content = editContent;
        setEdit(false);
    }
    //  function: delete comment response 처리 함수  //
    const deleteCommentResponse = (responseBody: DeleteCommentResponseDto | ResponseDto | null) => {
        // eslint-disable-next-line no-restricted-globals
        if (!confirm("댓글을 삭제하시겠습니까?")) return;

        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'AF' || code === 'NU' || code == 'NB' || code == 'NC' || code == 'NP') alert("잘못된 요청 시도입니다.");
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        setDelete(true);
    }

    //  event handler: 수정 버튼 클릭 이벤트 처리  //
    const onUpdateButtonClickHandler = () => {
        if (!commentListItem || !loginUser) return;
        if (loginUser.email !== userEmail) return;

        setEdit(true);
    }
    //  event handler: 댓글 변경 이벤트 처리  //
    const onCommentChangeHandler = (event: ChangeEvent<HTMLTextAreaElement>) => {
        const { value } = event.target;
        setEditContent(value);
        if (!commentRef.current) return;
        commentRef.current.style.height = 'auto';
        commentRef.current.style.height = `${commentRef.current.scrollHeight}px`;
    }
    //  event handler: 수정 취소 버튼 클릭 이벤트 처리  //
    const onCancelButtonClickHandler = () => {
        setEdit(false);
        setEditContent(content);
    }
    //  event handler: 수정 등록 버튼 클릭 이벤트 처리  //
    const onSubmitButtonClickHandler = () => {
        if (!accessToken) return;
        if (!boardNumber || !commentNumber) return;

        const requestBody: PatchCommentRequestDto = {
            // 수정된 내용 전송
            content: editContent
        }

        patchCommentRequest(boardNumber, commentNumber, requestBody, accessToken).then(patchCommentResponse);
    }
    //  event handler: 삭제 버튼 클릭 이벤트 처리  //
    const onDeleteButtonClickHandler = () => {
        if (!accessToken) return;
        if (!boardNumber || !commentNumber) return;

        deleteCommentRequest(boardNumber, commentNumber, accessToken).then(deleteCommentResponse);
    }

    useEffect(() => {
        setElapsedTime(getElapsedTime()); // 댓글 수정 후, 시간 갱신
    }, [editContent]);

    //  render: Comment List Item 렌더링 //
    if (isDelete) {
        return (
            <div className="comment-list-item-deleted">
                <p>이 댓글은 삭제되었습니다.</p>
            </div>
        );
    }
    return (
        <div className='comment-list-item'>
            {isEdit ?
                <div className='board-detail-bottom-comment-input-box'>
                    <div className='board-detail-bottom-comment-input-container'>
                        <textarea ref={commentRef} className='board-detail-bottom-comment-textarea' value={editContent} onChange={onCommentChangeHandler} />
                        <div className='board-detail-bottom-comment-button-box'>
                            <div className='board-detail-bottom-comment-cancel-button' onClick={onCancelButtonClickHandler}>{'취소'}</div>
                            <div className='comment-list-item-divider'>{'|'}</div>
                            <div className='board-detail-bottom-comment-submit-button' onClick={onSubmitButtonClickHandler}>{'등록'}</div>
                        </div>
                    </div>
                </div>
                :
                <div className='comment-list-item-top'>
                    <div className='comment-list-item-profile-box'>
                        <div className='comment-list-item-profile-image' style={{ backgroundImage: `url(${profileImage ? profileImage : defaultProfileImage})` }}></div>
                    </div>
                    <div className='comment-list-item-nickname'>{nickname}</div>
                    <div className='comment-list-item-divider'>{'\|'}</div>
                    <div className='comment-list-item-time'>{getElapsedTime()}</div>
                    {loginUser && loginUser.email === userEmail &&
                        <>
                            <div className='comment-list-update-button' onClick={onUpdateButtonClickHandler}>{'수정'}</div>
                            <div className='comment-list-item-divider'>{'|'}</div>
                            <div className='comment-list-delete-button' onClick={onDeleteButtonClickHandler}>{'삭제'}</div>
                        </>
                    }
                </div>
            }
            {!isEdit && (
                <div className='comment-list-item-main'>
                    <div className='comment-list-item-content'>{content}</div>
                </div>
            )}
        </div>
    );
}