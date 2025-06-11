import { google } from 'googleapis'
import create from './youtubeDao.js'

const youtube = google.youtube({
    version: 'v3',
    auth: 'API-KEY',
})

async function getLatestVideo(channelId) {
    try {
        const response = await youtube.search.list({
            channelId: channelId,
            maxResults: 3,
            order: 'date',
            part: 'snippet',
        })

        const videos = response.data.items.map((item) => {
            return {
                channelId,
                title: item.snippet.title,
                videoId: item.id.videoId,
                publishedAt: item.snippet.publishedAt,
            }
        })
        return videos
    } catch (error) {
        console.error('Errors:', error);
    }
}

// 채널 id를 입력해 실행
const videos = await getLatestVideo('UCHbXBo1fQAg7j0D7HKKYHJg')
videos.forEach((data) => {
    create(data)
})