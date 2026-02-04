import ResponseDto from '../response.dto';

export interface SubCategoryStats {
    subCategory: string;
    displayName: string;
    count: number;
}

export interface MainCategoryStats {
    mainCategory: string;
    displayName: string;
    count: number;
    subCategories: SubCategoryStats[];
}

export default interface GetCategoryStatsResponseDto extends ResponseDto {
    data: {
        categories: MainCategoryStats[];
    };
}
