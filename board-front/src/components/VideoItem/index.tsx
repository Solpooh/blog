import {VideoListItem} from 'types/interface';
import './style.css';
import {deleteVideoRequest, getTranscriptRequest} from 'apis';
import {DeleteVideoResponseDto, GetTranscriptResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import React, {useState, useRef, useCallback, useEffect} from "react";
import {createPortal} from 'react-dom';
import {Play, Eye, ThumbsUp, MessageCircle, Sparkles, Zap, RefreshCw, AlertCircle, Clock, CheckCircle2, Download, Brain} from 'lucide-react';
import ResponseCode from 'types/enum/response-code.enum';
import {useCookies} from 'react-cookie';

// Transcript 상태 타입
type TranscriptStatus = 'idle' | 'loading' | 'polling' | 'success' | 'error' | 'unavailable';

// 처리 단계 타입
type ProcessingPhase = 'collecting' | 'analyzing' | 'finalizing';

interface TranscriptState {
    status: TranscriptStatus;
    text: string | null;
    errorMessage: string | null;
    canRetry: boolean;
}

// 처리 단계 설정
const PROCESSING_PHASES: { phase: ProcessingPhase; label: string; duration: number; icon: 'download' | 'brain' | 'check' }[] = [
    { phase: 'collecting', label: '정확한 자막 데이터를 수집하고 있습니다', duration: 5, icon: 'download' },
    { phase: 'analyzing', label: 'AI가 영상 내용을 분석하고 있습니다', duration: 5, icon: 'brain' },
    { phase: 'finalizing', label: '요약을 마무리하고 있습니다', duration: 5, icon: 'check' },
];

const TOTAL_ESTIMATED_TIME = PROCESSING_PHASES.reduce((sum, p) => sum + p.duration, 0); // 15초
interface Props {
    videoItem: VideoListItem;
}

// 숫자를 간결하게 포맷팅 (예: 1200000 -> 120만)
const formatCount = (count: number | null | undefined): string => {
    if (count === null || count === undefined) return '-';
    if (count >= 100000000) return `${(count / 100000000).toFixed(1)}억`;
    if (count >= 10000) return `${(count / 10000).toFixed(1)}만`;
    if (count >= 1000) return `${(count / 1000).toFixed(1)}천`;
    return count.toLocaleString();
};

// 상대 시간 계산 (예: 3일 전)
const getRelativeTime = (dateString: string): string => {
    const now = new Date();
    const date = new Date(dateString);
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return '오늘';
    if (diffDays === 1) return '어제';
    if (diffDays < 7) return `${diffDays}일 전`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}주 전`;
    if (diffDays < 365) return `${Math.floor(diffDays / 30)}개월 전`;
    return `${Math.floor(diffDays / 365)}년 전`;
};

const VideoItem = React.memo(({ videoItem }: Props) => {

    const { videoId, title, thumbnail, channelTitle, customUrl, channelThumbnail, publishedAt, viewCount
    , commentCount, likeCount, isShort } = videoItem;
    const formattedDate = new Date(publishedAt).toISOString().split("T")[0];
    const relativeTime = getRelativeTime(publishedAt);

    // 진행 바 너비 계산 (viewCount 기반, 최대 100만 조회수 기준)
    const progressWidth = Math.min((viewCount || 0) / 1000000 * 100, 100);

    // accessToken 가져오기
    const [cookies] = useCookies();

    // iframe 전용 모달 상태
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [showIframe, setShowIframe] = useState(false);

    // transcript 전용 모달 상태
    const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
    const [transcriptState, setTranscriptState] = useState<TranscriptState>({
        status: 'idle',
        text: null,
        errorMessage: null,
        canRetry: true
    });

    // 카운트다운 & 진행 상태
    const [countdown, setCountdown] = useState(TOTAL_ESTIMATED_TIME);
    const [currentPhase, setCurrentPhase] = useState<ProcessingPhase>('collecting');
    const [elapsedTime, setElapsedTime] = useState(0);
    const countdownIntervalRef = useRef<NodeJS.Timeout | null>(null);

    // 폴링 관련 ref
    const pollingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const pollingCountRef = useRef(0);
    const MAX_POLLING_COUNT = 30; // 최대 30회 (약 90초)
    const POLLING_INTERVAL = 3000; // 3초

    // 삭제된 비디오 표시 상태
    const [isDeleted, setIsDeleted] = useState(false);

    //  function: 이미지 삭제 응답 함수  //
    const deleteVideoResponse = (responseBody: DeleteVideoResponseDto | ResponseDto | null) => {
        if (!responseBody) {
            console.error('삭제 API 응답 없음');
            return;
        }
        const { code } = responseBody;
        console.log('삭제 API 응답 코드:', code);

        if (code === 'DBE') {
            console.error('데이터베이스 오류');
            return;
        }
        if (code === 'NV') {
            console.error('존재하지 않는 비디오');
            return;
        }
        if (code === 'SU') {
            console.log('비디오 삭제 성공, 화면에서 제거');
            setIsDeleted(true);
        }
    }

    //  function: Modal 제어 함수 //
    const openModal = () => {
        setIsModalOpen(true);

        setTimeout(() => {
            setShowIframe(true);
        }, 100); // 모달 UI가 먼저 뜨도록 시간 확보
    }
    const closeModal = () => {
        setShowIframe(false);
        setIsModalOpen(false);
    }
    // 카운트다운 정리
    const clearCountdown = useCallback(() => {
        if (countdownIntervalRef.current) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
        }
    }, []);

    // 폴링 정리
    const clearPolling = useCallback(() => {
        if (pollingTimeoutRef.current) {
            clearTimeout(pollingTimeoutRef.current);
            pollingTimeoutRef.current = null;
        }
        pollingCountRef.current = 0;
        clearCountdown();
    }, [clearCountdown]);

    // 카운트다운 시작
    const startCountdown = useCallback(() => {
        setCountdown(TOTAL_ESTIMATED_TIME);
        setElapsedTime(0);
        setCurrentPhase('collecting');
        clearCountdown();

        countdownIntervalRef.current = setInterval(() => {
            setElapsedTime(prev => {
                const newElapsed = prev + 1;

                // 단계 전환 계산
                let accumulated = 0;
                for (const phaseInfo of PROCESSING_PHASES) {
                    accumulated += phaseInfo.duration;
                    if (newElapsed <= accumulated) {
                        setCurrentPhase(phaseInfo.phase);
                        break;
                    }
                }

                return newElapsed;
            });

            setCountdown(prev => Math.max(0, prev - 1));
        }, 1000);
    }, [clearCountdown]);

    // 컴포넌트 언마운트 시 정리
    useEffect(() => {
        return () => {
            clearPolling();
            clearCountdown();
        };
    }, [clearPolling, clearCountdown]);

    // Modal 열릴 때 body 스크롤 방지
    useEffect(() => {
        if (isModalOpen || isSummaryModalOpen) {
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
            document.body.style.overflow = 'hidden';
            document.body.style.paddingRight = `${scrollbarWidth}px`;
        } else {
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
        }

        return () => {
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
        };
    }, [isModalOpen, isSummaryModalOpen]);

    // Transcript 요청 함수
    const fetchTranscript = useCallback((isInitial: boolean = false) => {
        if (isInitial) {
            startCountdown();
        }
        getTranscriptRequest(videoId).then(getTranscriptResponse);
    }, [videoId, startCountdown]);

    //  function: Transcript 응답 함수 //
    const getTranscriptResponse = useCallback((responseBody: GetTranscriptResponseDto | ResponseDto | null) => {
        if (!responseBody) {
            setTranscriptState(prev => ({
                ...prev,
                status: 'error',
                errorMessage: '서버 연결에 실패했습니다.',
                canRetry: true
            }));
            return;
        }

        const { code, message } = responseBody;

        switch (code) {
            case ResponseCode.SUCCESS:
                // 성공
                clearPolling();
                clearCountdown();
                const { transcript } = (responseBody as GetTranscriptResponseDto).data;
                setTranscriptState({
                    status: 'success',
                    text: transcript,
                    errorMessage: null,
                    canRetry: false
                });
                break;

            case ResponseCode.TRANSCRIPT_PROCESSING:
                // 처리 중 → 폴링 시작/계속
                pollingCountRef.current += 1;

                if (pollingCountRef.current >= MAX_POLLING_COUNT) {
                    // 폴링 횟수 초과
                    clearPolling();
                    setTranscriptState(prev => ({
                        ...prev,
                        status: 'error',
                        errorMessage: '처리 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.',
                        canRetry: true
                    }));
                } else {
                    // 폴링 계속 (카운트다운은 유지)
                    setTranscriptState(prev => ({
                        ...prev,
                        status: 'polling',
                        errorMessage: null
                    }));
                    pollingTimeoutRef.current = setTimeout(() => fetchTranscript(false), POLLING_INTERVAL);
                }
                break;

            case ResponseCode.TRANSCRIPT_FAILED:
                // 일시적 실패 → 재시도 가능
                clearPolling();
                clearCountdown();
                setTranscriptState(prev => ({
                    ...prev,
                    status: 'error',
                    errorMessage: message || '자막 처리에 실패했습니다. 다시 시도해주세요.',
                    canRetry: true
                }));
                break;

            case ResponseCode.TRANSCRIPT_UNAVAILABLE:
            case ResponseCode.NOT_EXISTED_VIDEO:
                // 자막 없음 → 재시도 불가
                clearPolling();
                clearCountdown();
                setTranscriptState(prev => ({
                    ...prev,
                    status: 'unavailable',
                    errorMessage: '이 영상은 자막을 제공하지 않습니다.',
                    canRetry: false
                }));
                break;

            case ResponseCode.TRANSCRIPT_RETRY_EXHAUSTED:
                // 재시도 횟수 초과 → 재시도 불가
                clearPolling();
                clearCountdown();
                setTranscriptState(prev => ({
                    ...prev,
                    status: 'unavailable',
                    errorMessage: '자막 처리에 여러 번 실패했습니다. 이 영상은 자막을 지원하지 않을 수 있습니다.',
                    canRetry: false
                }));
                break;

            default:
                // 기타 오류
                clearPolling();
                clearCountdown();
                setTranscriptState(prev => ({
                    ...prev,
                    status: 'error',
                    errorMessage: message || '알 수 없는 오류가 발생했습니다.',
                    canRetry: true
                }));
        }
    }, [clearPolling, clearCountdown, fetchTranscript]);

    // 재시도 핸들러
    const handleRetry = useCallback(() => {
        pollingCountRef.current = 0;
        setTranscriptState(prev => ({
            ...prev,
            status: 'loading',
            errorMessage: null
        }));
        fetchTranscript(true); // 카운트다운 재시작
    }, [fetchTranscript]);

    //  event handler: 이미지 오류 이벤트 처리  //
    const onImageLoadHandler = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
        const img = e.currentTarget;
        // 유튜브의 기본 썸네일은 120x90 회색 이미지
        if (img.naturalWidth === 120 && img.naturalHeight === 90) {
            console.log('이미지 로드 실패 감지 (120x90), 삭제 API 호출');

            const accessToken = cookies.accessToken;
            if (!accessToken) {
                console.warn('삭제 권한 없음: 로그인되지 않음');
                return;
            }

            deleteVideoRequest(videoId, accessToken).then(deleteVideoResponse);
        }
    }


    // 삭제된 비디오는 렌더링하지 않음
    if (isDeleted) {
        return null;
    }

    return (
        <article className="video-card">
            {/* Thumbnail Section */}
            <div className="video-thumbnail-container" onClick={openModal}>
                <img
                    key={videoId}
                    alt={`${title} 비디오 썸네일`}
                    src={thumbnail}
                    className="video-thumbnail"
                    loading="lazy"
                    onLoad={onImageLoadHandler}
                />

                {/* SHORTS Badge */}
                {isShort && (
                    <div className="shorts-badge">
                        <Zap size={12} />
                        Shorts
                    </div>
                )}

                {/* Play Button Overlay */}
                <div className="play-overlay">
                    <Play size={48} fill="white" />
                </div>
            </div>

            {/* Video Details Section - YouTube Style */}
            <div className="video-details">
                {/* Channel Thumbnail */}
                <a
                    href={`https://www.youtube.com/${customUrl}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="channel-avatar-link"
                >
                    <img
                        src={channelThumbnail}
                        alt={channelTitle}
                        className="channel-avatar"
                        loading="lazy"
                    />
                </a>

                {/* Video Info */}
                <div className="video-info">
                    {/* Title */}
                    <h3 className="video-title" onClick={openModal}>{title}</h3>

                    {/* Channel Name */}
                    <a
                        href={`https://www.youtube.com/${customUrl}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="channel-name-link"
                    >
                        <span className="channel-name">{channelTitle}</span>
                    </a>

                    {/* View Count & Time */}
                    <div className="video-meta">
                        <span>조회수 {formatCount(viewCount)}회</span>
                        <span className="meta-separator">•</span>
                        <span>{relativeTime}</span>
                    </div>

                    {/* Action Buttons */}
                    <div className="video-actions">
                    <button className="action-btn" title={`좋아요 ${formatCount(likeCount)}`}>
                        <ThumbsUp size={14} />
                        <span>{formatCount(likeCount)}</span>
                    </button>
                    <button className="action-btn" title={`댓글 ${formatCount(commentCount)}`}>
                        <MessageCircle size={14} />
                        <span>{formatCount(commentCount)}</span>
                    </button>
                    <button
                        className={`action-btn ai-btn ${transcriptState.status === 'loading' || transcriptState.status === 'polling' ? 'loading' : ''} ${transcriptState.status === 'success' ? 'has-summary' : ''} ${transcriptState.status === 'unavailable' ? 'unavailable' : ''}`}
                        disabled={transcriptState.status === 'loading' || transcriptState.status === 'polling' || transcriptState.status === 'unavailable'}
                        title={transcriptState.status === 'unavailable' ? '자막 없음' : 'AI 요약'}
                        onClick={(e: React.MouseEvent) => {
                            e.stopPropagation();

                            const { status, text } = transcriptState;

                            if (status === 'loading' || status === 'polling') return;

                            // 이미 요약이 있으면 모달만 열기
                            if (status === 'success' && text) {
                                setIsSummaryModalOpen(true);
                                return;
                            }

                            // 에러 상태면서 재시도 가능하면 재시도
                            // 또는 idle 상태면 최초 요청
                            setIsSummaryModalOpen(true);
                            setTranscriptState(prev => ({
                                ...prev,
                                status: 'loading',
                                errorMessage: null
                            }));
                            pollingCountRef.current = 0;
                            fetchTranscript(true); // 카운트다운 시작
                        }}
                    >
                        <Sparkles size={14} />
                        <span>{transcriptState.status === 'loading' || transcriptState.status === 'polling' ? '...' : 'AI'}</span>
                    </button>
                    </div>
                </div>
            </div>

            {isSummaryModalOpen && createPortal(
                <div className="summary-modal-overlay" onClick={() => {
                    // 폴링 중이 아닐 때만 모달 닫기 허용
                    if (transcriptState.status !== 'polling') {
                        setIsSummaryModalOpen(false);
                    }
                }}>
                    <div className="summary-modal" onClick={(e) => e.stopPropagation()}>
                        <button
                            className="modal-close-btn"
                            onClick={() => setIsSummaryModalOpen(false)}
                        >
                            닫기 X
                        </button>

                        {/* 로딩/폴링 상태 - 카운트다운 UI */}
                        {(transcriptState.status === 'loading' || transcriptState.status === 'polling') && (
                            <div className="summary-progress">
                                {/* 단계 표시 */}
                                <div className="progress-phases">
                                    {PROCESSING_PHASES.map((phaseInfo, index) => {
                                        const isActive = currentPhase === phaseInfo.phase;
                                        const isPast = PROCESSING_PHASES.findIndex(p => p.phase === currentPhase) > index;

                                        return (
                                            <div
                                                key={phaseInfo.phase}
                                                className={`phase-item ${isActive ? 'active' : ''} ${isPast ? 'completed' : ''}`}
                                            >
                                                <div className="phase-icon">
                                                    {isPast ? (
                                                        <CheckCircle2 size={20} />
                                                    ) : phaseInfo.icon === 'download' ? (
                                                        <Download size={20} />
                                                    ) : phaseInfo.icon === 'brain' ? (
                                                        <Brain size={20} />
                                                    ) : (
                                                        <CheckCircle2 size={20} />
                                                    )}
                                                </div>
                                                <span className="phase-label">{phaseInfo.label}</span>
                                            </div>
                                        );
                                    })}
                                </div>

                                {/* 프로그레스 바 */}
                                <div className="progress-bar-container">
                                    <div
                                        className="progress-bar-fill"
                                        style={{
                                            width: `${Math.min((elapsedTime / TOTAL_ESTIMATED_TIME) * 100, 100)}%`
                                        }}
                                    />
                                </div>

                                {/* 카운트다운 표시 */}
                                <div className="countdown-display">
                                    {countdown > 0 ? (
                                        <>
                                            <span className="countdown-number">{countdown}</span>
                                            <span className="countdown-unit">초</span>
                                            <span className="countdown-text">후 완료 예정</span>
                                        </>
                                    ) : (
                                        <span className="countdown-overtime">거의 완료되었습니다...</span>
                                    )}
                                </div>

                                {/* 신뢰성 메시지 */}
                                <p className="trust-message">
                                    yt-dlp 기반의 정확한 자막 데이터로 높은 품질의 요약을 제공합니다
                                </p>
                            </div>
                        )}

                        {/* 성공 상태 */}
                        {transcriptState.status === 'success' && transcriptState.text && (
                            <div className="summary-result">
                                <h3>📋 영상 요약</h3>
                                <pre>{transcriptState.text}</pre>
                            </div>
                        )}

                        {/* 에러 상태 (재시도 가능) */}
                        {transcriptState.status === 'error' && (
                            <div className="summary-error">
                                <AlertCircle size={48} className="error-icon" />
                                <p className="error-message">{transcriptState.errorMessage}</p>
                                {transcriptState.canRetry && (
                                    <button className="retry-btn" onClick={handleRetry}>
                                        <RefreshCw size={16} />
                                        다시 시도
                                    </button>
                                )}
                            </div>
                        )}

                        {/* 자막 불가 상태 */}
                        {transcriptState.status === 'unavailable' && (
                            <div className="summary-unavailable">
                                <AlertCircle size={48} className="unavailable-icon" />
                                <p className="unavailable-message">{transcriptState.errorMessage}</p>
                            </div>
                        )}
                    </div>
                </div>,
                document.body
            )}

            {isModalOpen && createPortal(
                <div className="video-modal-overlay" onClick={closeModal}>
                    <div className="video-modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="video-modal-close-btn" onClick={closeModal}>
                            닫기 X
                        </button>

                        {!showIframe && <div className="iframe-skeleton" />}
                        {showIframe && (
                            <iframe
                                width="100%"
                                height="500"
                                src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                                title="YouTube player"
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowFullScreen
                            ></iframe>
                        )}
                    </div>
                </div>,
                document.body
            )}
        </article>
    );
});

export default VideoItem;