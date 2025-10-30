import java.sql.*;
import java.util.*;

// ---------- MODEL ----------
class Student {
    private int id;
    private String name;
    private String course;

    public Student(int id, String name, String course) {
        this.id = id;
        this.name = name;
        this.course = course;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCourse() { return course; }
}

// ---------- DAO ----------
class StudentDAO {
    private Connection conn;

    public StudentDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM student";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getString("course")));
            }
        }
        return list;
    }

    public void addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO student (name, course) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCourse());
            ps.executeUpdate();
        }
    }
}

// ---------- VIEW ----------
class StudentView {
    public void displayStudents(List<Student> students) {
        for (Student s : students) {
            System.out.println(s.getId() + " " + s.getName() + " " + s.getCourse());
        }
    }
}

// ---------- CONTROLLER ----------
class StudentController {
    private StudentDAO dao;
    private StudentView view;

    public StudentController(StudentDAO dao, StudentView view) {
        this.dao = dao;
        this.view = view;
    }

    public void showAllStudents() throws Exception {
        List<Student> students = dao.getAllStudents();
        view.displayStudents(students);
    }

    public void addStudent(String name, String course) throws Exception {
        Student s = new Student(0, name, course);
        dao.addStudent(s);
    }
}

// ---------- PRODUCT CRUD ----------
class ProductCRUD {
    private Connection conn;
    private Scanner sc;

    public ProductCRUD(Connection conn, Scanner sc) {
        this.conn = conn;
        this.sc = sc;
    }

    public void run() throws SQLException {
        while (true) {
            System.out.println("\n1. Add Product\n2. View Products\n3. Update Product\n4. Delete Product\n5. Back");
            int choice = sc.nextInt();
            switch (choice) {
                case 1 -> {
                    System.out.print("Enter name and price: ");
                    String name = sc.next();
                    double price = sc.nextDouble();
                    String sql = "INSERT INTO product (name, price) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.executeUpdate();
                        System.out.println("Product added.");
                    }
                }
                case 2 -> {
                    String sql = "SELECT * FROM product";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            System.out.println(rs.getInt("id") + " " +
                                               rs.getString("name") + " " +
                                               rs.getDouble("price"));
                        }
                    }
                }
                case 3 -> {
                    System.out.print("Enter product ID to update: ");
                    int id = sc.nextInt();
                    System.out.print("Enter new name and price: ");
                    String name = sc.next();
                    double price = sc.nextDouble();
                    String sql = "UPDATE product SET name=?, price=? WHERE id=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.setInt(3, id);
                        ps.executeUpdate();
                        System.out.println("Product updated.");
                    }
                }
                case 4 -> {
                    System.out.print("Enter product ID to delete: ");
                    int id = sc.nextInt();
                    String sql = "DELETE FROM product WHERE id=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                        System.out.println("Product deleted.");
                    }
                }
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}

// ---------- MAIN ----------
public class MainApp {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/demo_db";
        String user = "root";
        String password = "your_password";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Scanner sc = new Scanner(System.in)) {

            StudentDAO studentDAO = new StudentDAO(conn);
            StudentView studentView = new StudentView();
            StudentController studentController = new StudentController(studentDAO, studentView);
            ProductCRUD productCRUD = new ProductCRUD(conn, sc);

            while (true) {
                System.out.println("\n--- Main Menu ---");
                System.out.println("1. Fetch Employees");
                System.out.println("2. Product CRUD");
                System.out.println("3. Student Management");
                System.out.println("4. Exit");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> {
                        String sql = "SELECT * FROM employees";
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery(sql)) {
                            while (rs.next()) {
                                System.out.println(rs.getInt("id") + " " +
                                                   rs.getString("name") + " " +
                                                   rs.getString("department"));
                            }
                        }
                    }
                    case 2 -> productCRUD.run();
                    case 3 -> {
                        while (true) {
                            System.out.println("\n1. Add Student\n2. View Students\n3. Back");
                            int subChoice = sc.nextInt();
                            switch (subChoice) {
                                case 1 -> {
                                    System.out.print("Enter name and course: ");
                                    String name = sc.next();
                                    String course = sc.next();
                                    studentController.addStudent(name, course);
                                }
                                case 2 -> studentController.showAllStudents();
                                case 3 -> { break; }
                                default -> System.out.println("Invalid choice.");
                            }
                            if (subChoice == 3) break;
                        }
                    }
                    case 4 -> System.exit(0);
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (SQLException | Exception e) {
            e.printStackTrace();
        }
    }
}
