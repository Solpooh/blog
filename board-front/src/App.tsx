import './App.css';
import {Navigate, Route, Routes} from 'react-router-dom';
import Container from 'layouts/Container';
import {
    AUTH_PATH,
    BOARD_DETAIL_PATH,
    BOARD_PATH, BOARD_UPDATE_PATH,
    BOARD_WRITE_PATH,
    MAIN_PATH,
    SEARCH_PATH,
    USER_PATH, YOUTUBE_PATH, YOUTUBE_SEARCH_PATH, YOUTUBE_TREND_PATH,
    ADMIN_PATH, ADMIN_CHANNEL_PATH, ADMIN_VIDEO_PATH
} from './constants';
import {Cookies, useCookies} from 'react-cookie';
import React, {lazy, Suspense, useEffect} from 'react';
import {useLoginUserStore} from './stores';
import {getSignInUserRequest} from './apis';
import {GetSignInUserResponseDto} from './apis/response/user';
import {ResponseDto} from './apis/response';
import {User} from './types/interface';
import ScrollToTop from './components/ScrollToTop';
import TopButton from './components/TopButton';

// 코드 스플리팅: React.lazy로 컴포넌트 동적 import
const Main = lazy(() => import('views/Main'));
const Authentication = lazy(() => import('views/Authentication'));
const Search = lazy(() => import('views/Search'));
const UserP = lazy(() => import('views/User'));
const BoardWrite = lazy(() => import('views/Board/Write'));
const BoardUpdate = lazy(() => import('views/Board/Update'));
const BoardDetail = lazy(() => import('views/Board/Detail'));
const Youtube = lazy(() => import('./views/Youtube'));
const YoutubeTrend = lazy(() => import("./views/YoutubeTrend"));
const Admin = lazy(() => import('./views/Admin'));
const AdminChannel = lazy(() => import('./views/Admin/Channel'));
const AdminVideo = lazy(() => import('./views/Admin/Video'));


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
        const loginUser: User = { ...responseBody.data as GetSignInUserResponseDto };
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
        <>
            <ScrollToTop />
            <Suspense fallback={
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100vh',
                    fontSize: '18px',
                    color: '#888'
                }}>
                    로딩 중...
                </div>
            }>
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
                    <Route path={YOUTUBE_TREND_PATH()} element={<YoutubeTrend />}/>
                    <Route path={ADMIN_PATH()} element={<Admin />}>
                        <Route path={ADMIN_CHANNEL_PATH()} element={<AdminChannel />} />
                        <Route path={ADMIN_VIDEO_PATH()} element={<AdminVideo />} />
                    </Route>
                    <Route path='*' element={<h1>404 NOT FOUND</h1>}/>
                </Route>
            </Routes>
            </Suspense>
            <TopButton />
        </>
    );
}

export default App;
