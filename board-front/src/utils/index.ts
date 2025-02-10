import {ImageUrl, BoardImageFile} from 'types/interface';
interface ImageEntity {
    id: string;
    src: string;
}

//  function: ImageUrl[] 변환 함수 //
export const extractImageUrls = (entityMap: Record<string, { data: ImageEntity }>): ImageUrl[] => {
    return Object.values(entityMap).map(({ data }) => ({
        id: data.id,
        url: data.src,
    }));
};

//  function: URL => File 변환 함수 //
export const convertUrlToFile = async (s3Url: string) => {
    // s3 객체 URL 가져오기
    const response = await fetch(s3Url);

    const data = await response.blob();
    const extend = (s3Url).split('.').pop();
    const fileName = (s3Url).split('/').pop();
    const meta = { type: `image/${extend}` };

    return new File([data], fileName as string, meta);
};

export const convertUrlsToFile = async (s3Urls: ImageUrl[]) => {
    const filePromises = s3Urls.map(async ({ id, url }) => ({
        id,
        file: await convertUrlToFile(url),
    }));

    return Promise.all(filePromises);
}