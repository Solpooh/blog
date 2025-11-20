//  component: 유튜브 Trend 화면 컴포넌트 //

import React from 'react';
import { ChevronLeft, ChevronRight, Flame } from "lucide-react";
import {useEffect, useState} from 'react';
import {VideoListItem} from 'types/interface';
import {getHotVideoRequest, getShortsVideoRequest, getTopViewVideoRequest} from 'apis';
import {
    GetHotVideoListResponseDto,
    GetShortsVideoListResponseDto,
    GetTopViewVideoListResponseDto
} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import VideoItem from 'components/VideoItem';
import './style.css';

export default function YoutubeTrend() {
    //  state: 인기 급상승 동영상 list 상태 //
    const [hotList, setHotList] = useState<VideoListItem[]>([]);
    //  state: 조회수 TOP 동영상 list 상태 //
    const [topList, setTopList] = useState<VideoListItem[]>([]);
    //  state: Shorts 동영상 list 상태 //
    const [shortsList, setShortsList] = useState<VideoListItem[]>([]);
    //  state: 캐러셀 공용 인덱스는 섹션 단위로 분리
    const [hotIndex, setHotIndex] = useState(0);
    const [topIndex, setTopIndex] = useState(0);
    const [shortsIndex, setShortsIndex] = useState(0);

    const ITEMS_PER_VIEW = 4;
    const getVisible = (list: VideoListItem[], index: number) => {
        const start = index * ITEMS_PER_VIEW;
        return list.slice(start, start + ITEMS_PER_VIEW);
    };

    //  function: 이전 버튼 클릭 함수 //
    const handlePrev = (setter: React.Dispatch<React.SetStateAction<number>>) => {
        setter(prev => Math.max(prev - 1, 0));
    };
    //  function: 다음 버튼 클릭 함수 //
    const handleNext = (setter: React.Dispatch<React.SetStateAction<number>>, listLength: number) => {
        const maxIndex = Math.ceil(listLength / ITEMS_PER_VIEW) - 1;
        setter(prev => Math.min(prev + 1, maxIndex));
    };
    //  function: HOT VideoList response 처리 함수 //
    const getHotVideoResponse = (responseBody: GetHotVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { videoList } = (responseBody as GetHotVideoListResponseDto).data;
        setHotList(videoList);
    }
    //  function: Top View VideoList response 처리 함수 //
    const getTopViewVideoResponse = (responseBody: GetTopViewVideoListResponseDto | ResponseDto | null) => {
        if(!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { videoList } = (responseBody as GetTopViewVideoListResponseDto).data;
        setTopList(videoList);
    }
    const getShortsVideoResponse = (responseBody: GetShortsVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const {code} = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const {videoList} = (responseBody as GetShortsVideoListResponseDto).data;
        setShortsList(videoList);
    }
    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        getHotVideoRequest().then(getHotVideoResponse);
        getTopViewVideoRequest().then(getTopViewVideoResponse);
        getShortsVideoRequest().then(getShortsVideoResponse);
    }, []);

    const renderCarousel = (
        title: string,
        list: VideoListItem[],
        index: number,
        setter: React.Dispatch<React.SetStateAction<number>>,
        icon: string
    ) => (
        <>
            <div className="video-header">
                <div className="video-icon">{icon}</div>
                <h2>{title}</h2>
            </div>


            <div className="carousel-container">
                <button className="arrow-btn left" onClick={() => handlePrev(setter)}>‹</button>


                <div className="carousel-items">
                    {getVisible(list, index).map((videoItem) => (
                        <div key={videoItem.videoId} className="carousel-item">
                            <VideoItem videoItem={videoItem} />
                        </div>
                    ))}
                </div>


                <button className="arrow-btn right" onClick={() => handleNext(setter, list.length)}>›</button>
            </div>
        </>
    );

    return (
        <div className="video-wrapper">
            {renderCarousel('인기 급상승 동영상', hotList, hotIndex, setHotIndex, '🔥')}
            {renderCarousel('조회수 TOP 동영상', topList, topIndex, setTopIndex, '👑')}
            {renderCarousel('Shorts 동영상', shortsList, shortsIndex, setShortsIndex, '✂️')}
        </div>
    );
}