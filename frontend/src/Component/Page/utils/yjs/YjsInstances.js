import * as Y from "yjs";

const ydocs = {};

export const getYDocInstance = (roomId) => {
  if (!ydocs[roomId]) {
    ydocs[roomId] = new Y.Doc();
  }
  console.log(`연결: ${roomId}`);
  return ydocs[roomId];
};
