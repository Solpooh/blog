import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategoryStatsRequest } from 'apis';
import { GetCategoryStatsResponseDto } from 'apis/response/youtube';
import './style.css';

interface MainCategoryStats {
    mainCategory: string;
    displayName: string;
    count: number;
    subCategories: SubCategoryStats[];
}

interface SubCategoryStats {
    subCategory: string;
    displayName: string;
    count: number;
}

export default function CategoryDropdown() {
    const [isOpen, setIsOpen] = useState(false);
    const [categories, setCategories] = useState<MainCategoryStats[]>([]);
    const [selectedMain, setSelectedMain] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const navigate = useNavigate();

    // 카테고리 통계 로드
    useEffect(() => {
        if (categories.length === 0 && !isLoading) {
            loadCategoryStats();
        }
    }, []);

    const loadCategoryStats = async () => {
        setIsLoading(true);
        const response = await getCategoryStatsRequest();
        if (response && 'data' in response) {
            const { categories: categoryData } = (response as GetCategoryStatsResponseDto).data;
            setCategories(categoryData);
            // 기본으로 첫 번째 카테고리 선택
            if (categoryData.length > 0) {
                setSelectedMain(categoryData[0].mainCategory);
            }
        }
        setIsLoading(false);
    };

    // Hover 진입 시 드롭다운 열기
    const handleMouseEnter = () => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        setIsOpen(true);
    };

    // Hover 벗어날 때 약간의 지연 후 닫기
    const handleMouseLeave = () => {
        timeoutRef.current = setTimeout(() => {
            setIsOpen(false);
        }, 150);
    };

    const handleMainCategoryHover = (mainCategory: string) => {
        setSelectedMain(mainCategory);
    };

    const handleMainCategoryClick = (mainCategory: string) => {
        navigate(`/youtube/category/${mainCategory}`);
        setIsOpen(false);
    };

    const handleSubCategoryClick = (mainCategory: string, subCategory: string) => {
        navigate(`/youtube/category/${mainCategory}/${subCategory}`);
        setIsOpen(false);
    };

    const selectedCategory = categories.find(cat => cat.mainCategory === selectedMain);

    return (
        <div
            className="category-mega-menu"
            ref={dropdownRef}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >
            <button className={`category-mega-button ${isOpen ? 'active' : ''}`}>
                <span className="category-mega-text">Category</span>
                <span className={`category-mega-arrow ${isOpen ? 'open' : ''}`}>
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </span>
            </button>

            {isOpen && (
                <div className="category-mega-dropdown">
                    {isLoading ? (
                        <div className="category-mega-loading">
                            <span>Loading...</span>
                        </div>
                    ) : categories.length === 0 ? (
                        <div className="category-mega-empty">
                            <span>No categories available</span>
                        </div>
                    ) : (
                        <div className="category-mega-layout">
                            {/* 왼쪽: Main Categories */}
                            <div className="category-main-list">
                                {categories.map((category) => (
                                    <div
                                        key={category.mainCategory}
                                        className={`category-main-item ${selectedMain === category.mainCategory ? 'active' : ''}`}
                                        onMouseEnter={() => handleMainCategoryHover(category.mainCategory)}
                                        onClick={() => handleMainCategoryClick(category.mainCategory)}
                                    >
                                        <span className="category-main-name">{category.displayName}</span>
                                        <span className="category-main-count">({category.count.toLocaleString()})</span>
                                    </div>
                                ))}
                            </div>

                            {/* 오른쪽: Sub Categories */}
                            <div className="category-sub-panel">
                                {selectedCategory && (
                                    <>
                                        <div className="category-sub-header">
                                            <h3>{selectedCategory.displayName}</h3>
                                            <button
                                                className="category-view-all"
                                                onClick={() => handleMainCategoryClick(selectedCategory.mainCategory)}
                                            >
                                                전체보기
                                            </button>
                                        </div>
                                        <div className="category-sub-grid">
                                            {selectedCategory.subCategories.map((sub) => (
                                                <div
                                                    key={sub.subCategory}
                                                    className="category-sub-item"
                                                    onClick={() => handleSubCategoryClick(selectedCategory.mainCategory, sub.subCategory)}
                                                >
                                                    <span className="category-sub-name">{sub.displayName}</span>
                                                    <span className="category-sub-count">({sub.count.toLocaleString()})</span>
                                                </div>
                                            ))}
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
