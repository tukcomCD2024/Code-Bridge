const http = require('http');
const { WebSocketServer } = require('ws');
const Y = require('yjs');
const { MongodbPersistence } = require('y-mongodb-provider');
const yUtils = require('y-websocket/bin/utils');
const winston = require('winston');
const { log } = require('console');

// Winston 로거 설정
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'server.log' })
  ],
});

const port = 4000;
const databaseName = 'shareDB';

function createConnectionString(databaseName) {
  logger.info(`@@@@@@@@@@@mongodb://root:1234@localhost:27017/${databaseName}?authSource=admin`);
  //return `mongodb://root:1234@localhost:27017/${databaseName}?authSource=admin`;
  return `mongodb://root:1234@mongodbService:27017/${databaseName}?authSource=admin`;
}

const server = http.createServer((req, res) => {
  // CORS 헤더 설정
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type');

  // Preflight 요청 처리
  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  logger.info(`${req.method} ${req.url}`);
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('okay');
});

const wss = new WebSocketServer({ server });
wss.on('connection', (ws, req) => {
  try {
    logger.error('웹소켓 도착하긴 함');
    logger.info(`웹소켓 연결 요청 URL: ${req.url}`);
    yUtils.setupWSConnection(ws, req);
  } catch (error) {
    logger.error('Error during WebSocket connection setup:', error);
  }
});

const mdb = new MongodbPersistence(createConnectionString(databaseName), {
  collectionName: 'Pages',
  flushSize: 100,
  multipleCollections: true,
});

yUtils.setPersistence({
  bindState: async (docName, ydoc) => {
    try {
      const persistedYdoc = await mdb.getYDoc(docName);
      const newUpdates = Y.encodeStateAsUpdate(ydoc);
      await mdb.storeUpdate(docName, newUpdates);
      Y.applyUpdate(ydoc, Y.encodeStateAsUpdate(persistedYdoc));
      ydoc.on('update', async (update) => {
        await mdb.storeUpdate(docName, update);
      });
    } catch (error) {
      logger.error(`Error during persistence binding for ${docName}:`, error);
    }
  },
  writeState: async (docName, ydoc) => {
    // 별도의 처리가 필요한 경우 여기에 구현
    return new Promise((resolve) => {
      resolve();
    });
  },
});

server.listen(port, () => {
  logger.info(`Server listening on port ${port}`);
});
