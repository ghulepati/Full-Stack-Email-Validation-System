

package sai.ghule.om;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Name : Sai Ajay Ghule
 * Andrew ID : sghule
 * Project 4 Task 2
 *
 * MainActivity class is the main activity of the Android application.
 * It provides functionality for validating email addresses using a server-side API,
 * displaying validation results, and clearing validation results.
 * The activity includes UI elements such as EditText for entering email addresses,
 * TextView for displaying validation results, and Buttons for triggering validation and clearing.
 * This class extends AppCompatActivity and implements various methods for activity lifecycle management.
 * It also contains helper methods for handling button clicks, fetching validation responses from the server,
 * formatting validation output, updating the UI based on the response, and clearing validation results.
 **/



public class MainActivity extends AppCompatActivity {
    EditText emailEditText; // Input field for the user to enter their email
    TextView validationOutputTextView; // Displays validation results
    Button validateButton, clearButton; // Triggers email validation and clears the input/output

    //ImageView appLogoImageView;


    /*
     * This method is called when the activity is first created.
     * It initializes the user interface components and sets up
     * click listeners for the validation and clear buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass onCreate method to perform any necessary initialization.
        super.onCreate(savedInstanceState);

        // Set the content view to the activity_main layout resource.
        setContentView(R.layout.activity_main);

        // Find and initialize the views defined in the activity_main layout.
        emailEditText = findViewById(R.id.email);
        validationOutputTextView = findViewById(R.id.output);
        validateButton = findViewById(R.id.submitButton);
        clearButton = findViewById(R.id.resetButton);

        // Configure the button click listeners for the validate and clear buttons.
        configureButtonClickListeners();
    }


    /*
     * Configures the click listeners for the validate and clear buttons.
     */
    private void configureButtonClickListeners() {
        // Set the click listener for the validate button to trigger email validation.
        validateButton.setOnClickListener(v -> validateEmail());

        // Set the click listener for the clear button to clear validation results.
        clearButton.setOnClickListener(v -> clearValidationResults());
    }


    /*
     * Initiates the email validation process by executing it on a separate thread.
     */
    private void validateEmail() {
        // Create a new single-thread executor to handle the email validation task.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Execute the validation task asynchronously.
        executor.execute(() -> {
            // Fetch the validation response for the entered email.
            String emailValidationResponse = fetchEmailValidationResponse();

            // Update the validation output based on the response received.
            updateValidationOutput(emailValidationResponse);
        });
    }



    /*
     * Fetches the response from the email validation service.
     * Returns a string representing the response.
     */
    private String fetchEmailValidationResponse() {
        String response = ""; // Initialize the response string

        try {
            // Get the email to validate from the emailEditText field
            String emailToValidate = emailEditText.getText().toString();

            // Build the URL for email validation
            URL validationUrl = buildEmailValidationUrl(emailToValidate);

            // Log the validation URL
            Log.d("EmailValidator", "URL: " + validationUrl);

            // Open a connection to the validation URL
            URLConnection connection = validationUrl.openConnection();

            // Set the client type header for the connection
            connection.setRequestProperty("Client-Type", "MobileApp");

            // Read the response from the connection
            BufferedInputStream buffered = new BufferedInputStream(connection.getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = buffered.read(buffer)) != -1) {
                responseBuilder.append(new String(buffer, 0, bytesRead));
            }

            // Convert the response builder to a string
            response = responseBuilder.toString();

            // Log the response received from the server
            Log.d("EmailValidator", "Response: " + response);

        } catch (IOException e) {
            // Handle network-related errors
            Log.e("EmailValidator", "Network Error: ", e);
            response = "Network Error: Unable to connect to the server.";
        } catch (Exception e) {
            // Handle general errors
            Log.e("EmailValidator", "General Error: ", e);
            response = "Error: An unexpected issue occurred.";
        }

        // Return the response string
        return response;
    }


    /*
     * Builds the URL for email validation based on the provided email address.
     * Returns a URL object representing the validation URL.
     */
    private URL buildEmailValidationUrl(String email) throws Exception {
        // Base URL for the email validation service
       // String baseUrl = "http://10.0.2.2:8080/demou-1.0-SNAPSHOT/";
        String baseUrl = "https://studious-guacamole-v66x6jvw9g9pcx6pw-8080.app.github.dev/";
        // Construct the query parameter with the email address
        String queryParam = "EmailValidator?email=" + email;

        // Concatenate the base URL and query parameter to form the complete validation URL
        return new URL(baseUrl + queryParam);
    }

    /*
     * Formats the validation output received from the server and displays it in a table layout.
     * Accepts a JSONObject containing the validation results.
     * Modifies the layout to display the validation results.
     * Returns null.
     */
    private String formatValidationOutput(JSONObject json) {
        // Find the table layout in the activity layout
        TableLayout tableLayout = findViewById(R.id.validationTable);

        // Clear any previous results from the table layout
        tableLayout.removeAllViews();

        // Retrieve validation attributes from the JSON object
        boolean isValid = json.optBoolean("isValid", false);
        boolean isFree = json.optBoolean("isFree", false);
        boolean isDisposable = json.optBoolean("isDisposable", false);
        boolean isRole = json.optBoolean("isRole", false);

        // Add rows to the table layout to display the validation results
        addTableRow(tableLayout, "Valid Format", isValid ? "Yes" : "No");
        addTableRow(tableLayout, "Free Email", isFree ? "Yes" : "No");
        addTableRow(tableLayout, "Disposable Email", isDisposable ? "Yes" : "No");
        addTableRow(tableLayout, "Role Email", isRole ? "Yes" : "No");

        // Indicate that the method execution is complete
        return null;
    }

    private void addTableRow(TableLayout tableLayout, String label, String value) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView labelView = new TextView(this);
        TextView valueView = new TextView(this);

        labelView.setText(label);
        labelView.setGravity(Gravity.START);
        labelView.setPadding(5, 20, 5, 20); // Add padding as needed (left, top, right, bottom)

        valueView.setText(value);
        valueView.setGravity(Gravity.END);
        valueView.setPadding(5, 20, 5, 20); // Add padding as needed (left, top, right, bottom)

        // Setting background color for the row
        row.setBackgroundColor(Color.parseColor("#FFC0CB")); // A light pink color

        // Optional: Add text styles such as size, color, etc.
        labelView.setTextColor(Color.BLACK);
        valueView.setTextColor(Color.BLACK);

        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size

        // Add the TextViews to the TableRow
        row.addView(labelView);
        row.addView(valueView);

        // Add the TableRow to the TableLayout
        tableLayout.addView(row);
    }



    /*
     * Updates the validation output displayed to the user based on the response received from the server.
     * Accepts a string response from the server.
     * Modifies the validation output text view accordingly.
     * Runs on the UI thread to update the UI elements.
     */
    private void updateValidationOutput(String response) {
        runOnUiThread(() -> {
            // Check if the response indicates a network error
            if (response.equals("Network Error: Unable to connect to the server.")) {
                // Display a message indicating network error
                validationOutputTextView.setText("Network error: Unable to reach the email validation service.");
            }
            // Check if the response starts with "Error:"
            else if (response.startsWith("Error:")) {
                // Handle other errors by displaying the error message
                validationOutputTextView.setText(response);
            }
            // If the response is not a network error or an error message
            else {
                try {
                    // Parse the response as a JSON object
                    JSONObject jsonResponse = new JSONObject(response);

                    // Format the validation output based on the JSON response
                    String formattedOutput = formatValidationOutput(jsonResponse);

                    // Display the formatted output in the validation output text view
                    validationOutputTextView.setText(formattedOutput);
                }
                // Catch any parsing errors and display an error message
                catch (Exception e) {
                    Log.e("EmailValidator", "Parsing Error: ", e);
                    validationOutputTextView.setText("Error parsing the response.");
                }
            }
        });
    }

    /*
     * Clears the validation results displayed to the user.
     * Resets the text of the validation output text view and the email input field.
     */
    private void clearValidationResults() {
        // Clear the output text of the validation output text view
        validationOutputTextView.setText("");

        // Clear the text of the email input field
        emailEditText.setText("");

        // Reset the hint text of the email input field
        emailEditText.setHint("Please Enter Email");
    }
}
