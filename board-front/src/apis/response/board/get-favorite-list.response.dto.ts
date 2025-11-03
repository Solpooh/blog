import ResponseDto from '../response.dto';
import {FavoriteListItem} from 'types/interface';

export interface GetFavoriteList {
    favoriteList: FavoriteListItem[]
}

export type GetFavoriteListResponseDto = ResponseDto<GetFavoriteList>;