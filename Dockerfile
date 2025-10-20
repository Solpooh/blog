# 1. k6 공식 이미지 기반
FROM grafana/k6:latest

# 2. 스크립트 디렉토리 생성
WORKDIR /scripts

# 3. 로컬에 있는 k6 스크립트를 이미지 안으로 복사
COPY /k6/load.js /scripts/load.js

# 4. 컨테이너 실행 시 k6 실행
CMD ["run", "load.js"]