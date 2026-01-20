import React, {ChangeEvent, KeyboardEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import {
    AUTH_PATH,
    BOARD_DETAIL_PATH, BOARD_PATH,
    BOARD_UPDATE_PATH,
    BOARD_WRITE_PATH,
    MAIN_PATH,
    SEARCH_PATH,
    USER_PATH, YOUTUBE_PATH, YOUTUBE_TREND_PATH
} from '../../constants';
import {useCookies} from 'react-cookie';
import {useBoardStore, useEditorStore, useLoginUserStore} from '../../stores';
import {fileUploadRequest, patchBoardRequest, postBoardRequest} from '../../apis';
import {PatchBoardRequestDto, PostBoardRequestDto} from '../../apis/request/board';
import {PatchBoardResponseDto, PostBoardResponseDto} from '../../apis/response/board';
import {ResponseDto} from '../../apis/response';
import { convertToRaw, EditorState} from 'draft-js';
import SearchAutocomplete from '../../components/SearchAutocomplete';
import ThemeToggle from '../../components/ThemeToggle';

//  component: 헤더 레이아웃 //
export default function Header() {

    //  state: 로그인 유저 상태 //
    const { loginUser, setLoginUser, resetLoginUser } = useLoginUserStore();
    //  state: path 상태 //
    const { pathname } = useLocation();
    //  state: cookie 상태 //
    const [cookies, setCookie] = useCookies();
    //  state: 로그인 상태 //
    const [isLogin, setLogin] = useState<boolean>(false);

    const isAuthPage = pathname.startsWith(AUTH_PATH());
    const isMainPage = pathname === MAIN_PATH();
    const isSearchPage = pathname.startsWith(SEARCH_PATH(''));
    const isBoardDetailPage = pathname.startsWith(BOARD_PATH() + '/' + BOARD_DETAIL_PATH('', ''));
    const isBoardWritePage = pathname.startsWith(BOARD_PATH() + '/' + BOARD_WRITE_PATH());
    const isBoardUpdatePage = pathname.startsWith(BOARD_PATH() + '/' + BOARD_UPDATE_PATH(''));
    const isUserPage = pathname.startsWith(USER_PATH(''));
    const isYoutubePage = pathname.startsWith(YOUTUBE_PATH());

    //  function: 네비게이트 함수 //
    const navigate = useNavigate();

    //  event handler: 로고 클릭 이벤트 처리 함수 //
    const onLogoClickHandler = () => {
        navigate('/');
    }
    const onYoutubeClickHandler = () => {
        navigate(YOUTUBE_PATH())
    }
    const onTrendClickHandler = () => {
        navigate(YOUTUBE_TREND_PATH())
    }

    //  event handler: 키보드 네비게이션 이벤트 처리 함수 //
    const onKeyDownHandler = (e: React.KeyboardEvent, callback: () => void) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            callback();
        }
    }

    //  component: 검색 버튼 컴포넌트 //
    const SearchButton = () => {
        //  state: 검색어 상태 //
        const [word, setWord] = useState<string>('');
        //  state: 검색어 path variable 상태 //
        const { searchWord } = useParams();

        //  event handler: 검색어 변경 이벤트 처리 함수 //
        const onSearchWordChange = (value: string) => {
            setWord(value);
        };

        //  event handler: 검색 실행 처리 함수 //
        const onSearch = (value: string) => {
            if (!value.trim()) {
                alert('검색어를 입력해주세요.');
                return;
            }
            navigate(SEARCH_PATH(value));
        };

        //  effect: 검색어 path variable 변경 될때마다 실행될 함수 //
        useEffect(() => {
            if (searchWord) {
                setWord(searchWord);
            }
        }, [searchWord]);

        //  effect: login user 변경 될때마다 실행될 함수 //
        useEffect(() => {
            setLogin(loginUser !== null);
        }, [loginUser])

        // render: 검색 버튼 컴포넌트 렌더링 //
        return (
            <div className='header-search-box'>
                <SearchAutocomplete
                    value={word}
                    onChange={onSearchWordChange}
                    onSearch={onSearch}
                    placeholder='검색어를 입력해주세요.'
                />
            </div>
        );
    }

    //  component: 마이페이지 버튼 컴포넌트 //
    const MyPageButton = () => {
        //  state: userEmail path variable 상태 //
        const { userEmail } = useParams();

        //  event handler: 마이페이지 버튼 클릭 이벤트 처리 함수 //
        const onMyPageButtonClickHandler = () => {
            if (!loginUser) return;
            const { email } = loginUser;
            navigate(USER_PATH(email));
        }
        //  event handler: 로그아웃 버튼 클릭 이벤트 처리 함수 //
        const onSignOutButtonClickHandler = () => {
            // eslint-disable-next-line no-restricted-globals
            if (!confirm("정말 로그아웃 하시겠습니까?")) return;
            resetLoginUser();
            setCookie('accessToken', '', { path: '/', expires: new Date() });
            setLogin(false);
            navigate(MAIN_PATH());
        }

        //  event handler: 로그인 버튼 클릭 이벤트 처리 함수 //
        const onSignInButtonClickHandler = () => {
            navigate(AUTH_PATH());
        }

        //  effect: 새로고침 시 isLogin true 유지  //
        useEffect(() => {
            // 쿠키에서 토큰 확인 후 로그인 상태 복원
            if (!cookies.accessToken) {
                setLogin(false);
            } else {
                setLogin(true);
            }
        }, []);

        //    render: 로그아웃 버튼 컴포넌트 렌더링  //
        if(isLogin && (userEmail === loginUser?.email))
        return(
            <div className='black-button' onClick={onSignOutButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onSignOutButtonClickHandler)} role='button' aria-label='로그아웃' tabIndex={0}>{'로그아웃'}</div>
        )
        if(isLogin)
        //    render: 마이페이지 버튼 컴포넌트 렌더링  //
        return(
            <div className='white-button' onClick={onMyPageButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onMyPageButtonClickHandler)} role='button' aria-label='마이페이지로 이동' tabIndex={0}>{'마이페이지'}</div>
        )
        //    render: 로그인 버튼 컴포넌트 렌더링  //
        return(
            <div className='black-button' onClick={onSignInButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onSignInButtonClickHandler)} role='button' aria-label='로그인 페이지로 이동' tabIndex={0}>{'로그인'}</div>
        )
    };

    //  component: 업로드 버튼 컴포넌트 //
    const UploadButton = () => {
        //  state: 게시물 번호 path variable 상태 //
        const { boardNumber } = useParams();
        //  state: 게시물 상태 //
        const { title, content, category, boardImageFileList, resetBoard } = useBoardStore();
        //  state: EditorState 상태 //
        const { editorState, setEditorState } = useEditorStore();

        //  function: 네비게이트 함수 //
        const navigator = useNavigate();

        //  function: post board response 처리 함수 //
        const postBoardResponse = (responseBody: PostBoardResponseDto | ResponseDto | null) => {
            if (!responseBody) return;
            const { code } = responseBody;
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code === 'AF' || code === 'NU') navigate(AUTH_PATH());
            if (code === 'VF') alert('제목과 내용을 입력해주세요.');
            if (code !== 'SU') return;

            resetBoard();
            if (!loginUser) return;
            const { email } = loginUser;
            navigate(USER_PATH(email));
        }
        //  function: patch board response 처리 함수 //
        const patchBoardResponse = (responseBody: PatchBoardResponseDto | ResponseDto | null) => {
            if (!responseBody) return;
            const { code } = responseBody;
            if (code === 'AF' || code === 'NU' || code == 'NB' || code == 'NP') navigate(AUTH_PATH());
            if (code === 'VF') alert('제목과 내용은 필수입니다.');
            if (code === 'DBE') alert('데이터베이스 오류입니다.');
            if (code !== 'SU') return;

            if (!boardNumber) return;
            navigate(BOARD_PATH() + '/' + BOARD_DETAIL_PATH(category, boardNumber));
        }

        //  event handler: 업로드 버튼 클릭 이벤트 처리 함수 //
        const onUploadButtonClickHandler = async () => {
            // eslint-disable-next-line no-restricted-globals
            if (!confirm("게시글을 업로드 하시겠습니까?")) return;

            const accessToken = cookies.accessToken;
            if (!accessToken) {
                alert("로그인 정보가 만료되었습니다.");
                navigator(AUTH_PATH());
                return;
            }

            const boardImageList: string[] = [];

            for (const file of boardImageFileList) {
                const data = new FormData();
                data.append('file', file.file);

                const url = await fileUploadRequest(data);
                if (url) boardImageList.push(url);
            }

            // 기존 editorState에서 이미지 URL을 실제 S3 URL로 교체
            let newEditorState = editorState;
            const contentState = newEditorState.getCurrentContent();
            const blockMap = contentState.getBlockMap();

            let imageIndex = 0;

            blockMap.forEach((block: any) => {
                const entityKey = block.getEntityAt(0);

                if (entityKey) {
                    const entity = contentState.getEntity(entityKey);
                    const entityData = entity.getData();

                    if (entity.getType() === 'IMAGE' && boardImageList[imageIndex]) {
                        const newImageUrl = boardImageList[imageIndex]; // index로 접근
                        imageIndex++; // 다음 URL로 이동

                        const newEntityData = {...entityData, src: newImageUrl};
                        const contentStateWithUpdatedEntity = contentState.replaceEntityData(entityKey, newEntityData);

                        newEditorState = EditorState.push(
                            newEditorState,
                            contentStateWithUpdatedEntity,
                            'apply-entity'
                        );
                    }
                }
            });

            // JSON 변환
            const updatedContent = JSON.stringify(
                convertToRaw(newEditorState.getCurrentContent())
            );

            // 작성 페이지 여부 확인 후 요청 전송
            const isWritePage = pathname === BOARD_PATH() + '/' + BOARD_WRITE_PATH();
            if (isWritePage) {
                const requestBody: PostBoardRequestDto = {
                    title,
                    content: updatedContent, // 업데이트된 content 반영
                    category,
                    boardImageList,
                };
                postBoardRequest(requestBody, accessToken).then(postBoardResponse);
            } else {
                if (!boardNumber) return;
                const requestBody: PatchBoardRequestDto = {
                    title,
                    content: updatedContent,
                    category,
                    boardImageList,
                };
                patchBoardRequest(boardNumber, requestBody, accessToken).then(patchBoardResponse);
            }
        };

        //  render: 업로드 버튼 컴포넌트 렌더링 //
        if (title && content && category)
        return <div className='black-button' onClick={onUploadButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onUploadButtonClickHandler)} role='button' aria-label='게시글 업로드' tabIndex={0}>{'업로드'}</div>;
        //  render: 업로드 불가 버튼 컴포넌트 렌더링 //
        return <div className='disable-button' role='button' aria-label='업로드 불가 (제목, 내용, 카테고리를 입력해주세요)' aria-disabled='true'>{'업로드'}</div>;
    }
    //  render: 헤더 레이아웃 렌더링 //
    return (
        <header id='header'>
            <div className='header-container'>
                <nav className='header-left-box' aria-label='주요 네비게이션'>
                    <div className='header-leftOne-box' onClick={onLogoClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onLogoClickHandler)} role='button' aria-label='홈으로 이동' tabIndex={0}>
                        <div className='icon-box'>
                            <div className='icon logo-dark-icon' role='img' aria-label='DevHub 로고'></div>
                        </div>
                        <div className='header-logo'>{'DevHub'}</div>
                    </div>
                    <div className='header-leftTwo-box' onClick={onYoutubeClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onYoutubeClickHandler)} role='button' aria-label='DevTube로 이동' tabIndex={0}>
                        <div className='icon-box'>
                            <div className='icon logo-youtube-icon' role='img' aria-label='YouTube 아이콘'></div>
                        </div>
                        <div className='header-logo'>{'DevTube'}</div>
                    </div>
                    <div className='header-leftThree-box' onClick={onTrendClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onTrendClickHandler)} role='button' aria-label='트렌드 페이지로 이동' tabIndex={0}>
                        <div className='icon-box'>
                            <div className='icon logo-trend-icon' role='img' aria-label='트렌드 아이콘'></div>
                        </div>
                        <div className='header-logo'>{'Trend'}</div>
                    </div>
                </nav>
                <div className='header-right-box'>
                    {/*{(isMainPage || isSearchPage || isBoardDetailPage) && <SearchButton />}*/}
                    {/*{(isMainPage || isSearchPage || isBoardDetailPage || isUserPage || isYoutubePage) && <MyPageButton />}*/}
                    {!isYoutubePage && <SearchButton />}
                    <ThemeToggle />
                    <MyPageButton />
                    {(isBoardWritePage || isBoardUpdatePage) && <UploadButton />}
                </div>
            </div>
        </header>
    )
}