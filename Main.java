import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

class Student {
    private int roll;
    private String name;
    private int attended;
    private int held;

    public Student(int roll, String name) {
        this(roll, name, 0, 0);
    }

    public Student(int roll, String name, int attended, int held) {
        this.roll = roll;
        this.name = name;
        this.attended = attended;
        this.held = held;
    }

    public int getRoll() {
        return roll;
    }

    public String getName() {
        return name;
    }

    public int getAttended() {
        return attended;
    }

    public int getHeld() {
        return held;
    }

    public double getPercentage() {
        if (held == 0) {
            return 0.0;
        }
        return (attended * 100.0) / held;
    }

    public void markAttendance(char status) {
        held++;
        if (status == 'P') {
            attended++;
        }
    }

    public String toFileString() {
        return roll + "|" + name.replace("|", " ") + "|" + attended + "|" + held;
    }

    // Prints one table row. rank = 0 means "no rank column".
    public void display(int rank) {
        String rollText = (rank > 0) ? rank + ". " + roll : String.valueOf(roll);
        System.out.printf("%-8s | %-20s | %-10s | %.1f%%%n",
                rollText, name, attended + "/" + held, getPercentage());
    }

    public void display() {
        display(0);
    }
}

public class Main {
    private static final String FILE_NAME = "students.txt";
    private static final double MIN_PERCENTAGE = 75.0;

    private static final ArrayList<Student> students = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();

        System.out.println("========================================");
        System.out.println("     STUDENT ATTENDANCE MANAGEMENT");
        System.out.println("========================================");

        while (true) {
            showMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    markAttendance();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    showDefaulters();
                    break;
                case 5:
                    sortByPercentage();
                    break;
                case 6:
                    displayAllStudents();
                    break;
                case 7:
                    saveData();
                    System.out.println("Data saved successfully. Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1 to 7.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n--------------- MENU ---------------");
        System.out.println("1. Add Student");
        System.out.println("2. Mark Attendance");
        System.out.println("3. Search Student");
        System.out.println("4. Show Defaulters (Below 75%)");
        System.out.println("5. Sort by Attendance (High to Low)");
        System.out.println("6. Display All Students");
        System.out.println("7. Save and Exit");
        System.out.println("------------------------------------");
    }

    // Reads an integer; keeps asking until valid. Handles end of input safely.
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);

            if (!sc.hasNextLine()) {
                // No more input (e.g. Ctrl+D or piped input ended): save and quit cleanly.
                System.out.println();
                saveData();
                System.out.println("Input ended. Data saved. Goodbye!");
                System.exit(0);
            }

            String value = sc.nextLine().trim();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Reads a positive roll number (> 0).
    private static int readRoll(String message) {
        while (true) {
            int roll = readInt(message);
            if (roll > 0) {
                return roll;
            }
            System.out.println("Roll number must be greater than 0.");
        }
    }

    private static String readLine(String message) {
        System.out.print(message);
        if (!sc.hasNextLine()) {
            return "";
        }
        return sc.nextLine().trim();
    }

    private static void addStudent() {
        int roll = readRoll("Enter roll number: ");

        if (findStudent(roll) != null) {
            System.out.println("A student with roll number " + roll + " already exists.");
            return;
        }

        String name = readLine("Enter student name: ");

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        Student student = new Student(roll, name);
        students.add(student);
        saveData();

        System.out.println("Student added. " + roll + " | " + name);
    }

    private static void markAttendance() {
        int roll = readRoll("Enter roll number: ");
        Student student = findStudent(roll);

        if (student == null) {
            System.out.println("Roll " + roll + " not found.");
            return;
        }

        String input = readLine("Enter attendance (P for Present / A for Absent): ").toUpperCase();

        if (!input.equals("P") && !input.equals("A")) {
            System.out.println("Invalid attendance. Please enter only P or A.");
            return;
        }

        student.markAttendance(input.charAt(0));
        saveData();

        System.out.printf("Attendance marked for %s. Attendance: %d/%d (%.1f%%)%n",
                student.getName(), student.getAttended(), student.getHeld(),
                student.getPercentage());
    }

    private static void searchStudent() {
        int roll = readRoll("Enter roll number to search: ");
        Student student = findStudent(roll);

        if (student == null) {
            System.out.println("Roll " + roll + " not found.");
            return;
        }

        printHeader();
        student.display();
    }

    private static void showDefaulters() {
        System.out.println("\nStudents with attendance below 75%:");
        printHeader();

        boolean found = false;
        for (Student student : students) {
            if (student.getPercentage() < MIN_PERCENTAGE) {
                student.display();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No defaulters found.");
        }
    }

    private static void sortByPercentage() {
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }

        // Sort a COPY so the saved/original order is not changed.
        // Ties are broken by roll number for a predictable order.
        ArrayList<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparingDouble(Student::getPercentage).reversed()
                .thenComparingInt(Student::getRoll));

        System.out.println("\nStudents sorted by attendance (High to Low):");
        printHeader();

        int rank = 1;
        for (Student student : sorted) {
            student.display(rank++);
        }
    }

    private static void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }

        System.out.println("\nAll Student Records:");
        printHeader();

        for (Student student : students) {
            student.display();
        }
    }

    private static void printHeader() {
        System.out.println("Roll     | Name                 | Attendance | Percentage");
        System.out.println("---------+----------------------+------------+-----------");
    }

    private static Student findStudent(int roll) {
        for (Student student : students) {
            if (student.getRoll() == roll) {
                return student;
            }
        }
        return null;
    }

    private static void loadData() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // ignore blank lines silently
                }

                String[] data = line.split("\\|", -1);

                if (data.length != 4) {
                    System.out.println("Skipping invalid record: " + line);
                    continue;
                }

                try {
                    int roll = Integer.parseInt(data[0].trim());
                    String name = data[1].trim();
                    int attended = Integer.parseInt(data[2].trim());
                    int held = Integer.parseInt(data[3].trim());

                    if (roll > 0 && !name.isEmpty() && attended >= 0 && held >= 0
                            && attended <= held && findStudent(roll) == null) {
                        students.add(new Student(roll, name, attended, held));
                    } else {
                        System.out.println("Skipping invalid record: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid record: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private static void saveData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student student : students) {
                bw.write(student.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
}
