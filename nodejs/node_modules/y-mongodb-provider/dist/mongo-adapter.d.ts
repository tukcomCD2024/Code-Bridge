export class MongoAdapter {
    /**
     * Create a MongoAdapter instance.
     * @param {string} connectionString
     * @param {object} opts
     * @param {string} opts.collection Name of the collection where all documents are stored.
     * @param {boolean} opts.multipleCollections When set to true, each document gets an own
     * collection (instead of all documents stored in the same one).
     * When set to true, the option $collection gets ignored.
     */
    constructor(connectionString: string, { collection, multipleCollections }: {
        collection: string;
        multipleCollections: boolean;
    });
    collection: string;
    multipleCollections: boolean;
    mongoUrl: string;
    databaseName: string;
    client: MongoClient;
    db: import("mongodb").Db;
    /**
     * Get the MongoDB collection name for any docName
     * @param {object} opts
     * @param {string} opts.docName
     * @returns {string} collectionName
     */
    _getCollectionName({ docName }: {
        docName: string;
    }): string;
    /**
     * Apply a $query and get one document from MongoDB.
     * @param {object} query
     * @returns {Promise<object>}
     */
    get(query: object): Promise<object>;
    /**
     * Store one document in MongoDB.
     * @param {object} query
     * @param {object} values
     * @returns {Promise<object>} Stored document
     */
    put(query: object, values: object): Promise<object>;
    /**
     * Removes all documents that fit the $query
     * @param {object} query
     * @returns {Promise<object>} Contains status of the operation
     */
    del(query: object): Promise<object>;
    /**
     * Get all or at least $opts.limit documents that fit the $query.
     * @param {object} query
     * @param {object} [opts]
     * @param {number} [opts.limit]
     * @param {boolean} [opts.reverse]
     * @returns {Promise<Array<object>>}
     */
    readAsCursor(query: object, opts?: {
        limit?: number | undefined;
        reverse?: boolean | undefined;
    } | undefined): Promise<Array<object>>;
    /**
     * Close connection to MongoDB instance.
     */
    close(): Promise<void>;
    /**
     * Get all collection names stored on the MongoDB instance.
     * @returns {Promise<string[]>}
     */
    getCollectionNames(): Promise<string[]>;
    /**
     * Delete database
     */
    flush(): Promise<void>;
    /**
     * Delete collection
     * @param {string} collectionName
     */
    dropCollection(collectionName: string): Promise<boolean>;
}
import { MongoClient } from "mongodb";
//# sourceMappingURL=mongo-adapter.d.ts.map