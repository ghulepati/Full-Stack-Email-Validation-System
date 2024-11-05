package sai.ghule.om;

/**
 * * Author: Sai Ajay Ghule
 * * Andrew ID: sghule
 * * Project 4 Task 2
 *
 * DashboardHelper class for handling the retrieval and analysis of email validation data from MongoDB.
 * This class is designed to work with the "Test4" collection within the "Test" database and provides
 * various static methods to process and analyze this data for a dashboard view.
 *
 * The class includes methods to connect to MongoDB, fetch documents, and process them to extract
 * information such as email domain, validation status, and count of different types of emails.
 * It utilizes static fields to hold the results of analysis such as valid email count, free email count,
 * and other aggregates. The class uses PriorityQueue to hold top domains and dates, which allows for
 * easy retrieval of the most common elements.
 *
 * Why a separate DashboardHelper Class?
 * Firstly because, last time my 5 marks were cut for this and secondly,
 * A separate DashboardHelper class is used to encapsulate all the logic related to data fetching and
 * analysis for the dashboard. This follows the Single Responsibility Principle, one of the SOLID principles
 * of object-oriented design, which states that a class should have only one reason to change. It keeps the
 * data analysis concerns separate from the servlet code, which is responsible for handling HTTP requests
 * and responses. This separation also makes the code more maintainable and testable.
 *
 * The main methods of DashboardHelper are:
 * - getFromMongo: Connects to the MongoDB database and fetches the data.
 * - analyzeDashboardData: Processes each document from the database to extract and analyze data.
 * - processDocument: Extracts information from a single document and updates respective maps.
 * - updateCountsAndLogs: Updates the counts of different email types and logs specific information.
 * - updateQueueFromMap: Takes entries from a map and updates the queues with top entries.
 * - fetchDomains, fetchDate, fetchIsValid, fetchIsFree, fetchTotalEmails, fetchValidationSuccessRate:
 *   Methods for retrieving the results of analysis for use in the dashboard view.
 *
 * I have done this project with the help of ChatGPT-4.
 */

import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;


/**
 * DashboardHelper provides utility methods for retrieving and processing
 * email validation data for dashboard presentation.
 */
public class DashboardHelper {
    // Field names used within the MongoDB documents
    private static final String EMAIL_FIELD = "email";
    private static final String DATE_FIELD = "date";
    private static final String IS_VALID_FIELD = "isValid";
    private static final String IS_FREE_FIELD = "isFree";
    private static final String IS_DISPOSABLE_FIELD = "isDisposable";
    private static final String IS_ROLE_FIELD = "isRole";

    // Queues to hold the most common email domains and dates of submission
    private static PriorityQueue<String> emailQueue = new PriorityQueue<>();
    private static PriorityQueue<String> dateQueue = new PriorityQueue<>();

    // List to store log entries from the database for display on the dashboard
    private static List<String> databaseLogs = new ArrayList<>();

    // Counters to keep track of the number of valid emails and free emails processed
    private static int isValidCount = 0;
    private static int isFreeCount = 0;

    // Counter for the total number of emails processed, used to calculate success rate
    private static int totalEmailsCount = 0;

    /**
     * Retrieves the current list of database log entries.
     * @return A list of database log strings.
     */
    public static List<String> getDatabaseLogs() {
        return databaseLogs;
    }

    /**
     * Sets the current list of database logs to a new list of logs.
     * @param logs A list of log strings to replace the existing logs.
     */
    public static void setDatabaseLogs(List<String> logs) {
        databaseLogs = logs;
    }

    /**
     * Retrieves and removes the next most common domain from the queue.
     * @return The next most common email domain.
     */
    public static String fetchDomains() {
        return emailQueue.poll();
    }

    /**
     * Retrieves and removes the next most frequent date from the queue.
     * @return The next date with the highest user traffic.
     */
    public static String fetchDate() {
        return dateQueue.poll();
    }

    /**
     * Fetches the count of emails marked as valid.
     * @return The total count of valid emails.
     */
    public static int fetchIsValid() {
        return isValidCount;
    }

    /**
     * Fetches the count of emails marked as free.
     * @return The total count of free emails.
     */
    public static int fetchIsFree() {
        return isFreeCount;
    }

    /**
     * Fetches the total number of emails processed.
     * @return The total count of emails processed for validation.
     */
    public static int fetchTotalEmails() {
        return totalEmailsCount;
    }

    /**
     * Calculates the validation success rate as a percentage.
     * @return The percentage of emails that were successfully validated.
     */
    public static double fetchValidationSuccessRate() {
        return isValidCount / (double) totalEmailsCount * 100; // Convert to percentage
    }


    /**
     * Retrieves data from MongoDB for dashboard analysis.
     *
     * This method connects to the MongoDB database specified by the {@code mongoURL},
     * retrieves documents from the collection "Test4", and analyzes the dashboard data.
     *
     * @throws Exception if an error occurs during the MongoDB operation
     */
    public static void getFromMongo()
    {

        String mongoURL = "mongodb+srv://ghulepatilsaee:saee123@cluster0.npzuuju.mongodb.net/?retryWrites=true&w=majority";

        try (MongoClient mongoClient = MongoClients.create(mongoURL))
        {
            System.out.println("Connected to MongoDB."); //debugging

            // Connect to MongoDB and get the database and collection
            MongoDatabase db = mongoClient.getDatabase("Test");
            MongoCollection<Document> mongoCollection = db.getCollection("Test4");

            FindIterable<Document> documents = mongoCollection.find();
            System.out.println("Fetched documents from MongoDB.");//debugging

            MongoCursor<Document> mongoCursor = documents.iterator();
            if (!mongoCursor.hasNext()) {
                System.out.println("The cursor has no data.");
            }
            // Analyze dashboard data from MongoDB documents
            analyzeDashboardData(mongoCursor);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while connecting to MongoDB: " + e.getMessage());
        }
    }


    /**
     * Analyzes dashboard data from MongoDB documents.
     *
     * This method iterates over the MongoDB cursor containing dashboard documents,
     * processes each document to extract domain and date information, and updates
     * relevant counts and logs.
     *
     * @param mongoCursor the cursor containing MongoDB documents to analyze
     */
    private static void analyzeDashboardData(MongoCursor<Document> mongoCursor) {
        // Maps to store domain and date information
        Map<String, Integer> domainMap = new HashMap<>();
        Map<String, Integer> dateMap = new HashMap<>();

        // Iterate over MongoDB documents
        while (mongoCursor.hasNext()) {
            Document document = mongoCursor.next();
            System.out.println("Processing document: " + document.toJson());//
            // Process document to extract domain and date information
            processDocument(document, domainMap, dateMap);

            // Update counts and logs based on the document
            updateCountsAndLogs(document);
        }
        System.out.println("Finished processing all documents.");//
        System.out.println("Top domains: " + emailQueue);
        System.out.println("Top dates: " + dateQueue);
        System.out.println("Valid count: " + isValidCount);
        System.out.println("Free count: " + isFreeCount);//
        // Update priority queues with domain and date information
        updateQueueFromMap(domainMap, emailQueue);
        updateQueueFromMap(dateMap, dateQueue);
    }


    /**
     * Processes a dashboard document to extract domain and date information.
     *
     * This method extracts the email domain and date from the provided MongoDB document
     * and updates the corresponding domain and date maps with the extracted information.
     *
     * @param document the MongoDB document to process
     * @param domainMap the map to store domain information
     * @param dateMap the map to store date information
     */
    private static void processDocument(Document document, Map<String, Integer> domainMap, Map<String, Integer> dateMap) {
        System.out.println("Processing document: " + document.toJson());//

        // Existing code for extracting email domain...
        String email = document.getString(EMAIL_FIELD);
        Optional.ofNullable(email)
                .map(e -> e.split("@"))
                .filter(parts -> parts.length >= 2)
                .map(parts -> parts[1])
                .ifPresent(domain -> {
                    System.out.println("Extracted domain: " + domain);
                    incrementMapCount(domainMap, domain);
                });

        // Existing code for extracting date...
        String date = document.getString(DATE_FIELD);
        System.out.println("Extracted date: " + date);//
        incrementMapCount(dateMap, date);
    }


    /**
     * Updates counts and logs based on the provided document.
     *
     * This method updates the counts of valid and free emails based on the 'isValid' and 'isFree'
     * fields in the provided MongoDB document. It also adds a log entry to the databaseLogs list
     * containing information about the email, such as date, email address, validity, and freeness.
     *
     * @param document the MongoDB document to process
     */

    private static void updateCountsAndLogs(Document document) {
        Object isValidObj = document.get("isValid");
        Object isFreeObj = document.get("isFree");
        Object isDisposableObj = document.get("isDisposable");
        Object isRoleObj = document.get("isRole");

        boolean isValid = parseBooleanField(isValidObj);
        boolean isFree = parseBooleanField(isFreeObj);
        boolean isDisposable = parseBooleanField(isDisposableObj);
        boolean isRole = parseBooleanField(isRoleObj);

        // Increment the count of valid and free emails if applicable
        if (isValid) {
            isValidCount++;
        }
        if (isFree) {
            isFreeCount++;
        }
        totalEmailsCount++;
        // Existing code to add a log entry
        String email = document.getString(EMAIL_FIELD);
        String date = document.getString(DATE_FIELD);
        databaseLogs.add(date + " " + email + " " + isValid + " " + isFree + " " + isDisposable + " " + isRole);
    }

    private static boolean parseBooleanField(Object fieldObj) {
        if (fieldObj instanceof Boolean) {
            return (Boolean) fieldObj;
        } else if (fieldObj instanceof Document) {
            Document fieldDoc = (Document) fieldObj;
            return fieldDoc.getBoolean("value", false);
        } else if (fieldObj instanceof String) {
            return Boolean.parseBoolean((String) fieldObj);
        }
        return false;
    }



    /**
     * Increments the count associated with the specified key in the given map.
     *
     * This method increments the count associated with the specified key in the provided map
     * by 1. If the key is not present in the map, it initializes the count to 1.
     *
     * @param map the map containing the counts
     * @param key the key whose count should be incremented
     */
    private static void incrementMapCount(Map<String, Integer> map, String key) {
        int newCount = map.getOrDefault(key, 0) + 1;
        map.put(key, newCount);
        System.out.println("Updated count for key '" + key + "': " + newCount); //
    }


    /**
     * Updates the given queue from the provided map based on the map's values.
     *
     * This method updates the given queue from the provided map by adding elements
     * based on the map's values. The queue is assumed to be a Max Heap with a custom
     * comparator based on the map's values. It clears the existing elements in the
     * queue and adds the top 3 elements from the map to the queue.
     *
     * @param map the map containing the elements and their associated values
     * @param queue the queue to be updated
     */
    private static void updateQueueFromMap(Map<String, Integer> map, Queue<String> queue) {
        System.out.println("Updating queue from map. Map contents: " + map);
        queue.clear();
        map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEachOrdered(e -> {
                    queue.add(e.getKey());
                    System.out.println("Added to queue: " + e.getKey() + " with count: " + e.getValue());
                });
    }


}
