export const convertUrlToFile = async (s3Url: string) => {
    // s3 객체 URL 가져오기
    const response = await fetch(s3Url);
    const data = await response.blob();
    const extend = s3Url.split('.').pop();
    const fileName = s3Url.split('/').pop();
    const meta = { type: `image/${extend}` };

    return new File([data], fileName as string, meta);
};

export const convertUrlsToFile = async (s3Urls: string[]) => {
    const files: File[] = [];
    for (const s3Url of s3Urls) {
        const file = await convertUrlToFile(s3Url);
        files.push(file);
    }
    return files;
}