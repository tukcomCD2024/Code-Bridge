# 베이스 이미지로 Node.js 16 버전 사용
FROM node:16

# 작업 디렉토리 설정 (/app 내에서 모든 명령어 실행)
WORKDIR /app

# package.json 및 package-lock.json (있는 경우) 복사
COPY package*.json ./

# 패키지 의존성 설치
RUN npm install

# 나머지 서버 코드 복사
COPY . .

# 서버가 리스닝할 포트 번호 설정
EXPOSE 4000

# 서버 시작 명령어
CMD ["node", "server.js"]
