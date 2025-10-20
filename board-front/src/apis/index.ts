import {SignInRequestDto, SignUpRequestDto} from './request/auth';
import axios from 'axios';
import {SignInResponseDto, SignUpResponseDto} from './response/auth';
import {ResponseDto} from './response';
import {
    GetSignInUserResponseDto,
    GetUserResponseDto,
    PatchNicknameResponseDto,
    PatchProfileImageResponseDto
} from './response/user';
import {
    PatchBoardRequestDto,
    PatchCommentRequestDto,
    PostBoardRequestDto,
    PostCommentRequestDto
} from './request/board';
import {
    DeleteBoardResponseDto,
    DeleteCommentResponseDto,
    GetBoardResponseDto,
    GetCommentListResponseDto,
    GetFavoriteListResponseDto,
    GetLatestBoardListResponseDto,
    GetSearchBoardListResponseDto,
    GetTop3BoardListResponseDto,
    GetUserBoardListResponseDto,
    IncreaseViewCountResponseDto,
    PatchBoardResponseDto,
    PatchCommentResponseDto,
    PostBoardResponseDto,
    PostCommentResponseDto,
    PutFavoriteResponseDto
} from './response/board';
import {GetPopularListResponseDto, GetRelationListResponseDto} from './response/search';
import {PatchNicknameRequestDto, PatchProfileImageRequestDto} from './request/user';
import {GetVideoListResponseDto, DeleteVideoResponseDto, GetSearchVideoListResponseDto} from "./response/youtube";

const DOMAIN = 'http://localhost:4000/api';
// const DOMAIN = 'https://devhubs.site/api';
const API_DOMAIN = `${DOMAIN}/v1`;

const authorization = (accessToken: string) => {
    return { headers: { Authorization: `Bearer ${accessToken}` } }
};
const SIGN_IN_URL = () => `${API_DOMAIN}/auth/sign-in`;
const SIGN_UP_URL = () => `${API_DOMAIN}/auth/sign-up`;

export const signInRequest = async (requestBody: SignInRequestDto) => {
    const result = await axios.post(SIGN_IN_URL(), requestBody)
        .then(response => {
            const responseBody: SignInResponseDto = response.data;
            return responseBody;
        })
        .catch (error=> {
            if (!error.response.data) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}

export const signUpRequest = async (requestBody: SignUpRequestDto) => {
    const result = await axios.post(SIGN_UP_URL(), requestBody)
        .then(response => {
            const responseBody: SignUpResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response.data) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}

const GET_BOARD_URL = (category: string, boardNumber: number | string) => `${API_DOMAIN}/board/${category}/${boardNumber}`;
const GET_LATEST_BOARD_LIST_URL = (category: string | null, page: number = 0) => `${API_DOMAIN}/board/latest-list${category ? '/' + category : ''}?page=${page}`;
const GET_TOP_3_BOARD_LIST_URL = () => `${API_DOMAIN}/board/top-3`;
const GET_SEARCH_BOARD_LIST_URL = (searchWord: string, preSearchWord: string | null, page: number = 0) => `${API_DOMAIN}/board/search-list/${searchWord}${preSearchWord ? '/' + preSearchWord : ''}?page=${page}`;
const GET_USER_BOARD_LIST_URL = (email: string, page: number = 0) => `${API_DOMAIN}/board/user-board-list/${email}?page=${page}`;
const INCREASE_VIEW_COUNT_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/increase-view-count`;
const GET_FAVORITE_LIST_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/favorite-list`;
const GET_COMMENT_LIST_URL = (boardNumber: number | string, page: number = 0) => `${API_DOMAIN}/board/${boardNumber}/comment-list?page=${page}`;
const POST_BOARD_URL = () => `${API_DOMAIN}/board`;
const POST_COMMENT_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/comment`;
const PATCH_BOARD_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}`;
const PATCH_COMMENT_URL = (boardNumber: number | string, commentNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/comment/${commentNumber}`;
const PUT_FAVORITE_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/favorite`;
const DELETE_BOARD_URL = (boardNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}`;
const DELETE_COMMENT_URL = (boardNumber: number | string, commentNumber: number | string) => `${API_DOMAIN}/board/${boardNumber}/comment/${commentNumber}`;
export const getBoardRequest = async (category: string, boardNumber: number | string) => {
    const result = await axios.get(GET_BOARD_URL(category, boardNumber))
        .then(response => {
            const responseBody: GetBoardResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        })
    return result;
};
export const getLatestBoardListRequest = async (category: string | null, page: number = 0) => {
    const result = await axios.get(GET_LATEST_BOARD_LIST_URL(category, page))
        .then(response => {
            const responseBody: GetLatestBoardListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
export const getTop3BoardListRequest = async () => {
    const result = await axios.get(GET_TOP_3_BOARD_LIST_URL())
        .then(response => {
            const responseBody: GetTop3BoardListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const getSearchBoardListRequest = async (searchWord: string, preSearchWord: string | null, page: number = 0) => {
    const result = await axios.get(GET_SEARCH_BOARD_LIST_URL(searchWord, preSearchWord, page))
        .then(response => {
            const responseBody: GetSearchBoardListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
export const getUserBoardListRequest = async (email: string, page: number = 0) => {
    const result = await axios.get(GET_USER_BOARD_LIST_URL(email, page))
        .then(response => {
            const responseBody: GetUserBoardListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const increaseViewCountRequest = async (boardNumber: number | string) => {
    const result = await axios.get(INCREASE_VIEW_COUNT_URL(boardNumber))
        .then(response => {
            const responseBody: IncreaseViewCountResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        })
    return result;
}
export const getFavoriteListRequest = async (boardNumber: number | string) => {
    const result = await axios.get(GET_FAVORITE_LIST_URL(boardNumber))
        .then(response => {
            const responseBody: GetFavoriteListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const getCommentListRequest = async (boardNumber: number | string, page: number = 0) => {
    const result = await axios.get(GET_COMMENT_LIST_URL(boardNumber, page))
        .then(response => {
            const responseBody: GetCommentListResponseDto = response.data;
            return responseBody;
        })
        .catch((error)=>{
            if(!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const postBoardRequest = async (requestBody: PostBoardRequestDto, accessToken: string) => {
    const result = await axios.post(POST_BOARD_URL(), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PostBoardResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const postCommentRequest = async (boardNumber: number | string, requestBody: PostCommentRequestDto, accessToken: string) => {
    const result = await axios.post(POST_COMMENT_URL(boardNumber), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PostCommentResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const patchBoardRequest = async (boardNumber: number | string, requestBody: PatchBoardRequestDto, accessToken: string) => {
    const result = await axios.patch(PATCH_BOARD_URL(boardNumber), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PatchBoardResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.resposne) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const patchCommentRequest = async (boardNumber: number | string, commentNumber: number | string, requestBody: PatchCommentRequestDto, accessToken: string) => {
    const result = await axios.patch(PATCH_COMMENT_URL(boardNumber, commentNumber), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PatchCommentResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const putFavoriteRequest = async (boardNumber: number | string, accessToken: string) => {
    const result = await axios.put(PUT_FAVORITE_URL(boardNumber), {}, authorization(accessToken))
        .then(response => {
            const responseBody: PutFavoriteResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const deleteBoardRequest = async (boardNumber: number | string, accessToken: string) => {
    const result = await axios.delete(DELETE_BOARD_URL(boardNumber), authorization(accessToken))
        .then(response => {
            const responseBody: DeleteBoardResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
export const deleteCommentRequest = async (boardNumber: number | string, commentNumber: number | string, accessToken: string) => {
    const result = await axios.delete(DELETE_COMMENT_URL(boardNumber, commentNumber), authorization(accessToken))
        .then(response => {
            const responseBody: DeleteCommentResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};

const GET_POPULAR_LIST_URL = () => `${API_DOMAIN}/search/popular-list`;
const GET_RELATION_LIST_URL = (searchWord: string) => `${API_DOMAIN}/search/${searchWord}/relation-list`;

export const getPopularListRequest = async () => {
    const result = await axios.get(GET_POPULAR_LIST_URL())
        .then(response => {
            const responseBody: GetPopularListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
export const getRelationListRequest = async (searchWord: string) => {
    const result = await axios.get(GET_RELATION_LIST_URL(searchWord))
        .then(response => {
            const responseBody: GetRelationListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};

const GET_USER_URL = (email:string) => `${API_DOMAIN}/user/${email}`;
const GET_SIGN_IN_USER_URL = () => `${API_DOMAIN}/user`;
const PATCH_NICKNAME_URL = () => `${API_DOMAIN}/user/nickname`;
const PATCH_PROFILE_IMAGE_URL = () => `${API_DOMAIN}/user/profile-image`;
export const getUserRequest = async (email: string) => {
    const result = await axios.get(GET_USER_URL(email))
        .then(response => {
            const responseBody: GetUserResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const getSignInUserRequest = async (accessToken: string) => {
    const result = await axios.get(GET_SIGN_IN_USER_URL(), authorization(accessToken))
        .then(response => {
            const responseBody: GetSignInUserResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const patchNicknameRequest = async (requestBody: PatchNicknameRequestDto, accessToken: string) => {
    const result = await axios.patch(PATCH_NICKNAME_URL(), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PatchNicknameResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
}
export const patchProfileImageRequest = async (requestBody: PatchProfileImageRequestDto, accessToken: string) => {
    const result = await axios.patch(PATCH_PROFILE_IMAGE_URL(), requestBody, authorization(accessToken))
        .then(response => {
            const responseBody: PatchProfileImageResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};

const FILE_DOMAIN = `http://localhost:4000/file`;
// const FILE_DOMAIN = 'https://devhubs.site/file';

const FILE_UPLOAD_URL = () => `${FILE_DOMAIN}/upload`;

const multipartFormData = { headers: { 'Content-Type': 'multipart/form-data' } };

export const fileUploadRequest = async (data: FormData) => {
    const result = await axios.post(FILE_UPLOAD_URL(), data, multipartFormData)
        .then(response => {
            const responseBody: string = response.data;
            return responseBody;
        })
        .catch(error => {
            return null;
        });
    return result;
}

const GET_VIDEO_LIST_URL = (page: number = 0) => `${API_DOMAIN}/video?page=${page}`;
const GET_SEARCH_VIDEO_LIST_URL = (searchWord: string, type: string, page: number = 0) => `${API_DOMAIN}/video/search-list/${searchWord}?type=${type}&page=${page}`;
export const getVideoListRequest = async (page: number = 0) => {
    const result = await axios.get(GET_VIDEO_LIST_URL(page))
        .then(response => {
            const responseBody: GetVideoListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
export const getSearchVideoListRequest = async (searchWord: string, type: string, page: number = 0) => {
    const result = await axios.get(GET_SEARCH_VIDEO_LIST_URL(searchWord, type, page))
        .then(response => {
            const responseBody: GetSearchVideoListResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};
const DELETE_VIDEO_URL = (videoId: string) => `${API_DOMAIN}/video/${videoId}`;
export const deleteVideoRequest = async (videoId: string) => {
    const result = await axios.delete(DELETE_VIDEO_URL(videoId))
        .then(response => {
            const responseBody: DeleteVideoResponseDto = response.data;
            return responseBody;
        })
        .catch(error => {
            if (!error.response) return null;
            const responseBody: ResponseDto = error.response.data;
            return responseBody;
        });
    return result;
};