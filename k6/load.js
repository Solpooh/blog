import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    stages: [
        { duration: '3m', target: 25 }, //먼저 3분 동안 VUser 1에서 25까지 서서히 올린다.
        { duration: '10m',target: 25 }, //Vuser 25에서 10분간 유지한다.
        { duration: '3m', target: 125 }, //다시 3분간 25에서 125까지 서서히 올린다.
        { duration: '3m', target: 0 },
    ],

    thresholds: {
        http_req_duration: ['p(95) < 128'], // 95% 요청이 128ms 이내여야 통과
    },
};

export default function () {
    // 1. 메인 홈 화면 요청
    let homeRes = http.get('http://3.35.20.119:4000/api/v1/board/latest-list/All?page=0');
    check(homeRes, { '홈 화면 status 200': (r) => r.status === 200 });

    sleep(1);

    // 2. 상세 페이지 요청
    let detailRes = http.get('http://3.35.20.119:4000/api/v1/board/detail/34');
    check(detailRes, { '상세 페이지 status 200': (r) => r.status === 200 });

    sleep(1);
}