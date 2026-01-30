import React, {useEffect} from 'react';
import {Link, Outlet, useLocation, useNavigate} from 'react-router-dom';
import './style.css';
import {Video, Users, Settings} from 'lucide-react';
import {useLoginUserStore} from 'stores';
import {MAIN_PATH} from 'constants/index';

export default function Admin() {
    const location = useLocation();
    const navigate = useNavigate();
    const { loginUser } = useLoginUserStore();

    // 관리자 권한 확인
    useEffect(() => {
        if (!loginUser || loginUser.role !== 'ADMIN') {
            alert('관리자 권한이 필요합니다.');
            navigate(MAIN_PATH());
        }
    }, [loginUser, navigate]);

    // 권한이 없으면 렌더링하지 않음
    if (!loginUser || loginUser.role !== 'ADMIN') {
        return null;
    }

    const menuItems = [
        {
            path: '/admin/channel',
            label: '채널 관리',
            icon: <Users size={20}/>,
        },
        {
            path: '/admin/video',
            label: '비디오 관리',
            icon: <Video size={20}/>,
        },
        {
            path: '/admin/settings',
            label: '시스템 설정',
            icon: <Settings size={20}/>,
            disabled: true, // 추후 구현
        },
    ];

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <div className="admin-sidebar-header">
                    <h2>관리자 페이지</h2>
                </div>
                <nav className="admin-nav">
                    {menuItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.disabled ? '#' : item.path}
                            className={`admin-nav-item ${
                                location.pathname === item.path ? 'active' : ''
                            } ${item.disabled ? 'disabled' : ''}`}
                            onClick={(e) => item.disabled && e.preventDefault()}
                        >
                            {item.icon}
                            <span>{item.label}</span>
                            {item.disabled && <span className="coming-soon">준비중</span>}
                        </Link>
                    ))}
                </nav>
            </aside>
            <main className="admin-main">
                <Outlet/>
            </main>
        </div>
    );
}
