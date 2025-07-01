import {VideoListItem} from 'types/interface';
import './style.css';
interface Props {
    videoItem: VideoListItem;
}
export default function VideoItem({ videoItem }: Props) {

    const { videoId, title, thumbnail, channelId, channelTitle, channelThumbnail, publishedAt } = videoItem;

    return (
        <div className="video-card">
            <img src={thumbnail} alt={title} className="video-thumbnail" />
            <div className="video-info">
                <h3 className="video-title">{title}</h3>
                <p className="video-channel">{channelTitle}</p>
            </div>
        </div>
    );
}