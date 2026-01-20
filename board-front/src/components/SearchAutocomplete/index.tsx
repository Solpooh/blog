// component: 검색 자동완성 컴포넌트
import React, { useState, useEffect, useRef, ChangeEvent, KeyboardEvent } from 'react';
import { Search, Clock, X, TrendingUp } from 'lucide-react';
import './style.css';

interface SearchAutocompleteProps {
    value: string;
    onChange: (value: string) => void;
    onSearch: (value: string) => void;
    placeholder?: string;
}

interface SearchSuggestion {
    text: string;
    type: 'history' | 'trending' | 'suggestion';
}

export default function SearchAutocomplete({
    value,
    onChange,
    onSearch,
    placeholder = '검색어를 입력해주세요.'
}: SearchAutocompleteProps) {
    // state: 자동완성 제안 목록
    const [suggestions, setSuggestions] = useState<SearchSuggestion[]>([]);
    // state: 드롭다운 표시 여부
    const [isOpen, setIsOpen] = useState(false);
    // state: 선택된 제안 인덱스 (키보드 네비게이션용)
    const [selectedIndex, setSelectedIndex] = useState(-1);
    // state: 최근 검색어 히스토리
    const [history, setHistory] = useState<string[]>([]);

    // ref: 컴포넌트 외부 클릭 감지용
    const wrapperRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);
    const debounceTimer = useRef<NodeJS.Timeout | null>(null);

    // 인기 검색어 (실제로는 API에서 가져와야 함)
    const trendingSearches = [
        '클로드 코드',
        'React',
        'TypeScript',
        'Spring Boot',
        'Next.js'
    ];

    // effect: 최근 검색어 로드
    useEffect(() => {
        const savedHistory = localStorage.getItem('youtube-search-history');
        if (savedHistory) {
            try {
                setHistory(JSON.parse(savedHistory));
            } catch (e) {
                console.error('Failed to parse search history:', e);
            }
        }
    }, []);

    // effect: 외부 클릭 감지
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setIsOpen(false);
                setSelectedIndex(-1);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // effect: 검색어 변경 시 자동완성 제안 업데이트
    useEffect(() => {
        if (!value.trim()) {
            // 검색어가 없으면 최근 검색어와 인기 검색어 표시
            const suggestions: SearchSuggestion[] = [
                ...history.slice(0, 5).map(h => ({ text: h, type: 'history' as const })),
                ...trendingSearches.slice(0, 5).map(t => ({ text: t, type: 'trending' as const }))
            ];
            setSuggestions(suggestions);
            return;
        }

        // Debounce를 적용하여 API 호출 최적화
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }

        debounceTimer.current = setTimeout(() => {
            // 실제로는 API 호출로 관련 검색어를 가져와야 함
            // 여기서는 최근 검색어와 인기 검색어에서 필터링
            const filtered = [
                ...history.filter(h => h.toLowerCase().includes(value.toLowerCase())),
                ...trendingSearches.filter(t => t.toLowerCase().includes(value.toLowerCase()))
            ].map(text => ({ text, type: 'suggestion' as const }));

            setSuggestions(filtered.slice(0, 8));
        }, 300);

        return () => {
            if (debounceTimer.current) {
                clearTimeout(debounceTimer.current);
            }
        };
    }, [value, history]);

    // function: 검색어 히스토리에 추가
    const addToHistory = (searchText: string) => {
        const newHistory = [
            searchText,
            ...history.filter(h => h !== searchText)
        ].slice(0, 10); // 최대 10개까지만 저장

        setHistory(newHistory);
        localStorage.setItem('youtube-search-history', JSON.stringify(newHistory));
    };

    // function: 히스토리 항목 삭제
    const removeFromHistory = (searchText: string, e: React.MouseEvent) => {
        e.stopPropagation();
        const newHistory = history.filter(h => h !== searchText);
        setHistory(newHistory);
        localStorage.setItem('youtube-search-history', JSON.stringify(newHistory));
        setSuggestions(suggestions.filter(s => s.text !== searchText));
    };

    // event handler: 입력 변경
    const onInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    // event handler: 포커스
    const onInputFocus = () => {
        setIsOpen(true);
    };

    // event handler: 제안 항목 클릭
    const onSuggestionClick = (suggestion: SearchSuggestion) => {
        onChange(suggestion.text);
        addToHistory(suggestion.text);
        onSearch(suggestion.text);
        setIsOpen(false);
        setSelectedIndex(-1);
    };

    // event handler: 키보드 이벤트
    const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (!isOpen && e.key !== 'Enter') {
            setIsOpen(true);
            return;
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev =>
                    prev < suggestions.length - 1 ? prev + 1 : prev
                );
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
                    onSuggestionClick(suggestions[selectedIndex]);
                } else if (value.trim()) {
                    addToHistory(value);
                    onSearch(value);
                    setIsOpen(false);
                }
                break;
            case 'Escape':
                setIsOpen(false);
                setSelectedIndex(-1);
                inputRef.current?.blur();
                break;
        }
    };

    // function: 아이콘 렌더링
    const renderIcon = (type: 'history' | 'trending' | 'suggestion') => {
        switch (type) {
            case 'history':
                return <Clock size={16} className="suggestion-icon" />;
            case 'trending':
                return <TrendingUp size={16} className="suggestion-icon trending" />;
            default:
                return <Search size={16} className="suggestion-icon" />;
        }
    };

    return (
        <div className="search-autocomplete-wrapper" ref={wrapperRef}>
            <div className="search-input-container">
                <Search size={20} className="search-icon" aria-hidden="true" />
                <input
                    ref={inputRef}
                    type="text"
                    className="search-input-enhanced"
                    placeholder={placeholder}
                    value={value}
                    onChange={onInputChange}
                    onFocus={onInputFocus}
                    onKeyDown={onKeyDown}
                    aria-label="검색어 입력"
                    role="searchbox"
                    aria-autocomplete="list"
                    aria-controls="search-suggestions"
                    aria-expanded={isOpen}
                />
                {value && (
                    <button
                        className="clear-button"
                        onClick={() => onChange('')}
                        aria-label="검색어 지우기"
                    >
                        <X size={18} />
                    </button>
                )}
            </div>

            {isOpen && suggestions.length > 0 && (
                <div className="suggestions-dropdown" id="search-suggestions" role="listbox">
                    {!value.trim() && history.length > 0 && (
                        <div className="suggestions-section">
                            <div className="suggestions-header">
                                <span>최근 검색</span>
                                <button
                                    className="clear-all-button"
                                    onClick={() => {
                                        setHistory([]);
                                        localStorage.removeItem('youtube-search-history');
                                        setSuggestions(suggestions.filter(s => s.type !== 'history'));
                                    }}
                                >
                                    전체 삭제
                                </button>
                            </div>
                        </div>
                    )}

                    {suggestions.map((suggestion, index) => (
                        <div
                            key={`${suggestion.type}-${suggestion.text}-${index}`}
                            className={`suggestion-item ${index === selectedIndex ? 'selected' : ''}`}
                            onClick={() => onSuggestionClick(suggestion)}
                            onMouseEnter={() => setSelectedIndex(index)}
                            role="option"
                            aria-selected={index === selectedIndex}
                        >
                            {renderIcon(suggestion.type)}
                            <span className="suggestion-text">{suggestion.text}</span>
                            {suggestion.type === 'history' && (
                                <button
                                    className="remove-history-button"
                                    onClick={(e) => removeFromHistory(suggestion.text, e)}
                                    aria-label="검색 기록 삭제"
                                >
                                    <X size={14} />
                                </button>
                            )}
                        </div>
                    ))}

                    {!value.trim() && (
                        <div className="suggestions-section">
                            <div className="suggestions-header">
                                <span>인기 검색어</span>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
