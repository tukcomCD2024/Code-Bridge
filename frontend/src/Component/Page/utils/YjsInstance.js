import * as Y from "yjs";

const ydocs = {};

export const getYDocInstance = (roomId) => {
  if (!ydocs[roomId]) {
    ydocs[roomId] = new Y.Doc();
    console.log(`Created new Y.Doc for room: ${roomId}`);
  } else {
    console.log(`Using existing Y.Doc for room: ${roomId}`);
  }
  return ydocs[roomId];
};
