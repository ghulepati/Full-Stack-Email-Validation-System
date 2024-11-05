package sai.ghule.om;

/**
 *  * Author: Sai Ajay Ghule
 *  * Andrew ID: sghule
 *  * Project 4 Task 2
 *
 * Servlet class EmailVerificationServlet is responsible for handling all requests related to email validation
 * and dashboard analysis for email validation data.
 *
 * The servlet listens on two main URL patterns: "/EmailValidator" for validating individual emails and
 * "/dashboardanalysis" for aggregating and displaying validation data on a dashboard.
 *
 * When an HTTP GET request is received, the servlet determines the appropriate action based on the URL pattern:
 * - If the pattern is "/EmailValidator", it proceeds to validate the provided email address.
 * - If the pattern is "/dashboardanalysis", it fetches and prepares data for dashboard display.
 *
 * For email validation:
 * - The email address is extracted from the request.
 * - The EmailVerificationModel class is used to validate the email and construct a response.
 * - The response can be in JSON format for API clients or forwarded to a JSP for web clients.
 *
 * For dashboard analysis:
 * - The DashboardHelper class is utilized to retrieve data from MongoDB.
 * - The retrieved data is added to the request as attributes.
 * - These attributes are used to populate the dashboard which is then displayed using a JSP.
 *
 * Acknowledgement: This project has been completed with the assistance of ChatGPT-4.
 */


/**Author : Sai Ajay Ghule
 * Andrew ID : sghule
 * This Project has been done with the help of Chat Gpt 4 !!!!!
 *
 */
import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * Servlet for handling email validation and dashboard analysis requests.
 *
 * This servlet listens on the URL patterns "/EmailValidator" and "/dashboardanalysis".
 * It routes incoming requests to appropriate methods based on the requested path.
 */
@WebServlet(name = "EmailValidator", urlPatterns = {"/EmailValidator", "/dashboardanalysis"})
public class EmailVerificationServlet extends HttpServlet {


    /**
     * Handles HTTP GET requests.
     *
     * This method extracts the servlet path from the request and routes the request to
     * appropriate methods based on the path.
     *
     * @param request  the HttpServletRequest object containing the request parameters
     * @param response the HttpServletResponse object for sending the response
     * @throws ServletException if there is an error processing the request
     * @throws IOException      if there is an I/O error while processing the request
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the servlet path from the request
        String path = request.getServletPath();
        // Get the client type header from the request
        String clientType = request.getHeader("Client-Type");

        // Route the request based on the servlet path
        switch (path) {
            case "/EmailValidator":
                handleEmailValidation(request, response, clientType);
                break;
            case "/dashboardanalysis":
                handleDashboardAnalysis(request, response);
                break;
            default:
                // If the requested path does not match, send a 404 error response
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource is not available");
                break;
        }
    }

    /**
     * Handles email validation requests.
     *
     * This method validates the provided email address and returns the validation result.
     * If the request is from a mobile app, it returns the result in JSON format.
     * If the request is from a web client, it forwards the result to a JSP page for display.
     *
     * @param request    the HttpServletRequest object containing the request parametersz
     * @param response   the HttpServletResponse object for sending the response
     * @param clientType the client type header indicating the origin of the request (e.g., "MobileApp")
     * @throws IOException if there is an I/O error while processing the request
     */
    private void handleEmailValidation(HttpServletRequest request, HttpServletResponse response, String clientType) throws IOException {
        // Extract the email parameter from the request
        String email = request.getParameter("email");

        if (email != null) {
            // Get the validation result for the provided email
            String result = EmailVerificationModel.getInfo(email);

            if ("MobileApp".equals(clientType)) {
                // If the request is from a mobile app, return the result in JSON format
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(result);
            } else {
                // If the request is from a web client, forward the result to a JSP page for display
                request.setAttribute("result", result);
                RequestDispatcher requestDispatcherView = request.getRequestDispatcher("result.jsp");
                try {
                    requestDispatcherView.forward(request, response);
                } catch (ServletException e) {
                    // Forwarding error handling
                    throw new RuntimeException(e);
                }
            }
        } else {
            if ("MobileApp".equals(clientType)) {
                // If email parameter is missing and request is from a mobile app, send bad request error
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email parameter is missing");
            } else {
                // If email parameter is missing and request is from a web client, forward to index.jsp
                RequestDispatcher requestDispatcherView = request.getRequestDispatcher("index.jsp");
                try {
                    requestDispatcherView.forward(request, response);
                } catch (ServletException e) {
                    // Forwarding error handling
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * Handles dashboard analysis requests.
     *
     * This method retrieves data from MongoDB using the DashboardHelper class.
     * It populates the request attributes with the retrieved data for display on the dashboard.
     *
     * @param request  the HttpServletRequest object containing the request parameters
     * @param response the HttpServletResponse object for sending the response
     * @throws ServletException if there is a servlet exception while processing the request
     * @throws IOException      if there is an I/O error while processing the request
     */
    private void handleDashboardAnalysis(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve data from MongoDB using DashboardHelper
        DashboardHelper.getFromMongo();

        request.setAttribute("databaseLogs", DashboardHelper.getDatabaseLogs());
        ArrayList<String> topDomains = new ArrayList<>();
        ArrayList<String> maxDates = new ArrayList<>();
        String domain;
        while ((domain = DashboardHelper.fetchDomains()) != null) {
            topDomains.add(domain);
        }
        request.setAttribute("topDomains", topDomains);
        String date;
        while ((date = DashboardHelper.fetchDate()) != null) {
            maxDates.add(date);
        }
        request.setAttribute("maxDates", maxDates.isEmpty() ? new ArrayList<>() : maxDates);
        int isValidCount = DashboardHelper.fetchIsValid();
        request.setAttribute("isValidCount", isValidCount);
        int isFreeCount = DashboardHelper.fetchIsFree();
        request.setAttribute("isFreeCount", isFreeCount);
        double validationSuccessRate = DashboardHelper.fetchValidationSuccessRate();
        request.setAttribute("validationSuccessRate", validationSuccessRate);


        RequestDispatcher dispatcher = request.getRequestDispatcher("/dashboardanalysis.jsp");
        dispatcher.forward(request, response);
    }



}
