import {ImageUrl, BoardImageFile} from 'types/interface';

export const convertUrlToFile = async (s3Url: ImageUrl) => {
    // s3 객체 URL 가져오기
    const response = await fetch(s3Url.url);
    const data = await response.blob();
    const extend = (s3Url.url).split('.').pop();
    const fileName = (s3Url.url).split('/').pop();
    const meta = { type: `image/${extend}` };

    return new File([data], fileName as string, meta);
};

export const convertUrlsToFile = async (s3Urls: ImageUrl[]) => {
    const files: BoardImageFile[] = [];
    for (const s3Url of s3Urls) {
        const file = await convertUrlToFile(s3Url);
        files.push({
            id: s3Url.id,
            file: file,
        });
    }
    return files;
}