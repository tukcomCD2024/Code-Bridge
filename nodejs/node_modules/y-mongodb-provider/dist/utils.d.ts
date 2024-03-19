export const PREFERRED_TRIM_SIZE: 400;
export function clearUpdatesRange(db: any, docName: string, from: number, to: number): Promise<void>;
export function createDocumentUpdateKey(docName: string, clock?: number | undefined): any;
export function createDocumentStateVectorKey(docName: string): any;
export function createDocumentMetaKey(docName: string, metaKey: string): any;
export function _getMongoBulkData(db: any, query: object, opts: object): Promise<any[]>;
export function flushDB(db: any): Promise<any>;
export function getMongoUpdates(db: any, docName: string, opts?: any): Promise<any[]>;
export function getCurrentUpdateClock(db: any, docName: string): Promise<number>;
export function writeStateVector(db: any, docName: string, sv: Uint8Array, clock: number): Promise<void>;
export function storeUpdate(db: any, docName: string, update: Uint8Array): Promise<number>;
export function mergeUpdates(updates: Array<Uint8Array>): {
    update: Uint8Array;
    sv: Uint8Array;
};
export function decodeMongodbStateVector(buf: Uint8Array): {
    sv: Uint8Array;
    clock: number;
};
export function readStateVector(db: any, docName: string): Promise<{
    sv: Uint8Array;
    clock: number;
} | {
    sv: null;
    clock: number;
}>;
export function getAllSVDocs(db: any): Promise<any>;
export function flushDocument(db: any, docName: string, stateAsUpdate: Uint8Array, stateVector: Uint8Array): Promise<number>;
//# sourceMappingURL=utils.d.ts.map