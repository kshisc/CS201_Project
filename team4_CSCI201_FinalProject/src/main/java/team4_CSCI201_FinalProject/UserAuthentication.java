

package team4_CSCI201_FinalProject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({"/auth/signup", "/auth/login", "/auth/guest"})
public class UserAuthentication extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private int guestNum = 0;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/drawinggame?user=root&password=root");

            if (path.equals("/auth/login")) {
                authenticate(request, response, conn, out);
            } else if (path.equals("/auth/signup")) {
                signUp(request, response, conn, out);
            } else if (path.equals("/auth/guest")) {
                int guestId = guest();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\": \"Guest login successful.\", \"userId\": \"" + guestId + "\", \"username\": \"Guest" + guestId + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
                out.print("{\"message\": \"Invalid path.\"}");
          
            }

        } catch (Exception sqle) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"message\": \"An error occurred.\"}");
            sqle.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    private JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(request.getReader(), JsonObject.class);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, Connection conn, PrintWriter out)
            throws IOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            JsonObject requestBody = parseRequestBody(request);
            String username = requestBody.get("username").getAsString();
            String password = requestBody.get("password").getAsString();

            ps = conn.prepareStatement("SELECT user_id, password FROM Users WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                int userId = rs.getInt("user_id");

                if (password.equals(storedPassword)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"message\": \"Login successful.\", \"userId\": " + userId + ", \"username\": \"" + username + "\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    out.print("{\"message\": \"Invalid password.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
                out.print("{\"message\": \"User not found.\"}");
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            out.print("{\"message\": \"An error occurred during authentication.\"}");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }


    private void signUp(HttpServletRequest request, HttpServletResponse response, Connection conn, PrintWriter out)
            throws IOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            JsonObject requestBody = parseRequestBody(request);
            String username = requestBody.get("username").getAsString();
            String password = requestBody.get("password").getAsString();

            // Check if the username already exists
            ps = conn.prepareStatement("SELECT username FROM Users WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                out.print("{\"message\": \"Username already exists.\"}");
            } else {
                // Insert the new user into the database and get the generated userId
                ps = conn.prepareStatement(
                    "INSERT INTO Users (username, password) VALUES (?, ?)", 
                    PreparedStatement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, username);
                ps.setString(2, password);
                ps.executeUpdate();

                // Fetch the generated userId
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                    out.print("{\"message\": \"User registered successfully.\", \"userId\": " + userId + ", \"username\": \"" + username + "\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
                    out.print("{\"message\": \"Failed to retrieve user ID.\"}");
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            out.print("{\"message\": \"An error occurred during signup.\"}");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }


    private int guest() {
        guestNum++;
        return guestNum;
    }
}
