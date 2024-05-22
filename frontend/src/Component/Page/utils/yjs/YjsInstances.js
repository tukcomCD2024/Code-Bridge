import * as Y from "yjs";

const ydocs = {};

export const getYDocInstance = (roomId) => {
  if (!ydocs[roomId]) {
    ydocs[roomId] = new Y.Doc();
  }
  return ydocs[roomId];
};
