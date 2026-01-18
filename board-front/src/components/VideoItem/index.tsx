import {VideoListItem} from 'types/interface';
import './style.css';
import {deleteVideoRequest, getTranscriptRequest} from 'apis';
import {DeleteVideoResponseDto, GetTranscriptResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import React, {useState} from "react";
interface Props {
    videoItem: VideoListItem;
}

export default function VideoItem({ videoItem }: Props) {

    const { videoId, title, thumbnail, channelTitle, customUrl, channelThumbnail, publishedAt, viewCount
    , commentCount, likeCount, isShort } = videoItem;
    const formattedDate = new Date(publishedAt).toISOString().split("T")[0]; // 'yyyy-MM-dd' 포맷

    // iframe 전용 모달 상태
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [showIframe, setShowIframe] = useState(false);

    // transcript 전용 모달 상태
    const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
    const [isSummaryLoading, setIsSummaryLoading] = useState(false);
    const [summaryText, setSummaryText] = useState<string | null>(null);


    //  function: 이미지 삭제 응답 함수  //
    const deleteVideoResponse = (responseBody: DeleteVideoResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code === 'NV') alert('존재하지 않는 비디오입니다.');
        if (code !== 'SU') return;
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
    //  function: Transcript 응답 함수 //
    const getTranscriptResponse = (responseBody: GetTranscriptResponseDto | ResponseDto | null) => {
        setIsSummaryLoading(false);

        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const {transcript} = (responseBody as GetTranscriptResponseDto).data;

        setSummaryText(transcript);
    };

    //  event handler: 이미지 오류 이벤트 처리  //
    const onImageLoadHandler = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
        const img = e.currentTarget;
        // 유튜브의 기본 썸네일은 120x90 회색 이미지
        if (img.naturalWidth === 120 && img.naturalHeight === 90) {
            console.log('이미지 로드 실패, 삭제 API 호출');
            deleteVideoRequest(videoId).then(deleteVideoResponse);
        }
    }


    return (
        <div className="video-card">
            <a
                href={`https://www.youtube.com/${customUrl}`}
                target="_blank"
                rel="noopener noreferrer"
                className="channel-info-link"
            >
                <div className="channel-info">
                    <img src={channelThumbnail} alt={channelTitle} className="channel-thumbnail" loading="lazy"/>
                    <p className="video-channel">{channelTitle}</p>
                </div>
            </a>
            <div className="video-info-link" onClick={openModal}>
                <img
                    key={videoId}
                    alt={title}
                    src={thumbnail}
                    className="video-thumbnail"
                    loading="lazy"
                    onLoad={onImageLoadHandler}
                />
                <div className="video-info">
                    <div className="video-meta">
                        <span className="video-published-at">{formattedDate}</span>
                        <span className="video-view-count">{viewCount !== null && viewCount !== undefined ? `조회수 ${viewCount.toLocaleString()}회` : "조회수 -"}</span>
                    </div>
                    <h3 className="video-title">{title}</h3>
                    <div className="video-item-counts">
                        {`좋아요 ${likeCount} 댓글 ${commentCount}`}
                        <button className="summary-button" onClick={(e: React.MouseEvent) => {
                            e.stopPropagation();
                            setIsSummaryModalOpen(true);
                            setIsSummaryLoading(true);
                            setSummaryText(null);

                            getTranscriptRequest(videoId).then(getTranscriptResponse);
                        }}>
                            📋영상 요약하기
                        </button>
                    </div>
                </div>
            </div>

            {isSummaryModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content summary-modal">
                        <button onClick={() => setIsSummaryModalOpen(false)}>
                            닫기 X
                        </button>

                        {isSummaryLoading && (
                            <div className="summary-loading">
                                <div className="spinner" />
                                <p>AI가 영상을 요약 중입니다...</p>
                            </div>
                        )}

                        {!isSummaryLoading && summaryText && (
                            <div className="summary-result">
                                <h3>📋 영상 요약</h3>
                                <pre>{summaryText}</pre>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button className="modal-close-btn" onClick={closeModal}>
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
                </div>
            )}
        </div>
    );
}