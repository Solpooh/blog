import {VideoListItem} from 'types/interface';
import './style.css';
import {deleteVideoRequest} from 'apis';
import {DeleteVideoResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import React from "react";
interface Props {
    videoItem: VideoListItem;
}

export default function VideoItem({ videoItem }: Props) {

    const { videoId, title, thumbnail, channelTitle, customUrl, channelThumbnail, publishedAt, viewCount } = videoItem;
    const formattedDate = new Date(publishedAt).toISOString().split("T")[0]; // 'yyyy-MM-dd' 포맷

    //  function: 이미지 삭제 응답 함수  //
    const deleteVideoResponse = (responseBody: DeleteVideoResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code === 'NV') alert('존재하지 않는 비디오입니다.');
        if (code !== 'SU') return;
    }

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
            <a href={`https://youtu.be/${videoId}`} target="_blank" className="video-info-link">
                <img key={videoId} alt={title} src={thumbnail} className="video-thumbnail" loading="lazy" onLoad={onImageLoadHandler} />
                <div className="video-info">
                    <div className="video-meta">
                        <span className="video-published-at">{formattedDate}</span>
                        <span className="video-view-count">{viewCount !== null && viewCount !== undefined ? `조회수 ${viewCount.toLocaleString()}회` : "조회수 -"}</span>
                    </div>

                    <h3 className="video-title">{title}</h3>
                </div>
            </a>
        </div>
    );
}