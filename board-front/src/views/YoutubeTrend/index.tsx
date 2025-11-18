//  component: 유튜브 Trend 화면 컴포넌트 //

import React from 'react';
import { ChevronLeft, ChevronRight, Flame } from "lucide-react";
import {useEffect, useState} from 'react';
import {VideoListItem} from 'types/interface';
import {getTopTrendVideoRequest} from 'apis';
import {GetTopTrendVideoListResponseDto} from 'apis/response/youtube';
import {ResponseDto} from 'apis/response';
import VideoItem from 'components/VideoItem';

export default function YoutubeTrend() {
    //  state: 인기 급상승 동영상 list 상태 //
    const [topTrendList, setTopTrendList] = useState<VideoListItem[]>([]);
    //  state: 동영상 list에 사용할 Index
    const [currentIndex, setCurrentIndex] = useState(0);

    const ITEMS_PER_VIEW = 4;
    const start = currentIndex * ITEMS_PER_VIEW;
    const end = start + ITEMS_PER_VIEW;
    const visibleItems = topTrendList.slice(start, end);

    //  function: 이전 버튼 클릭 함수 //
    const handlePrev = () => {
        setCurrentIndex((prev) => Math.max(prev - 1, 0));
    }
    //  function: 다음 버튼 클릭 함수 //
    const handleNext = () => {
        const maxIndex = Math.ceil(topTrendList.length / ITEMS_PER_VIEW) - 1;
        setCurrentIndex((prev) => Math.min(prev + 1, maxIndex));
    };
    //  function: Top Trend VideoList response 처리 함수 //
    const getTopTrendVideoResponse = (responseBody: GetTopTrendVideoListResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') return;

        const { videoList } = (responseBody as GetTopTrendVideoListResponseDto).data;
        setTopTrendList(videoList);
    }
    //  effect: 첫 마운트 시 실행될 함수 //
    useEffect(() => {
        getTopTrendVideoRequest().then(getTopTrendVideoResponse);
    }, []);

    return (
        <div className="w-full flex flex-col items-center px-6 py-10">
            {/* Header */}
            <div className="flex items-center justify-center gap-2 mb-6 w-full">
                <Flame className="w-7 h-7 text-red-500" />
                <h2 className="text-xl font-bold">인기 급상승 동영상</h2>
            </div>

            {/* Carousel Wrapper */}
            <div className="relative w-full max-w-6xl flex items-center justify-center">
                {/* Left Arrow */}
                <button
                    onClick={handlePrev}
                    className="absolute left-0 top-1/2 -translate-y-1/2 z-20 p-4 rounded-full bg-white shadow-lg hover:scale-110 transition flex items-center justify-center border border-gray-200"
                >
                    <ChevronLeft className="w-8 h-8"/>
                </button>
                {/* Items */}
                <div className="w-full grid grid-cols-4 gap-6 justify-items-center video-grid transition-all duration-300">
                    {visibleItems.map((videoItem) => (
                        <VideoItem key={videoItem.videoId} videoItem={videoItem} />
                    ))}
                </div>
                {/* Right Arrow */}
                <button
                    onClick={handleNext}
                    className="absolute right-0 z-20 p-4 rounded-full bg-white shadow-lg hover:scale-110 transition flex items-center justify-center border border-gray-200"
                >
                    <ChevronRight className="w-8 h-8"/>
                </button>
            </div>

            {/* 여기에 앞으로 새로운 리스트들이 아래로 계속 붙을 수 있는 구조 */}
            <div className="flex flex-col gap-10 mt-10 w-full max-w-6xl">
                {/* 예: <AnotherVideoSection /> */}
                {/* 예: <RecommendedVideos /> */}
            </div>
        </div>
    );
}