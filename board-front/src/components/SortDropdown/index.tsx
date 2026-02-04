import React, { useState, useRef, useEffect } from 'react';
import './style.css';

export type SortType = 'LATEST' | 'VIEWS' | 'RELEVANCE';

interface SortOption {
    value: SortType;
    label: string;
}

interface SortDropdownProps {
    value: SortType;
    onChange: (value: SortType) => void;
    includeRelevance?: boolean; // 검색 결과에서만 관련도순 표시
}

const SORT_OPTIONS: SortOption[] = [
    { value: 'LATEST', label: '최신순' },
    { value: 'VIEWS', label: '조회수순' },
];

const SORT_OPTIONS_WITH_RELEVANCE: SortOption[] = [
    { value: 'RELEVANCE', label: '관련도순' },
    { value: 'LATEST', label: '최신순' },
    { value: 'VIEWS', label: '조회수순' },
];

export default function SortDropdown({ value, onChange, includeRelevance = false }: SortDropdownProps) {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const options = includeRelevance ? SORT_OPTIONS_WITH_RELEVANCE : SORT_OPTIONS;
    const selectedOption = options.find(opt => opt.value === value) || options[0];

    // 외부 클릭 시 드롭다운 닫기
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    const handleSelect = (optionValue: SortType) => {
        onChange(optionValue);
        setIsOpen(false);
    };

    return (
        <div className="sort-dropdown" ref={dropdownRef}>
            <button
                className={`sort-dropdown-button ${isOpen ? 'active' : ''}`}
                onClick={() => setIsOpen(!isOpen)}
            >
                <span className="sort-dropdown-label">{selectedOption.label}</span>
                <span className={`sort-dropdown-arrow ${isOpen ? 'open' : ''}`}>
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </span>
            </button>

            {isOpen && (
                <div className="sort-dropdown-menu">
                    {options.map((option) => (
                        <div
                            key={option.value}
                            className={`sort-dropdown-item ${option.value === value ? 'selected' : ''}`}
                            onClick={() => handleSelect(option.value)}
                        >
                            {option.value === value && (
                                <span className="sort-dropdown-check">
                                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                                        <path d="M3.5 7L6 9.5L10.5 4.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </span>
                            )}
                            <span className="sort-dropdown-item-label">{option.label}</span>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
