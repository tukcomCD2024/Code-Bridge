export class MongodbPersistence {
    /**
     * Create a y-mongodb persistence instance.
     * @param {string} location The connection string for the MongoDB instance.
     * @param {object} [opts] Additional optional parameters.
     * @param {string} [opts.collectionName] Name of the collection where all
     * documents are stored. Default: "yjs-writings"
     * @param {boolean} [opts.multipleCollections] When set to true, each document gets
     * an own collection (instead of all documents stored in the same one). When set to true,
     * the option collectionName gets ignored. Default: false
     * @param {number} [opts.flushSize] The number of stored transactions needed until
     * they are merged automatically into one Mongodb document. Default: 400
     */
    constructor(location: string, opts?: {
        collectionName?: string | undefined;
        multipleCollections?: boolean | undefined;
        flushSize?: number | undefined;
    } | undefined);
    flushSize: number;
    multipleCollections: boolean;
    tr: {};
    /**
     * Execute an transaction on a database. This will ensure that other processes are
     * currently not writing.
     *
     * This is a private method and might change in the future.
     *
     * @template T
     *
     * @param {function(any):Promise<T>} f A transaction that receives the db object
     * @return {Promise<T>}
     */
    _transact: <T>(docName: any, f: (arg0: any) => Promise<T>) => Promise<T>;
    /**
     * Create a Y.Doc instance with the data persistet in mongodb.
     * Use this to temporarily create a Yjs document to sync changes or extract data.
     *
     * @param {string} docName
     * @return {Promise<Y.Doc>}
     */
    getYDoc(docName: string): Promise<Y.Doc>;
    /**
     * Store a single document update to the database.
     *
     * @param {string} docName
     * @param {Uint8Array} update
     * @return {Promise<number>} Returns the clock of the stored update
     */
    storeUpdate(docName: string, update: Uint8Array): Promise<number>;
    /**
     * The state vector (describing the state of the persisted document - see https://github.com/yjs/yjs#Document-Updates) is maintained in a separate field and constantly updated.
     *
     * This allows you to sync changes without actually creating a Yjs document.
     *
     * @param {string} docName
     * @return {Promise<Uint8Array>}
     */
    getStateVector(docName: string): Promise<Uint8Array>;
    /**
     * Get the differences directly from the database.
     * The same as Y.encodeStateAsUpdate(ydoc, stateVector).
     * @param {string} docName
     * @param {Uint8Array} stateVector
     */
    getDiff(docName: string, stateVector: Uint8Array): Promise<Uint8Array>;
    /**
     * Delete a document, and all associated data from the database.
     * When option multipleCollections is set, it removes the corresponding collection
     * @param {string} docName
     * @return {Promise<void>}
     */
    clearDocument(docName: string): Promise<void>;
    /**
     * Persist some meta information in the database and associate it
     * with a document. It is up to you what you store here.
     * You could, for example, store credentials here.
     *
     * @param {string} docName
     * @param {string} metaKey
     * @param {any} value
     * @return {Promise<void>}
     */
    setMeta(docName: string, metaKey: string, value: any): Promise<void>;
    /**
     * Retrieve a store meta value from the database. Returns undefined if the
     * metaKey doesn't exist.
     *
     * @param {string} docName
     * @param {string} metaKey
     * @return {Promise<any>}
     */
    getMeta(docName: string, metaKey: string): Promise<any>;
    /**
     * Delete a store meta value.
     *
     * @param {string} docName
     * @param {string} metaKey
     * @return {Promise<any>}
     */
    delMeta(docName: string, metaKey: string): Promise<any>;
    /**
     * Retrieve the names of all stored documents.
     *
     * @return {Promise<string[]>}
     */
    getAllDocNames(): Promise<string[]>;
    /**
     * Retrieve the state vectors of all stored documents.
     * You can use this to sync two y-leveldb instances.
     * !Note: The state vectors might be outdated if the associated document
     * is not yet flushed. So use with caution.
     * @return {Promise<{ name: string, sv: Uint8Array, clock: number }[]>}
     * @todo may not work?
     */
    getAllDocStateVectors(): Promise<{
        name: string;
        sv: Uint8Array;
        clock: number;
    }[]>;
    /**
     * Internally y-mongodb stores incremental updates. You can merge all document
     * updates to a single entry. You probably never have to use this.
     * It is done automatically every $options.flushsize (default 400) transactions.
     *
     * @param {string} docName
     * @return {Promise<void>}
     */
    flushDocument(docName: string): Promise<void>;
    /**
     * Delete the whole yjs mongodb
     * @return {Promise<void>}
     */
    flushDB(): Promise<void>;
    /**
     * Closes open database connection
     * @returns {Promise<void>}
     */
    destroy(): Promise<void>;
}
import * as Y from "yjs";
//# sourceMappingURL=y-mongodb.d.ts.map