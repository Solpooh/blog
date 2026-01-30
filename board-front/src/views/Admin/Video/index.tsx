import React, {useEffect, useState} from 'react';
import './style.css';
import {
    deleteVideosRequest,
    getAdminVideoListRequest,
    searchAdminVideosByChannelRequest
} from '../../../apis';
import {AdminVideoItem, GetAdminVideoListResponseDto} from '../../../apis/response/admin';
import {Trash2, Search} from 'lucide-react';
import Paging from '../../../components/Paging';
import {useCookies} from 'react-cookie';
import {useNavigate} from 'react-router-dom';
import {AUTH_PATH} from '../../../constants';

export default function AdminVideo() {
    const [videos, setVideos] = useState<AdminVideoItem[]>([]);
    const [selectedVideos, setSelectedVideos] = useState<Set<string>>(new Set());
    const [pagination, setPagination] = useState<any>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [isSearching, setIsSearching] = useState(false);
    const pageSize = 20;
    const [cookies] = useCookies();
    const navigate = useNavigate();

    useEffect(() => {
        fetchVideos(1); // 1-based index
    }, []);

    const fetchVideos = async (page: number) => {
        const accessToken = cookies.accessToken;
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate(AUTH_PATH());
            return;
        }

        const pageIndex = page - 1; // 0-based index
        const response = isSearching
            ? await searchAdminVideosByChannelRequest(searchQuery, pageIndex, pageSize, accessToken)
            : await getAdminVideoListRequest(pageIndex, pageSize, accessToken);

        if (!response) {
            console.error('API 응답이 없습니다.');
            return;
        }

        if (response.code !== 'SU') {
            alert('비디오 목록을 불러오는데 실패했습니다.');
            return;
        }

        const {data} = response as GetAdminVideoListResponseDto;
        setVideos(data.videoList);

        // Paging 컴포넌트에 맞게 pagination 구성
        setPagination({
            page: data.currentPage,
            size: pageSize,
            totalPages: data.totalPages,
            totalElements: data.totalElements,
            first: data.currentPage === 0,
            last: data.currentPage === data.totalPages - 1,
            numberOfElements: data.videoList.length
        });

        // 페이지 변경 시 선택 초기화
        setSelectedVideos(new Set());
    };

    const handleSearch = () => {
        if (!searchQuery.trim()) {
            setIsSearching(false);
        } else {
            setIsSearching(true);
        }
        fetchVideos(1);
    };

    const handleClearSearch = () => {
        setSearchQuery('');
        setIsSearching(false);
        fetchVideos(1);
    };

    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked) {
            setSelectedVideos(new Set(videos.map(v => v.videoId)));
        } else {
            setSelectedVideos(new Set());
        }
    };

    const handleSelectVideo = (videoId: string) => {
        const newSelected = new Set(selectedVideos);
        if (newSelected.has(videoId)) {
            newSelected.delete(videoId);
        } else {
            newSelected.add(videoId);
        }
        setSelectedVideos(newSelected);
    };

    const handleDeleteSelected = async () => {
        if (selectedVideos.size === 0) {
            alert('삭제할 비디오를 선택해주세요.');
            return;
        }

        if (!window.confirm(`선택한 ${selectedVideos.size}개의 비디오를 삭제하시겠습니까?`)) {
            return;
        }

        const accessToken = cookies.accessToken;
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate(AUTH_PATH());
            return;
        }

        const response = await deleteVideosRequest({ videoIds: Array.from(selectedVideos) }, accessToken);
        if (!response) {
            alert('네트워크 오류가 발생했습니다.');
            return;
        }

        if (response.code !== 'SU') {
            alert('비디오 삭제에 실패했습니다.');
            return;
        }

        const deletedCount = response.data.deletedCount;
        alert(`${deletedCount}개의 비디오가 삭제되었습니다.`);
        fetchVideos(pagination?.page + 1 || 1);
    };

    const formatDateTime = (dateString: string) => {
        const date = new Date(dateString);
        const year = String(date.getFullYear()).slice(2);
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");
        return `${year}-${month}-${day} ${hour}:${minute}`;
    };

    const onPageChange = (page: number) => {
        fetchVideos(page);
    };

    const isAllSelected = videos.length > 0 && selectedVideos.size === videos.length;
    const isSomeSelected = selectedVideos.size > 0 && !isAllSelected;

    return (
        <div className="admin-video-container">
            <div className="admin-header">
                <h1>비디오 관리</h1>
                <div className="search-box">
                    <input
                        type="text"
                        placeholder="채널명으로 검색..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    />
                    <button className="search-button" onClick={handleSearch}>
                        <Search size={20}/>
                    </button>
                    {isSearching && (
                        <button className="clear-search-button" onClick={handleClearSearch}>
                            전체 보기
                        </button>
                    )}
                </div>
            </div>

            <div className="video-controls">
                <div className="video-stats">
                    총 {pagination?.totalElements.toLocaleString() || 0}개의 비디오
                    {isSearching && ` (검색 결과)`}
                    {selectedVideos.size > 0 && ` | ${selectedVideos.size}개 선택됨`}
                </div>
                {selectedVideos.size > 0 && (
                    <button className="delete-selected-button" onClick={handleDeleteSelected}>
                        <Trash2 size={16}/>
                        선택 삭제 ({selectedVideos.size})
                    </button>
                )}
            </div>

            <div className="video-table-wrapper">
                <table className="video-table">
                    <thead>
                    <tr>
                        <th style={{width: '40px'}}>
                            <input
                                type="checkbox"
                                checked={isAllSelected}
                                ref={input => {
                                    if (input) input.indeterminate = isSomeSelected;
                                }}
                                onChange={handleSelectAll}
                            />
                        </th>
                        <th>채널</th>
                        <th>비디오</th>
                        <th>게시일</th>
                        <th>생성일</th>
                        <th>수정일</th>
                    </tr>
                    </thead>
                    <tbody>
                    {videos.map((video) => (
                        <tr key={video.videoId}>
                            <td>
                                <input
                                    type="checkbox"
                                    checked={selectedVideos.has(video.videoId)}
                                    onChange={() => handleSelectVideo(video.videoId)}
                                />
                            </td>
                            <td>
                                <div className="channel-info">
                                    <img
                                        src={video.channelThumbnail}
                                        alt={video.channelTitle}
                                        className="channel-thumbnail"
                                    />
                                    <a
                                        href={`https://www.youtube.com/channel/${video.channelId}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="channel-link"
                                    >
                                        {video.channelTitle}
                                    </a>
                                </div>
                            </td>
                            <td>
                                <div className="video-info">
                                    <img
                                        src={video.videoThumbnail}
                                        alt={video.videoTitle}
                                        className="video-thumbnail"
                                    />
                                    <a
                                        href={`https://www.youtube.com/watch?v=${video.videoId}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="video-link"
                                    >
                                        {video.videoTitle}
                                    </a>
                                </div>
                            </td>
                            <td>{formatDateTime(video.publishedAt)}</td>
                            <td>{formatDateTime(video.createdAt)}</td>
                            <td>{formatDateTime(video.updatedAt)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {pagination && (
                <div className="main-bottom-pagination-box">
                    <Paging
                        pagination={pagination}
                        onPageChange={onPageChange}
                    />
                </div>
            )}
        </div>
    );
}
