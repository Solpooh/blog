import {ResponseDto} from "../index";

export interface ChannelItem {
    channelId: string;
    title: string;
    thumbnail: string;
    customUrl: string;
    lang: string;
    createdAt: string;
    updatedAt: string;
}

export interface GetChannelListResponseDto extends ResponseDto {
    data: {
        channelList: ChannelItem[];
    };
}

export interface PostChannelResponseDto extends ResponseDto {}

export interface DeleteChannelResponseDto extends ResponseDto {
    data: {
        channelId: string;
        deletedVideoCount: number;
    };
}

export interface AdminVideoItem {
    videoId: string;
    videoTitle: string;
    videoThumbnail: string;
    channelId: string;
    channelTitle: string;
    channelThumbnail: string;
    publishedAt: string;
    createdAt: string;
    updatedAt: string;
}

export interface GetAdminVideoListResponseDto extends ResponseDto {
    data: {
        videoList: AdminVideoItem[];
        currentPage: number;
        totalPages: number;
        totalElements: number;
    };
}

export interface DeleteVideosResponseDto extends ResponseDto {
    data: {
        deletedCount: number;
    };
}
