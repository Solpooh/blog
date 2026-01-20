import React from 'react';
import './style.css';

//  component: 푸터 레이아웃 //
export default function Footer() {
    //  event handler: 인스타 아이콘 버튼 클릭 이벤트 처리 //
    const onInstaIconButtonClickHandler = () => {
        window.open('https://www.instagram.com');
    };
    //  event handler: 네이버 블로그 아이콘 버튼 클릭 이벤트 처리 //
    const onNaverBlogIconButtonClickHandler = () => {
        window.open('https://blog.naver.com');
    }

    //  event handler: 키보드 네비게이션 이벤트 처리 함수 //
    const onKeyDownHandler = (e: React.KeyboardEvent, callback: () => void) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            callback();
        }
    }

    //  render: 푸터 레이아웃 렌더링 //
    return (
        <footer id='footer'>
            <div className='footer-container'>
                <div className='footer-top'>
                    <div className='footer-logo-box'>
                        <div className='icon-box'>
                            <div className='icon logo-light-icon' role='img' aria-label='DevHub 로고'></div>
                        </div>
                        <div className='footer-logo-text'>{'DevHub'}</div>
                    </div>
                    <div className='footer-link-box'>
                        <div className='footer-email-link'>{'hsw5761@gmail.com'}</div>
                        <div className='icon-button' onClick={onInstaIconButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onInstaIconButtonClickHandler)} role='button' aria-label='인스타그램으로 이동' tabIndex={0}>
                            <div className='icon insta-icon' role='img' aria-label='인스타그램 아이콘'></div>
                        </div>
                        <div className='icon-button' onClick={onNaverBlogIconButtonClickHandler} onKeyDown={(e) => onKeyDownHandler(e, onNaverBlogIconButtonClickHandler)} role='button' aria-label='네이버 블로그로 이동' tabIndex={0}>
                            <div className='icon naver-blog-icon' role='img' aria-label='네이버 블로그 아이콘'></div>
                        </div>
                    </div>
                </div>
                <div className='footer-bottom'>
                    <div className='footer-copyright'>{'Copyright @ 2024 Solpooh. All Rights Reserved.'}</div>
                </div>
            </div>
        </footer>
    )
}