import { Board } from "types/interface";

const boardMock: Board = {
    boardNumber: 1,
    title: '오늘 점심 뭐먹지 추천 좀 부탁해 오늘 점심 뭐먹지 추천 좀 부탁해 오늘 점심 뭐먹지 추천 좀 부탁해',
    content: '그래서 점심 뭐 먹었어? 그래서 점심 뭐 먹었어? 그래서 점심 뭐 먹었어? 그래서 점심 뭐 먹었어?',
    boardImageList: ['https://pds.joongang.co.kr/news/component/htmlphoto_mmdata/201901/20/28017477-0365-4a43-b546-008b603da621.jpg', 'data:image/jpeg;base64,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'],
    writeDatetime: '2024. 07. 02.',
    writerEmail: 'email@email2.com',
    writerNickname: 'Solpooh',
    writerProfileImage: 'data:image/jpeg;base64,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'
}

export default boardMock