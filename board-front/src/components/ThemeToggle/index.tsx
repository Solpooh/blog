import React from 'react';
import { Moon, Sun } from 'lucide-react';
import { useThemeStore } from '../../stores';
import './style.css';

export default function ThemeToggle() {
    const { resolvedTheme, toggleTheme } = useThemeStore();
    const isDark = resolvedTheme === 'dark';

    return (
        <button
            className={`theme-toggle ${isDark ? 'dark' : 'light'}`}
            onClick={toggleTheme}
            aria-label={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}
            title={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}
        >
            <div className="theme-toggle-track">
                <div className="theme-toggle-thumb">
                    {isDark ? (
                        <Moon size={14} className="theme-icon moon" />
                    ) : (
                        <Sun size={14} className="theme-icon sun" />
                    )}
                </div>
            </div>
        </button>
    );
}
