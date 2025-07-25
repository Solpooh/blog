import './App.css';
import {Navigate, Route, Routes} from 'react-router-dom';
import Main from 'views/Main';
import Authentication from 'views/Authentication';
import Search from 'views/Search';
import UserP from 'views/User';
import BoardWrite from 'views/Board/Write';
import BoardUpdate from 'views/Board/Update';
import BoardDetail from 'views/Board/Detail';
import Container from 'layouts/Container';
import {
    AUTH_PATH,
    BOARD_DETAIL_PATH,
    BOARD_PATH, BOARD_UPDATE_PATH,
    BOARD_WRITE_PATH,
    MAIN_PATH,
    SEARCH_PATH,
    USER_PATH, YOUTUBE_PATH, YOUTUBE_SEARCH_PATH
} from './constants';
import {Cookies, useCookies} from 'react-cookie';
import React, {useEffect} from 'react';
import {useLoginUserStore} from './stores';
import {getSignInUserRequest} from './apis';
import {GetSignInUserResponseDto} from './apis/response/user';
import {ResponseDto} from './apis/response';
import {User} from './types/interface';
import Youtube from './views/Youtube';


//  component: Application 컴포넌트 //
function App() {

    //  state: 로그인 유저 전역 상태 //
    const { setLoginUser, resetLoginUser } = useLoginUserStore();
    //  state: cookie 상태  //
    const [cookies, setCookie] = useCookies();

    const getSignInUserResponse = (responseBody: GetSignInUserResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'AF' || code === 'NU' || code === 'DBE') {
            resetLoginUser();
            return;
        }
        const loginUser: User = { ...responseBody as GetSignInUserResponseDto };
        setLoginUser(loginUser);
    }

    //  effect: accessToken cookie 값이 변경될 때마다 실행할 함수 //
    useEffect(() => {
        if (!cookies.accessToken) {
            resetLoginUser();
            return;
        }
        getSignInUserRequest(cookies.accessToken).then(getSignInUserResponse);

    }, [cookies.accessToken]);

    //  render: Application 컴포넌트 렌더링 //
    //  description: 메인 화면 : '/' - Main //
    //  description: 로그인 + 회원가입 화면 : '/auth' - Authentication //
    //  description: 검색 화면 : '/search/:searchWord' - Search //
    //  description: 유저 페이지 : '/user/:userEmail' - User //
    //  description: 게시물 상세보기 : '/board/detail/:boardNumber' - BoardDetail //
    //  description: 게시물 작성하기 : '/board/write' - BoardWrite //
    //  description: 게시물 수정하기 : '/board/update/:boardNumber' - BoardUpdate //
    //  description: 유튜브 비디오 화면 : '/youtube' - Youtube //
    return (
        <Routes>
            <Route element={<Container/>}>
                {/* 기본 접속 시 all 카테고리로 리다이렉트 */}
                <Route path="/" element={<Main />} />

                {/* 게시글 카테고리 별 메인 목록 */}
                <Route path={MAIN_PATH(':category')} element={<Main />} />
                <Route path={AUTH_PATH()} element={<Authentication/>}/>
                <Route path={SEARCH_PATH(':searchWord')} element={<Search/>}/>
                <Route path={USER_PATH(':userEmail')} element={<UserP/>}/>x
                <Route path={BOARD_PATH()}>
                    <Route path={BOARD_WRITE_PATH()} element={<BoardWrite/>}/>
                    <Route path={BOARD_DETAIL_PATH(':category', ':boardNumber')} element={<BoardDetail/>}/>
                    <Route path={BOARD_UPDATE_PATH(':boardNumber')} element={<BoardUpdate/>}/>
                </Route>
                <Route path={YOUTUBE_PATH()} element={<Youtube/>}/>
                <Route path={YOUTUBE_SEARCH_PATH(':searchWord')} element={<Youtube />} />
                <Route path='*' element={<h1>404 NOT FOUND</h1>}/>
            </Route>
        </Routes>
    );
}

export default App;
