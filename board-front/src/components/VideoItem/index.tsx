import {VideoListItem} from 'types/interface';
import './style.css';
interface Props {
    videoItem: VideoListItem;
}
export default function VideoItem({ videoItem }: Props) {

    const { videoId, title, thumbnail, channelId, channelTitle, channelThumbnail, publishedAt } = videoItem;
    const formattedDate = new Date(publishedAt).toISOString().split("T")[0]; // 'yyyy-MM-dd' 포맷


    return (
        <div className="video-card">
            <div className="channel-info">
                <img src={channelThumbnail} alt={channelTitle} className="channel-thumbnail" loading="lazy"/>
                <p className="video-channel">{channelTitle}</p>
            </div>
            <a href={`https://youtu.be/${videoId}`} target="_blank">
                <img src={thumbnail} className="video-thumbnail" loading="lazy" />
                <div className="video-info">
                    <p className="video-published-at">{formattedDate}</p>
                    <h3 className="video-title">{title}</h3>
                </div>
            </a>
        </div>
    );
}