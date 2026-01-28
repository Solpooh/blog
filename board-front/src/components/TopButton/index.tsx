import React, { useEffect, useState } from 'react';
import './style.css';
import { ChevronUp } from 'lucide-react';

/**
 * 페이지 맨 위로 스크롤하는 버튼
 * 스크롤이 300px 이상 내려갔을 때 표시
 */
export default function TopButton() {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        const toggleVisibility = () => {
            if (window.scrollY > 300) {
                setIsVisible(true);
            } else {
                setIsVisible(false);
            }
        };

        window.addEventListener('scroll', toggleVisibility);

        return () => {
            window.removeEventListener('scroll', toggleVisibility);
        };
    }, []);

    const scrollToTop = () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    };

    return (
        <button
            className={`top-button ${isVisible ? 'visible' : ''}`}
            onClick={scrollToTop}
            aria-label="맨 위로"
            title="맨 위로"
        >
            <ChevronUp size={24} />
            <span>TOP</span>
        </button>
    );
}
