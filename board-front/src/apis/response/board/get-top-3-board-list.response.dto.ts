import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';

export interface GetTop3BoardList {
    top3List: BoardListItem[];
}
export type GetTop3BoardListResponseDto = ResponseDto<GetTop3BoardList>;