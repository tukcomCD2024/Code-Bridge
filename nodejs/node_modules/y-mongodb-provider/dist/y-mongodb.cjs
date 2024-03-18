'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var Y = require('yjs');
var binary = require('lib0/dist/binary.cjs');
var promise = require('lib0/dist/promise.cjs');
var mongodb = require('mongodb');
var encoding = require('lib0/dist/encoding.cjs');
var decoding = require('lib0/dist/decoding.cjs');
var buffer = require('buffer');

function _interopNamespace(e) {
	if (e && e.__esModule) return e;
	var n = Object.create(null);
	if (e) {
		Object.keys(e).forEach(function (k) {
			if (k !== 'default') {
				var d = Object.getOwnPropertyDescriptor(e, k);
				Object.defineProperty(n, k, d.get ? d : {
					enumerable: true,
					get: function () { return e[k]; }
				});
			}
		});
	}
	n["default"] = e;
	return Object.freeze(n);
}

var Y__namespace = /*#__PURE__*/_interopNamespace(Y);
var binary__namespace = /*#__PURE__*/_interopNamespace(binary);
var promise__namespace = /*#__PURE__*/_interopNamespace(promise);
var encoding__namespace = /*#__PURE__*/_interopNamespace(encoding);
var decoding__namespace = /*#__PURE__*/_interopNamespace(decoding);

function parseMongoDBConnectionString(connectionString) {
	const url = new URL(connectionString);
	const database = url.pathname.slice(1);
	url.pathname = '/';

	return {
		database,
		linkWithoutDatabase: url.toString(),
	};
}
class MongoAdapter {
	/**
	 * Create a MongoAdapter instance.
	 * @param {string} connectionString
	 * @param {object} opts
	 * @param {string} opts.collection Name of the collection where all documents are stored.
	 * @param {boolean} opts.multipleCollections When set to true, each document gets an own
	 * collection (instead of all documents stored in the same one).
	 * When set to true, the option $collection gets ignored.
	 */
	constructor(connectionString, { collection, multipleCollections }) {
		this.collection = collection;
		this.multipleCollections = multipleCollections;
		const connectionParams = parseMongoDBConnectionString(connectionString);
		this.mongoUrl = connectionParams.linkWithoutDatabase;
		this.databaseName = connectionParams.database;
		this.client = new mongodb.MongoClient(this.mongoUrl);
		/*
			client.connect() is optional since v4.7
			"However, MongoClient.connect can still be called manually and remains useful for
			learning about misconfiguration (auth, server not started, connection string correctness)
			early in your application's startup."

			I will not use it for now, but may change that in the future.
		*/
		this.db = this.client.db(this.databaseName);
	}

	/**
	 * Get the MongoDB collection name for any docName
	 * @param {object} opts
	 * @param {string} opts.docName
	 * @returns {string} collectionName
	 */
	_getCollectionName({ docName }) {
		if (this.multipleCollections) {
			return docName;
		} else {
			return this.collection;
		}
	}

	/**
	 * Apply a $query and get one document from MongoDB.
	 * @param {object} query
	 * @returns {Promise<object>}
	 */
	get(query) {
		const collection = this.db.collection(this._getCollectionName(query));
		return collection.findOne(query);
	}

	/**
	 * Store one document in MongoDB.
	 * @param {object} query
	 * @param {object} values
	 * @returns {Promise<object>} Stored document
	 */
	async put(query, values) {
		if (!query.docName || !query.version || !values.value) {
			throw new Error('Document and version must be provided');
		}

		const collection = this.db.collection(this._getCollectionName(query));

		await collection.updateOne(query, { $set: values }, { upsert: true });
		return this.get(query);
	}

	/**
	 * Removes all documents that fit the $query
	 * @param {object} query
	 * @returns {Promise<object>} Contains status of the operation
	 */
	del(query) {
		const collection = this.db.collection(this._getCollectionName(query));

		/*
			Note from mongodb v4.7 release notes:
			"It's a known limitation that explicit sessions (client.startSession) and
			initializeOrderedBulkOp, initializeUnorderedBulkOp cannot be used until
			MongoClient.connect is first called.
			Look forward to a future patch release that will correct these inconsistencies."

			I dont know yet if this is a problem for me here.
		*/
		const bulk = collection.initializeOrderedBulkOp();
		bulk.find(query).delete();
		return bulk.execute();
	}

	/**
	 * Get all or at least $opts.limit documents that fit the $query.
	 * @param {object} query
	 * @param {object} [opts]
	 * @param {number} [opts.limit]
	 * @param {boolean} [opts.reverse]
	 * @returns {Promise<Array<object>>}
	 */
	readAsCursor(query, opts = {}) {
		const { limit = 0, reverse = false } = opts;

		const collection = this.db.collection(this._getCollectionName(query));

		/** @type {{ clock: 1 | -1, part: 1 | -1 }} */
		const sortQuery = reverse ? { clock: -1, part: 1 } : { clock: 1, part: 1 };
		const curs = collection.find(query).sort(sortQuery).limit(limit);

		return curs.toArray();
	}

	/**
	 * Close connection to MongoDB instance.
	 */
	async close() {
		await this.client.close();
	}

	/**
	 * Get all collection names stored on the MongoDB instance.
	 * @returns {Promise<string[]>}
	 */
	async getCollectionNames() {
		const collectionInfos = await this.db.listCollections().toArray();
		return collectionInfos.map((c) => c.name);
	}

	/**
	 * Delete database
	 */
	async flush() {
		await this.db.dropDatabase();
		await this.client.close();
	}

	/**
	 * Delete collection
	 * @param {string} collectionName
	 */
	dropCollection(collectionName) {
		return this.db.collection(collectionName).drop();
	}
}

const PREFERRED_TRIM_SIZE = 400;
const MAX_DOCUMENT_SIZE = 15000000; // ~15MB (plus space for metadata)

/**
 * Remove all documents from db with Clock between $from and $to
 *
 * @param {any} db
 * @param {string} docName
 * @param {number} from Greater than or equal
 * @param {number} to lower than (not equal)
 * @return {Promise<void>}
 */
const clearUpdatesRange = async (db, docName, from, to) =>
	db.del({
		docName,
		clock: {
			$gte: from,
			$lt: to,
		},
	});

/**
 * Create a unique key for a update message.
 * @param {string} docName
 * @param {number} [clock] must be unique
 * @return {Object} [opts.version, opts.docName, opts.action, opts.clock]
 */
const createDocumentUpdateKey = (docName, clock) => {
	if (clock !== undefined) {
		return {
			version: 'v1',
			action: 'update',
			docName,
			clock,
		};
	} else {
		return {
			version: 'v1',
			action: 'update',
			docName,
		};
	}
};

/**
 * We have a separate state vector key so we can iterate efficiently over all documents
 * @param {string} docName
 * @return {Object} [opts.docName, opts.version]
 */
const createDocumentStateVectorKey = (docName) => ({
	docName,
	version: 'v1_sv',
});

/**
 * @param {string} docName
 * @param {string} metaKey
 * @return {Object} [opts.docName, opts.version, opts.docType, opts.metaKey]
 */
const createDocumentMetaKey = (docName, metaKey) => ({
	version: 'v1',
	docName,
	metaKey: `meta_${metaKey}`,
});

/**
 * @param {any} db
 * @param {object} query
 * @param {object} opts
 * @return {Promise<any[]>}
 */
const _getMongoBulkData = (db, query, opts) => db.readAsCursor(query, opts);

/**
 * @param {any} db
 * @return {Promise<any>}
 */
const flushDB = (db) => db.flush();

/**
 * Convert the mongo document array to an array of values (as buffers)
 *
 * @param {any[]} docs
 * @return {Buffer[]}
 */
const _convertMongoUpdates = (docs) => {
	if (!Array.isArray(docs) || !docs.length) return [];

	const updates = [];
	for (let i = 0; i < docs.length; i++) {
		const doc = docs[i];
		if (!doc.part) {
			updates.push(doc.value.buffer);
		} else if (doc.part === 1) {
			// merge the docs together that got split because of mongodb size limits
			const parts = [doc.value.buffer];
			let j;
			let currentPartId = doc.part;
			for (j = i + 1; j < docs.length; j++) {
				const part = docs[j];
				if (part.clock === doc.clock) {
					if (currentPartId !== part.part - 1) {
						throw new Error('Couldnt merge updates together because a part is missing!');
					}
					parts.push(part.value.buffer);
					currentPartId = part.part;
				} else {
					break;
				}
			}
			updates.push(...parts);
		}
	}
	return updates;
};
/**
 * Get all document updates for a specific document.
 *
 * @param {any} db
 * @param {string} docName
 * @param {any} [opts]
 * @return {Promise<any[]>}
 */
const getMongoUpdates = async (db, docName, opts = {}) => {
	const docs = await _getMongoBulkData(db, createDocumentUpdateKey(docName), opts);
	return _convertMongoUpdates(docs);
};

/**
 * @param {any} db
 * @param {string} docName
 * @return {Promise<number>} Returns -1 if this document doesn't exist yet
 */
const getCurrentUpdateClock = (db, docName) =>
	_getMongoBulkData(
		db,
		{
			...createDocumentUpdateKey(docName, 0),
			clock: {
				$gte: 0,
				$lt: binary__namespace.BITS32,
			},
		},
		{ reverse: true, limit: 1 },
	).then((updates) => {
		if (updates.length === 0) {
			return -1;
		} else {
			return updates[0].clock;
		}
	});

/**
 * @param {any} db
 * @param {string} docName
 * @param {Uint8Array} sv state vector
 * @param {number} clock current clock of the document so we can determine
 * when this statevector was created
 */
const writeStateVector = async (db, docName, sv, clock) => {
	const encoder = encoding__namespace.createEncoder();
	encoding__namespace.writeVarUint(encoder, clock);
	encoding__namespace.writeVarUint8Array(encoder, sv);
	await db.put(createDocumentStateVectorKey(docName), {
		value: encoding__namespace.toUint8Array(encoder),
	});
};

/**
 * @param {any} db
 * @param {string} docName
 * @param {Uint8Array} update
 * @return {Promise<number>} Returns the clock of the stored update
 */
const storeUpdate = async (db, docName, update) => {
	const clock = await getCurrentUpdateClock(db, docName);
	if (clock === -1) {
		// make sure that a state vector is always written, so we can search for available documents
		const ydoc = new Y__namespace.Doc();
		Y__namespace.applyUpdate(ydoc, update);
		const sv = Y__namespace.encodeStateVector(ydoc);
		await writeStateVector(db, docName, sv, 0);
	}

	// mongodb has a maximum document size of 16MB;
	//  if our buffer exceeds it, we store the update in multiple documents
	if (update.length <= MAX_DOCUMENT_SIZE) {
		await db.put(createDocumentUpdateKey(docName, clock + 1), {
			value: update,
		});
	} else {
		const totalChunks = Math.ceil(update.length / MAX_DOCUMENT_SIZE);

		const putPromises = [];
		for (let i = 0; i < totalChunks; i++) {
			const start = i * MAX_DOCUMENT_SIZE;
			const end = Math.min(start + MAX_DOCUMENT_SIZE, update.length);
			const chunk = update.subarray(start, end);

			putPromises.push(
				db.put({ ...createDocumentUpdateKey(docName, clock + 1), part: i + 1 }, { value: chunk }),
			);
		}

		await Promise.all(putPromises);
	}

	return clock + 1;
};

/**
 * For now this is a helper method that creates a Y.Doc and then re-encodes a document update.
 * In the future this will be handled by Yjs without creating a Y.Doc (constant memory consumption).
 *
 * @param {Array<Uint8Array>} updates
 * @return {{update:Uint8Array, sv: Uint8Array}}
 */
const mergeUpdates = (updates) => {
	const ydoc = new Y__namespace.Doc();
	ydoc.transact(() => {
		for (let i = 0; i < updates.length; i++) {
			Y__namespace.applyUpdate(ydoc, updates[i]);
		}
	});
	return { update: Y__namespace.encodeStateAsUpdate(ydoc), sv: Y__namespace.encodeStateVector(ydoc) };
};

/**
 * @param {Uint8Array} buf
 * @return {{ sv: Uint8Array, clock: number }}
 */
const decodeMongodbStateVector = (buf) => {
	let decoder;
	if (buffer.Buffer.isBuffer(buf)) {
		decoder = decoding__namespace.createDecoder(buf);
	} else if (buffer.Buffer.isBuffer(buf?.buffer)) {
		decoder = decoding__namespace.createDecoder(buf.buffer);
	} else {
		throw new Error('No buffer provided at decodeMongodbStateVector()');
	}
	const clock = decoding__namespace.readVarUint(decoder);
	const sv = decoding__namespace.readVarUint8Array(decoder);
	return { sv, clock };
};

/**
 * @param {any} db
 * @param {string} docName
 */
const readStateVector = async (db, docName) => {
	const doc = await db.get({ ...createDocumentStateVectorKey(docName) });
	if (!doc?.value) {
		// no state vector created yet or no document exists
		return { sv: null, clock: -1 };
	}
	return decodeMongodbStateVector(doc.value);
};

const getAllSVDocs = async (db) => db.readAsCursor({ version: 'v1_sv' });

/**
 * Merge all MongoDB documents of the same yjs document together.
 * @param {any} db
 * @param {string} docName
 * @param {Uint8Array} stateAsUpdate
 * @param {Uint8Array} stateVector
 * @return {Promise<number>} returns the clock of the flushed doc
 */
const flushDocument = async (db, docName, stateAsUpdate, stateVector) => {
	const clock = await storeUpdate(db, docName, stateAsUpdate);
	await writeStateVector(db, docName, stateVector, clock);
	await clearUpdatesRange(db, docName, 0, clock);
	return clock;
};

class MongodbPersistence {
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
	constructor(location, opts = {}) {
		const { collectionName = 'yjs-writings', multipleCollections = false, flushSize = 400 } = opts;
		if (typeof collectionName !== 'string' || !collectionName) {
			throw new Error(
				'Constructor option "collectionName" is not a valid string. Either dont use this option (default is "yjs-writings") or use a valid string! Take a look into the Readme for more information: https://github.com/MaxNoetzold/y-mongodb-provider#persistence--mongodbpersistenceconnectionlink-string-options-object',
			);
		}
		if (typeof multipleCollections !== 'boolean') {
			throw new Error(
				'Constructor option "multipleCollections" is not a boolean. Either dont use this option (default is "false") or use a valid boolean! Take a look into the Readme for more information: https://github.com/MaxNoetzold/y-mongodb-provider#persistence--mongodbpersistenceconnectionlink-string-options-object',
			);
		}
		if (typeof flushSize !== 'number' || flushSize <= 0) {
			throw new Error(
				'Constructor option "flushSize" is not a valid number. Either dont use this option (default is "400") or use a valid number larger than 0! Take a look into the Readme for more information: https://github.com/MaxNoetzold/y-mongodb-provider#persistence--mongodbpersistenceconnectionlink-string-options-object',
			);
		}
		const db = new MongoAdapter(location, {
			collection: collectionName,
			multipleCollections,
		});
		this.flushSize = flushSize ?? PREFERRED_TRIM_SIZE;
		this.multipleCollections = multipleCollections;

		// scope the queue of the transaction to each docName
		// -> this should allow concurrency for different rooms
		// Idea and adjusted code from: https://github.com/fadiquader/y-mongodb/issues/10
		this.tr = {};

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
		this._transact = (docName, f) => {
			if (!this.tr[docName]) {
				this.tr[docName] = promise__namespace.resolve();
			}

			const currTr = this.tr[docName];
			let nextTr = null;

			nextTr = (async () => {
				await currTr;

				let res = /** @type {any} */ (null);
				try {
					res = await f(db);
				} catch (err) {
					// eslint-disable-next-line no-console
					console.warn('Error during saving transaction', err);
				}

				// once the last transaction for a given docName resolves, remove it from the queue
				if (this.tr[docName] === nextTr) {
					delete this.tr[docName];
				}

				return res;
			})();

			this.tr[docName] = nextTr;

			return this.tr[docName];
		};
	}

	/**
	 * Create a Y.Doc instance with the data persistet in mongodb.
	 * Use this to temporarily create a Yjs document to sync changes or extract data.
	 *
	 * @param {string} docName
	 * @return {Promise<Y.Doc>}
	 */
	getYDoc(docName) {
		return this._transact(docName, async (db) => {
			const updates = await getMongoUpdates(db, docName);
			const ydoc = new Y__namespace.Doc();
			ydoc.transact(() => {
				for (let i = 0; i < updates.length; i++) {
					Y__namespace.applyUpdate(ydoc, updates[i]);
					updates[i] = null;
				}
			});
			if (updates.length > this.flushSize) {
				await flushDocument(db, docName, Y__namespace.encodeStateAsUpdate(ydoc), Y__namespace.encodeStateVector(ydoc));
			}
			return ydoc;
		});
	}

	/**
	 * Store a single document update to the database.
	 *
	 * @param {string} docName
	 * @param {Uint8Array} update
	 * @return {Promise<number>} Returns the clock of the stored update
	 */
	storeUpdate(docName, update) {
		return this._transact(docName, (db) => storeUpdate(db, docName, update));
	}

	/**
	 * The state vector (describing the state of the persisted document - see https://github.com/yjs/yjs#Document-Updates) is maintained in a separate field and constantly updated.
	 *
	 * This allows you to sync changes without actually creating a Yjs document.
	 *
	 * @param {string} docName
	 * @return {Promise<Uint8Array>}
	 */
	getStateVector(docName) {
		return this._transact(docName, async (db) => {
			const { clock, sv } = await readStateVector(db, docName);
			let curClock = -1;
			if (sv !== null) {
				curClock = await getCurrentUpdateClock(db, docName);
			}
			if (sv !== null && clock === curClock) {
				return sv;
			} else {
				// current state vector is outdated
				const updates = await getMongoUpdates(db, docName);
				const { update, sv: newSv } = mergeUpdates(updates);
				await flushDocument(db, docName, update, newSv);
				return newSv;
			}
		});
	}

	/**
	 * Get the differences directly from the database.
	 * The same as Y.encodeStateAsUpdate(ydoc, stateVector).
	 * @param {string} docName
	 * @param {Uint8Array} stateVector
	 */
	async getDiff(docName, stateVector) {
		const ydoc = await this.getYDoc(docName);
		return Y__namespace.encodeStateAsUpdate(ydoc, stateVector);
	}

	/**
	 * Delete a document, and all associated data from the database.
	 * When option multipleCollections is set, it removes the corresponding collection
	 * @param {string} docName
	 * @return {Promise<void>}
	 */
	clearDocument(docName) {
		return this._transact(docName, async (db) => {
			if (!this.multipleCollections) {
				await db.del(createDocumentStateVectorKey(docName));
				await clearUpdatesRange(db, docName, 0, binary__namespace.BITS32);
			} else {
				await db.dropCollection(docName);
			}
		});
	}

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
	setMeta(docName, metaKey, value) {
		/*	Unlike y-leveldb, we simply store the value here without encoding
	 		 it in a buffer beforehand. */
		return this._transact(docName, async (db) => {
			await db.put(createDocumentMetaKey(docName, metaKey), { value });
		});
	}

	/**
	 * Retrieve a store meta value from the database. Returns undefined if the
	 * metaKey doesn't exist.
	 *
	 * @param {string} docName
	 * @param {string} metaKey
	 * @return {Promise<any>}
	 */
	getMeta(docName, metaKey) {
		return this._transact(docName, async (db) => {
			const res = await db.get({
				...createDocumentMetaKey(docName, metaKey),
			});
			if (!res?.value) {
				return undefined;
			}
			return res.value;
		});
	}

	/**
	 * Delete a store meta value.
	 *
	 * @param {string} docName
	 * @param {string} metaKey
	 * @return {Promise<any>}
	 */
	delMeta(docName, metaKey) {
		return this._transact(docName, (db) =>
			db.del({
				...createDocumentMetaKey(docName, metaKey),
			}),
		);
	}

	/**
	 * Retrieve the names of all stored documents.
	 *
	 * @return {Promise<string[]>}
	 */
	getAllDocNames() {
		return this._transact('global', async (db) => {
			if (this.multipleCollections) {
				// get all collection names from db
				return db.getCollectionNames();
			} else {
				// when all docs are stored in the same collection we just need to get all
				//  statevectors and return their names
				const docs = await getAllSVDocs(db);
				return docs.map((doc) => doc.docName);
			}
		});
	}

	/**
	 * Retrieve the state vectors of all stored documents.
	 * You can use this to sync two y-leveldb instances.
	 * !Note: The state vectors might be outdated if the associated document
	 * is not yet flushed. So use with caution.
	 * @return {Promise<{ name: string, sv: Uint8Array, clock: number }[]>}
	 * @todo may not work?
	 */
	getAllDocStateVectors() {
		return this._transact('global', async (db) => {
			const docs = await getAllSVDocs(db);
			return docs.map((doc) => {
				const { sv, clock } = decodeMongodbStateVector(doc.value);
				return { name: doc.docName, sv, clock };
			});
		});
	}

	/**
	 * Internally y-mongodb stores incremental updates. You can merge all document
	 * updates to a single entry. You probably never have to use this.
	 * It is done automatically every $options.flushsize (default 400) transactions.
	 *
	 * @param {string} docName
	 * @return {Promise<void>}
	 */
	flushDocument(docName) {
		return this._transact(docName, async (db) => {
			const updates = await getMongoUpdates(db, docName);
			const { update, sv } = mergeUpdates(updates);
			await flushDocument(db, docName, update, sv);
		});
	}

	/**
	 * Delete the whole yjs mongodb
	 * @return {Promise<void>}
	 */
	flushDB() {
		return this._transact('global', async (db) => {
			await flushDB(db);
		});
	}

	/**
	 * Closes open database connection
	 * @returns {Promise<void>}
	 */
	destroy() {
		return this._transact('global', async (db) => {
			await db.close();
		});
	}
}

exports.MongodbPersistence = MongodbPersistence;
//# sourceMappingURL=y-mongodb.cjs.map
