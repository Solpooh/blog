import React, {useEffect, useState, useMemo} from 'react';
import './style.css';
import {deleteChannelRequest, getChannelListRequest, postChannelRequest} from '../../../apis';
import {
    DeleteChannelResponseDto,
    GetChannelListResponseDto,
    PostChannelResponseDto,
    ChannelItem
} from '../../../apis/response/admin';
import {Trash2, Plus, X, Search} from 'lucide-react';
import {useCookies} from 'react-cookie';
import {useNavigate} from 'react-router-dom';
import {AUTH_PATH} from '../../../constants';

export default function AdminChannel() {
    const [channels, setChannels] = useState<ChannelItem[]>([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [channelId, setChannelId] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [sortConfig, setSortConfig] = useState<{key: keyof ChannelItem, direction: 'asc' | 'desc'} | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [cookies] = useCookies();
    const navigate = useNavigate();

    useEffect(() => {
        fetchChannels();
    }, []);

    const fetchChannels = async () => {
        const accessToken = cookies.accessToken;
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate(AUTH_PATH());
            return;
        }

        const response = await getChannelListRequest(accessToken);
        if (!response) {
            console.error('API 응답이 없습니다.');
            return;
        }

        if (response.code !== 'SU') {
            alert('채널 목록을 불러오는데 실패했습니다.');
            return;
        }
        const {data} = response as GetChannelListResponseDto;
        setChannels(data?.channelList || []);
    };

    const handleAddChannel = async () => {
        if (!channelId.trim()) {
            alert('Channel ID를 입력해주세요.');
            return;
        }

        const accessToken = cookies.accessToken;
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate(AUTH_PATH());
            return;
        }

        setIsLoading(true);
        const response = await postChannelRequest({channelId: channelId.trim()}, accessToken);
        setIsLoading(false);

        if (!response) {
            alert('네트워크 오류가 발생했습니다.');
            return;
        }

        if (response.code === 'VF') {
            alert('이미 존재하는 채널이거나 유효하지 않은 Channel ID입니다.');
            return;
        }

        if (response.code === 'NEC') {
            alert('YouTube에서 채널을 찾을 수 없습니다.');
            return;
        }

        if (response.code !== 'SU') {
            alert('채널 추가에 실패했습니다.');
            return;
        }

        alert('채널이 추가되었습니다.');
        setChannelId('');
        setIsModalOpen(false);
        await fetchChannels();
    };

    const handleDeleteChannel = async (channelId: string, channelTitle: string) => {
        if (!window.confirm(`"${channelTitle}" 채널을 삭제하시겠습니까?\n연관된 모든 비디오도 함께 삭제됩니다.`)) {
            return;
        }

        const accessToken = cookies.accessToken;
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate(AUTH_PATH());
            return;
        }

        const response = await deleteChannelRequest(channelId, accessToken);
        if (!response) {
            alert('네트워크 오류가 발생했습니다.');
            return;
        }

        if (response.code !== 'SU') {
            alert('채널 삭제에 실패했습니다.');
            return;
        }

        const {data} = response as DeleteChannelResponseDto;
        alert(`채널이 삭제되었습니다. (삭제된 비디오: ${data.deletedVideoCount}개)`);
        await fetchChannels();
    };

    const sortData = (key: keyof ChannelItem) => {
        let direction: 'asc' | 'desc' = 'asc';
        if (sortConfig && sortConfig.key === key && sortConfig.direction === 'asc') {
            direction = 'desc';
        }
        setSortConfig({key, direction});

        const sorted = [...channels].sort((a, b) => {
            if (a[key] < b[key]) return direction === 'asc' ? -1 : 1;
            if (a[key] > b[key]) return direction === 'asc' ? 1 : -1;
            return 0;
        });
        setChannels(sorted);
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);

        const year = String(date.getFullYear()).slice(2);
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    };

    // 실시간 필터링
    const filteredChannels = useMemo(() => {
        if (!searchQuery.trim()) {
            return channels;
        }

        const query = searchQuery.toLowerCase();
        return channels.filter(channel =>
            channel.title.toLowerCase().includes(query) ||
            channel.channelId.toLowerCase().includes(query) ||
            channel.customUrl.toLowerCase().includes(query)
        );
    }, [channels, searchQuery]);

    return (
        <div className="admin-channel-container">
            <div className="admin-header">
                <h1>채널 관리</h1>
                <div className="header-actions">
                    <div className="search-box">
                        <Search size={18} className="search-icon"/>
                        <input
                            type="text"
                            placeholder="채널명, ID, URL로 검색..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="search-input"
                        />
                        {searchQuery && (
                            <button
                                className="clear-search"
                                onClick={() => setSearchQuery('')}
                                title="검색 지우기"
                            >
                                <X size={16}/>
                            </button>
                        )}
                    </div>
                    <button className="add-button" onClick={() => setIsModalOpen(true)}>
                        <Plus size={20}/>
                        채널 추가
                    </button>
                </div>
            </div>

            {searchQuery && (
                <div className="search-results-info">
                    {filteredChannels.length}개의 채널 (전체 {channels.length}개 중)
                </div>
            )}

            <div className="channel-table-wrapper">
                <table className="channel-table">
                    <thead>
                    <tr>
                        <th>썸네일</th>
                        <th onClick={() => sortData('title')} className="sortable">
                            채널명 {sortConfig?.key === 'title' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => sortData('channelId')} className="sortable">
                            Channel ID {sortConfig?.key === 'channelId' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => sortData('customUrl')} className="sortable">
                            Custom URL {sortConfig?.key === 'customUrl' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => sortData('lang')} className="sortable">
                            언어 {sortConfig?.key === 'lang' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => sortData('createdAt')} className="sortable">
                            생성일 {sortConfig?.key === 'createdAt' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => sortData('updatedAt')} className="sortable">
                            수정일 {sortConfig?.key === 'updatedAt' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                        </th>
                        <th>작업</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredChannels.length === 0 ? (
                        <tr>
                            <td colSpan={8} className="empty-state">
                                {searchQuery ? (
                                    <>
                                        <div className="empty-icon">🔍</div>
                                        <div className="empty-title">검색 결과가 없습니다</div>
                                        <div className="empty-description">
                                            "{searchQuery}"와(과) 일치하는 채널이 없습니다.
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <div className="empty-icon">📺</div>
                                        <div className="empty-title">등록된 채널이 없습니다</div>
                                        <div className="empty-description">
                                            새로운 채널을 추가해주세요.
                                        </div>
                                    </>
                                )}
                            </td>
                        </tr>
                    ) : (
                        filteredChannels.map((channel) => (
                            <tr key={channel.channelId}>
                                <td>
                                    <img src={channel.thumbnail} alt={channel.title} className="channel-thumbnail"/>
                                </td>
                                <td>
                                    <a
                                        href={`https://www.youtube.com/channel/${channel.channelId}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="channel-link"
                                    >
                                        {channel.title}
                                    </a>
                                </td>
                                <td className="channel-id">{channel.channelId}</td>
                                <td>
                                    <a
                                        href={`https://www.youtube.com/${channel.customUrl}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="custom-url"
                                    >
                                        {channel.customUrl}
                                    </a>
                                </td>
                                <td>
                                    <span className={`lang-badge lang-${channel.lang}`}>{channel.lang}</span>
                                </td>
                                <td>{formatDate(channel.createdAt)}</td>
                                <td>{formatDate(channel.updatedAt)}</td>
                                <td>
                                    <button
                                        className="delete-button"
                                        onClick={() => handleDeleteChannel(channel.channelId, channel.title)}
                                    >
                                        <Trash2 size={16}/>
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            {isModalOpen && (
                <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>채널 추가</h2>
                            <button className="close-button" onClick={() => setIsModalOpen(false)}>
                                <X size={24}/>
                            </button>
                        </div>
                        <div className="modal-body">
                            <label htmlFor="channelId">YouTube Channel ID</label>
                            <input
                                type="text"
                                id="channelId"
                                placeholder="예: UCajnLt9NyrPI8txIiefinzw"
                                value={channelId}
                                onChange={(e) => setChannelId(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleAddChannel()}
                            />
                            <p className="help-text">
                                YouTube 채널 페이지 URL에서 Channel ID를 확인할 수 있습니다.
                            </p>
                        </div>
                        <div className="modal-footer">
                            <button className="cancel-button" onClick={() => setIsModalOpen(false)}>
                                취소
                            </button>
                            <button
                                className="submit-button"
                                onClick={handleAddChannel}
                                disabled={isLoading}
                            >
                                {isLoading ? '추가 중...' : '추가'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
