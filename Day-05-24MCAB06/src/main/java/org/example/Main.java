package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.bson.json.JsonWriterSettings;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.setResourceBase("src/main/resources/public");
        handler.setWelcomeFiles(new String[]{"index.html"});

        handler.addServlet(new ServletHolder("default", DefaultServlet.class), "/");
        handler.addServlet(new ServletHolder(new AddEmployeeServlet()), "/api/add-employee");
        handler.addServlet(new ServletHolder(new GetEmployeesServlet()), "/api/employees");
        handler.addServlet(new ServletHolder(new DeleteEmployeeServlet()), "/api/delete-employee");
        handler.addServlet(new ServletHolder(new UpdateEmployeeServlet()), "/api/update-employee");
        handler.addServlet(new ServletHolder(new DeleteByEmailServlet()), "/api/delete-by-email");
        handler.addServlet(new ServletHolder(new SearchEmployeesServlet()), "/api/search-employees");
        handler.addServlet(new ServletHolder(new EmployeesPaginatedServlet()), "/api/employees-paginated");
        handler.addServlet(new ServletHolder(new DepartmentStatsServlet()), "/api/department-stats");
        handler.addServlet(new ServletHolder(new DashboardStatsServlet()), "/api/dashboard-stats");



        server.setHandler(handler);
        server.start();
        System.out.println("Server running at http://localhost:8080/");
        server.join();
    }

    // ✅ Add Employee
    public static class AddEmployeeServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                String body = new String(req.getInputStream().readAllBytes());
                Document input = Document.parse(body);
                String email = input.getString("email");

                if (collection.find(new Document("email", email)).first() != null) {
                    resp.getWriter().write(new Document("message", "Email already exists").toJson());
                    return;
                }

                collection.insertOne(input);
                resp.getWriter().write(new Document("message", "Employee added successfully").toJson());
            }
        }
    }

    // ✅ Get All Employees
    public static class GetEmployeesServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                ArrayList<Document> employees = collection.find().into(new ArrayList<>());

                // Convert to JSON array
                String json = employees.stream()
                        .map(doc -> doc.toJson(JsonWriterSettings.builder().build()))
                        .collect(Collectors.joining(",", "[", "]"));

                resp.setContentType("application/json");
                resp.getWriter().write(json);
            }
        }
    }

    // ✅ Delete Employee
    public static class DeleteEmployeeServlet extends HttpServlet {
        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String id = req.getParameter("id");

            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                var result = collection.deleteOne(new Document("_id", new ObjectId(id)));
                String msg = result.getDeletedCount() > 0 ? "Deleted successfully" : "Employee not found";

                resp.setContentType("application/json");
                resp.getWriter().write(new Document("message", msg).toJson());
            }
        }
    }

    // ✅ Update Employee
    public static class UpdateEmployeeServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String body = new String(req.getInputStream().readAllBytes());
            Document updated = Document.parse(body);
            String id = updated.getString("_id");
            updated.remove("_id");

            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                var result = collection.updateOne(
                        new Document("_id", new ObjectId(id)),
                        new Document("$set", updated)
                );

                String msg = result.getModifiedCount() > 0 ? "Updated successfully" : "No changes or employee not found";

                resp.setContentType("application/json");
                resp.getWriter().write(new Document("message", msg).toJson());
            }
        }
    }

    // ✅ Delete by Email Servlet
    public static class DeleteByEmailServlet extends HttpServlet {
        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String email = req.getParameter("email");
            if (email == null || email.isEmpty()) {
                resp.getWriter().write(new Document("message", "Email is required").toJson());
                return;
            }

            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                var result = collection.deleteOne(new Document("email", email));
                String msg = result.getDeletedCount() > 0 ? "Employee deleted" : "Employee not found";

                resp.setContentType("application/json");
                resp.getWriter().write(new Document("message", msg).toJson());
            }
        }
    }



    public static class SearchEmployeesServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                Document query = new Document();

                String name = req.getParameter("name");
                if (name != null && !name.isEmpty()) {
                    query.append("name", new Document("$regex", name).append("$options", "i"));
                }

                String department = req.getParameter("department");
                if (department != null && !department.isEmpty()) {
                    query.append("department", department);
                }

                String skill = req.getParameter("skill");
                if (skill != null && !skill.isEmpty()) {
                    query.append("skills", new Document("$in", java.util.Collections.singletonList(skill)));
                }

                String from = req.getParameter("from");
                String to = req.getParameter("to");
                if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty())) {
                    Document dateRange = new Document();
                    if (from != null && !from.isEmpty()) {
                        dateRange.append("$gte", from);
                    }
                    if (to != null && !to.isEmpty()) {
                        dateRange.append("$lte", to);
                    }
                    query.append("joiningDate", dateRange);
                }

                var employees = collection.find(query).into(new java.util.ArrayList<>());
                String json = employees.stream()
                        .map(doc -> doc.toJson(JsonWriterSettings.builder().build()))
                        .collect(java.util.stream.Collectors.joining(",", "[", "]"));

                resp.setContentType("application/json");
                resp.getWriter().write(json);
            }
        }
    }


    public static class EmployeesPaginatedServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                int page = Integer.parseInt(req.getParameter("page"));
                int size = Integer.parseInt(req.getParameter("size"));
                String sortField = req.getParameter("sort");
                String sortOrder = req.getParameter("order");

                int skip = (page - 1) * size;
                int sortDirection = "desc".equalsIgnoreCase(sortOrder) ? -1 : 1;

                var sortDoc = new Document(sortField, sortDirection);

                var employees = collection.find()
                        .sort(sortDoc)
                        .skip(skip)
                        .limit(size)
                        .into(new java.util.ArrayList<>());

                long totalCount = collection.countDocuments();
                int totalPages = (int) Math.ceil((double) totalCount / size);

                Document responseDoc = new Document("data", employees)
                        .append("totalPages", totalPages)
                        .append("currentPage", page);

                resp.setContentType("application/json");
                resp.getWriter().write(responseDoc.toJson(JsonWriterSettings.builder().build()));
            }
        }
    }

    public static class DepartmentStatsServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                var pipeline = Arrays.asList(
                        new Document("$group", new Document("_id", "$department")
                                .append("count", new Document("$sum", 1)))
                );

                var result = collection.aggregate(pipeline).into(new ArrayList<>());

                // Build JSON manually using MongoDB's toJson()
                String json = result.stream()
                        .map(doc -> {
                            String department = doc.getString("_id");
                            int count = doc.getInteger("count");
                            return String.format("{\"department\":\"%s\",\"count\":%d}", department, count);
                        })
                        .collect(Collectors.joining(",", "[", "]"));

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json);
            }
        }
    }

    public static class DashboardStatsServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase db = client.getDatabase("employeeDB");
                var collection = db.getCollection("employees");

                long totalEmployees = collection.countDocuments();

                long newHires = collection.countDocuments(
                        new Document("joiningDate", new Document("$gte", LocalDate.now().minusDays(30).toString()))
                );

                List<String> departments = collection.distinct("department", String.class).into(new ArrayList<>());

                String json = String.format("""
                {
                    "totalEmployees": %d,
                    "totalDepartments": %d,
                    "newHires": %d
                }
                """, totalEmployees, departments.size(), newHires);

                resp.setContentType("application/json");
                resp.getWriter().write(json);
            }
        }
    }



}
