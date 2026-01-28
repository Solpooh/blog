import React from 'react';
import {Link, Outlet, useLocation} from 'react-router-dom';
import './style.css';
import {Video, Users, Settings} from 'lucide-react';

export default function Admin() {
    const location = useLocation();

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
