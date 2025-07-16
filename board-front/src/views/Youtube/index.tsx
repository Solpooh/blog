//  component: 유튜브 메인 화면 컴포넌트  //
import {VideoListItem} from 'types/interface';
import {useEffect, useState} from 'react';
import VideoItem from 'components/VideoItem';
import {getVideoListRequest} from 'apis';
import {GetVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import './style.css';
import Pagination from 'types/interface/pagination.interface';
import Paging from 'components/Paging';

export default function Youtube() {
    //  state: 유튜브 최신 비디오 리스트 상태  //
    const [videoList, setVideoList] = useState<VideoListItem[]>([]);
    //  state: 페이지네이션 상태 //
    const [pagination, setPagination] = useState<Pagination<VideoListItem> | null>(null)
    //  state: 현재 페이지 상태 //
    const [currentPage, setCurrentPage] = useState<number>(1);

    //  function: videoList response 처리 함수 //
    const getVideoListResponse = (responseBody: GetVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { pagination } = responseBody as GetVideoListResponseDto;
        setVideoList(pagination.content);
        setPagination(pagination);
    }

    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        getVideoListRequest(currentPage - 1).then(getVideoListResponse);
    }, [currentPage]);

    return (
        <div className="youtube-wrapper">
            <div className="youtube-header">
                <h2>추천 유튜브 영상</h2>
            </div>
            <div className="video-grid">
                {videoList.map(videoItem => <VideoItem key={videoItem.videoId} videoItem={videoItem} />)}
            </div>

            {pagination && (
                <div className="main-bottom-pagination-box">
                    <Paging
                        currentPage={currentPage}
                        totalPages={pagination.totalPages}
                        onPageChange={setCurrentPage}
                    />
                </div>
            )}
        </div>
    );
}