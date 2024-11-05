package sai.ghule.om;
/**
 * * Author: Sai Ajay Ghule
 * * Andrew ID: sghule
 * * Project 4 Task 2
 *
 * The EmailVerificationModel class encapsulates the functionality required to validate an email address.
 * It interacts with an external API to validate the provided email and then records the validation results
 * along with additional metadata into a MongoDB database for further analysis.
 *
 * The primary method, getInfo, constructs the request to the email validation API, processes the response,
 * and organizes the data in a structured JSON format. It also handles the persistence of validation results
 * by inserting a new document into the MongoDB collection.
 *
 * This class aids in abstracting the complexities of API communication and data persistence, allowing other
 * parts of the application to perform email validation with minimal concern for the underlying implementation
 * details.
 *
 * The class contains several helper methods:
 * - getString: Fetches the response from an API endpoint as a String.
 * - extractValidationField: Extracts validation data from the JSON response.
 * - buildErrorResponse: Constructs a JSON response for error handling.
 * - pushToMongo: Inserts the validation results and metadata into the MongoDB database.
 * - buildResponse: Constructs the final JSON response containing the validation results.
 *
 * Utilization of this class requires a valid API key for the email validation service and an accessible
 * MongoDB instance with proper configuration as defined in the MongoDBHelper class.
 *
 * Acknowledgment: Assistance provided by ChatGPT-4 from OpenAI.
 */



import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import org.bson.Document;

import static sai.ghule.om.DashboardHelper.getFromMongo;

public class EmailVerificationModel {


    /**
     * Retrieves email validation information from a third-party API using the provided email address.
     *
     * This method constructs the API URL using the endpoint and API key, makes a request to the API,
     * and processes the response to extract validation information such as whether the email is valid,
     * free, disposable, or a role-based email address. It then pushes the validation results to a MongoDB
     * database along with the current date and time. Finally, it builds and returns a JSON response
     * containing the validation results.
     *
     * @param email the email address to be validated
     * @return a JSON response containing the validation results
     */


    public static String getInfo(String email) {// Construct the API URL
        String apiEndpoint = "https://emailvalidation.abstractapi.com/v1/";
        String apiKey = "41d96ebc65264e25ad4ee668ffda2dc6";
        String apiUrl = apiEndpoint + "?api_key=" + apiKey + "&email=" + email;

        // Fetch API response
        String apiResponse = getString(apiUrl);
        if (apiResponse == null) {
            return buildErrorResponse("API response was null.");
        }

        // Parse API response
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(apiResponse);
        if (jsonResponse == null) {
            return buildErrorResponse("Failed to parse API response.");
        }

        // Extract validation fields
        String isValid = extractValidationField(jsonResponse, "is_valid_format");
        String isFree = extractValidationField(jsonResponse, "is_free_email");
        String isDisposable = extractValidationField(jsonResponse, "is_disposable_email");
        String isRole = extractValidationField(jsonResponse, "is_role_email");

        // Get current date and time
        String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        getFromMongo();

        // Push data to MongoDB
        pushToMongo(email, isValid, isFree, isDisposable, isRole, currentDate, currentTime);

        // Build JSON response
        return buildResponse(isValid, isFree, isDisposable, isRole);
    }


    // Helper method to extract fields from the JSON response
    private static String extractValidationField(JSONObject jsonResponse, String key) {
        JSONObject jsonField = (JSONObject) jsonResponse.get(key);
        return jsonField != null ? (String) jsonField.get("text") : "Unknown";
    }

    // Helper method to build error responses
    private static String buildErrorResponse(String errorMessage) {
        JSONObject response = new JSONObject();
        response.put("error", errorMessage);
        return response.toJSONString();
    }

    /**
     * Fetches the response from the specified URL.
     *
     * This method establishes a connection to the provided URL, reads the response
     * from the input stream, and returns the response as a string.
     *
     * @param urlString the URL to fetch the response from
     * @return the response from the URL as a string, or null if an exception occurs
     */
    private static String getString(String urlString) {
        try {
            // Open a connection to the URL
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Read the response from the input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Return the response as a string
            return response.toString();
        } catch (IOException e) {
            // Handle exceptions and return null
            System.out.println(" An exception occurred !!!!!" + e.getMessage());
            return buildErrorResponse("Third-party API unavailable");
            /**
            Why: The third-party API might be down or unreachable due to network issues.
            Handling this scenario prevents your application from crashing or becoming unresponsive and allows you to provide a meaningful response to the user.

            How: The getString method includes a try-catch block to catch IOException, which is typically thrown if the API is unreachable.
            In case of such an exception, a custom error message is returned, informing of the API's unavailability.
             **/

        }
    }





    /**
     * Pushes email validation data to MongoDB.
     *
     * This method creates a document containing email validation data, including email address,
     * validation results, and timestamp. It then inserts the document into MongoDB using the
     * MongoDBHelper class.
     *
     * @param email the email address being validated
     * @param isValid the validation result indicating whether the email format is valid
     * @param isFree the validation result indicating whether the email is from a free email provider
     * @param isDisposable the validation result indicating whether the email is disposable
     * @param isRole the validation result indicating whether the email is a role-based email
     * @param date the current date when the validation occurs
     * @param time the current time when the validation occurs
     */
    private static void pushToMongo(String email, String isValid, String isFree, String isDisposable, String isRole, String date, String time)
    {

        Document document = new Document("time", time)
                .append("email", email)
                .append("isValid", isValid)
                .append("isFree", isFree)
                .append("isDisposable", isDisposable)
                .append("isRole", isRole)
                .append("date", date);

        try (MongoDBHelper dbHelper = new MongoDBHelper()) {
            dbHelper.insertDocument(document);
        }

    }


    /**
     * Builds a JSON response string containing email validation results.
     *
     * This method constructs a JSON object with the provided validation results, including
     * whether the email format is valid, whether it is from a free email provider, whether
     * it is disposable, and whether it is a role-based email. The JSON object is then converted
     * to a string representation using the toJSONString() method.
     *
     * @param isValid a string indicating whether the email format is valid
     * @param isFree a string indicating whether the email is from a free email provider
     * @param isDisposable a string indicating whether the email is disposable
     * @param isRole a string indicating whether the email is a role-based email
     * @return a JSON string representing the email validation results
     */
    private static String buildResponse(String isValid, String isFree, String isDisposable, String isRole)
    {

        // Create a JSON object to store validation results

        JSONObject response = new JSONObject();
        response.put("isValid", isValid);
        response.put("isFree", isFree);
        response.put("isDisposable", isDisposable);
        response.put("isRole", isRole);

        // Convert the JSON object to a string representation
        return response.toJSONString();

    }





}
