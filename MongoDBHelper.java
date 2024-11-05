package sai.ghule.om;
/**
 *  * Author: Sai Ajay Ghule
 *  * Andrew ID: sghule
 *  * Project 4 Task 2
 *
 * MongoDBHelper facilitates interaction with the MongoDB database.
 * It provides utility functions to connect to MongoDB, perform operations such as insertions,
 * and retrieve collections for further operations. The class implements the AutoCloseable
 * interface to ensure proper cleanup of resources, such as the MongoDB client connection.
 *
 * Why am I using a MongoDB Helper Class?
 * Again I don't want to lose marks and by using this helper class,
 * database connection details and operations are encapsulated within a single class.
 * This encapsulation makes the code cleaner and separates the database logic from
 * the business logic of the application. It also provides a single point of modification if the
 * database connection details or operations need to change, adhering to the Single Responsibility
 * Principle.
 *
 * The MongoDBHelper class is designed to be used as a resource in a try-with-resources statement,
 * which ensures that the close method is called automatically at the end of the block, even if an
 * exception is thrown, thereby avoiding potential resource leaks.
 *
 * The use of a separate MongoDBHelper class also simplifies unit testing by allowing mock
 * implementations to be used in place of actual database connections.

 * Acknowledgement: Assistance provided by ChatGPT-4 from OpenAI.
 */
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDBHelper implements AutoCloseable {

    private static final String MONGO_URL = "mongodb+srv://ghulepatilsaee:saee123@cluster0.npzuuju.mongodb.net/?retryWrites=true&w=majority";
    private static final String DATABASE_NAME = "Test";
    private static final String COLLECTION_NAME = "Test4";



    private MongoClient mongoClient;
    private MongoDatabase database;
    private static MongoCollection<Document> collection;



    /**
     * Constructs a new MongoDBHelper object and initializes the MongoDB client connection.
     */
    public MongoDBHelper() {
        mongoClient = MongoClients.create(MONGO_URL);
        database = mongoClient.getDatabase(DATABASE_NAME);
        collection = database.getCollection(COLLECTION_NAME);
    }


    /**
     * Inserts a document into the MongoDB collection.
     *
     * @param document the document to insert
     */
    public void insertDocument(Document document) {
        collection.insertOne(document);
    }



    /**
     * Retrieves a specified collection from the database.
     *
     * @param collectionName the name of the collection to retrieve
     * @return the MongoCollection<Document> object representing the specified collection
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        // Get the collection from the database.
        return database.getCollection(collectionName);
    }




    /**
     * Closes the MongoDB client connection.
     */
    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
