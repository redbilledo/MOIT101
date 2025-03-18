import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MotorPHPayrollMS2 {
    static List<Employee> employees = new ArrayList<>();
    static List<Attendance> attendanceRecords = new ArrayList<>();

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        while (true) {
            System.out.println("------- LOGIN PAGE -------");
            System.out.println("1. Login as Employee");
            System.out.println("2. Login as Admin");
            System.out.println("3. Exit Program");
            System.out.print("Choose an option > ");
    
            int choice = s.nextInt();
            s.nextLine(); // Consume newline
    
            switch (choice) {
                case 1:
                    System.out.print("Enter Employee Number: ");
                    int empNumber = s.nextInt();
                    s.nextLine(); // Consume newline
    
                    System.out.print("Enter Password (arbitrary): ");
                    String password = s.nextLine();
    
                    if (password.equals("arbitrary")) {
                        System.out.println("Login successful! Redirecting to Employee Dashboard...");
                        employeeDashboard(s, empNumber);
                    } else {
                        System.out.println("Invalid credentials. Please try again.");
                    }
                    break;
    
                case 2:
                    System.out.print("Enter Admin Username: ");
                    s.nextLine(); // Consume newline
    
                    System.out.print("Enter Admin Password: ");
                    String adminPass = s.nextLine();
    
                    if (adminPass.equals("admin")) { 
                        System.out.println("Admin login successful! Redirecting to Admin Dashboard...");
                        adminDashboard(s);
                    } else {
                        System.out.println("Invalid admin credentials. Please try again.");
                    }
                    break;
    
                case 3:
                    System.out.println("Exiting program...");
                    System.exit(0);
                    break;
    
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Function to properly split CSV lines while respecting quoted values
    public static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == ',' && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0); // Clear the buffer
            } else if (c == '"') {
                inQuotes = !inQuotes; // Toggle quote mode
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString().trim()); // Add last token

        return tokens.toArray(new String[0]);
    }
    
    public static void adminDashboard(Scanner s) {
        int option = -1;
        while (option != 4) {
            System.out.println("\n----------ADMIN MENU----------");
            System.out.println("1. Load employee data");
            System.out.println("2. Display employee data");
            System.out.println("3. Calculate hours worked");
            System.out.println("4. Calculate monthly salary");
            System.out.println("5. Calculate final monthly salary with deductions");
            System.out.println("6. Exit menu");
            System.out.print("Choose an option > ");
            
            try {
                option = s.nextInt();
                s.nextLine(); // Consume newline
                switch (option) {
                    case 1:
                    loadEmployeeCSVs();
                break;
                    case 2:
                         System.out.println("Displaying all employee data...");
                        displayEmployeeData();
                        break;
                    case 3:
                        System.out.print("Enter Employee ID: ");
                        int empId = s.nextInt();
                        s.nextLine(); // Consume newline
                    
                        double hoursWorked = calculateTotalHoursWorked(empId);
                        if (hoursWorked > 0) {
                            System.out.printf("Total hours worked: %.2f hours%n", hoursWorked);
                        }
                        break;
                    case 4:
                        System.out.print("Enter Employee ID: ");
                        int employeeId = s.nextInt();
                        s.nextLine(); // Consume newline
                    
                        System.out.print("Enter Month and Year (M/YYYY): ");
                        String monthYearInput = s.nextLine();
                    
                        double salary = calculateMonthlySalary(employeeId, monthYearInput);
                    
                        if (salary > 0) {
                            System.out.printf("Employee %d's gross salary for %s: %.2f%n", employeeId, monthYearInput, salary);
                        } else {
                            System.out.println("No records found or invalid input.");
                        }
                        break;
                    case 5:
                         System.out.print("Enter Employee ID: ");
                        int empIdForSalary = s.nextInt();
                        s.nextLine(); // Consume newline
                        System.out.print("Enter Month and Year (M/YYYY): ");
                        String monthYear = s.nextLine();
                
                        calculateFinalMonthlySalary(empIdForSalary, monthYear);
                        saveProcessedSalaries();
                        System.out.println("Final salaries saved to data/Processed_Salaries.csv.");
                        break;
                    case 6:
                        System.out.println("Exiting admin menu...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            }
        }
    }

    public static void displayEmployeeData() {
        System.out.println("\n==================== EMPLOYEE DETAILS ====================");
        System.out.printf("%-10s %-15s %-15s %-12s %-30s %-12s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-8s %-10s %-10s %-10s %-15s %-10s\n",
                "Emp #", "Last Name", "First Name", "Birthday", "Address", "Phone", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
                "Status", "Position", "Supervisor", "Salary", "Rice", "Phone", "Clothing", "Semi-Monthly", "Hourly");
        
        for (Employee emp : employees) {
            System.out.printf("%-10d %-15s %-15s %-12s %-30s %-12s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-8s %-10.2f %-10.2f %-10.2f %-15.2f %-10.2f\n",
                    emp.employeeNumber, emp.lastName, emp.firstName, emp.birthday, emp.address, emp.phone, emp.sss, emp.philhealth, emp.tin, emp.pagibig,
                    emp.status, emp.position, emp.supervisor != null ? emp.supervisor : "N/A", emp.basicSalary, emp.riceSubsidy, emp.phoneAllowance, emp.clothingAllowance, emp.semiMonthlyRate, emp.hourlyRate);
        }
    
        System.out.println("\n==================== ATTENDANCE RECORDS ====================");
        System.out.printf("%-10s %-15s %-15s %-12s %-8s %-8s\n",
                "Emp #", "Last Name", "First Name", "Date", "Log In", "Log Out");
    
        for (Attendance record : attendanceRecords) {
            System.out.printf("%-10d %-15s %-15s %-12s %-8s %-8s\n",
                    record.employeeNumber, record.lastName, record.firstName, record.date, record.logIn, record.logOut);
        }
    }
    

    public static void employeeDashboard(Scanner s, int empNumber) {
        int option = -1;
        while (option != 4) {
            System.out.println("\n----------EMPLOYEE MENU----------");
            System.out.println("1. Load employee data");
            System.out.println("2. View my details");
            System.out.println("3. See monthly salary and deductions");
            System.out.println("4. Exit menu");
            System.out.print("Choose an option > ");
    
            try {
                String input = s.nextLine().trim(); // Read input as a string
                option = Integer.parseInt(input); // Convert to integer
    
                switch (option) {
                    case 1:
                        loadEmployeeCSVs();
                        break;
                    case 2:
                        System.out.println("Displaying your employee details and attendance...");
                        displayEmployeeDataForUser(empNumber);
                        break;
                    case 3:
                        System.out.println("Retrieving your salary details...");
                        displayProcessedSalaryForUser(empNumber);
                        break;
                    case 4:
                        System.out.println("Exiting employee menu...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    

    public static void loadEmployeeCSVs() {
        employees.clear();
        attendanceRecords.clear();

        try {
            // Load Employee Details CSV
            List<String> employeeLines = Files.readAllLines(Paths.get("data\\MotorPH_Employee_Details.csv"));
            employeeLines.remove(0); // Remove header

            for (String line : employeeLines) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 19) { // Ensure correct format
                    Employee emp = new Employee(data);
                    if (!employees.contains(emp)) {
                        employees.add(emp);
                    }
                }
            }

        // Load Attendance Records CSV
        List<String> attendanceLines = Files.readAllLines(Paths.get("data\\MotorPH_Attendance_Record.csv"));
        attendanceLines.remove(0); // Remove header

        for (String line : attendanceLines) {
            String[] data = line.split(",");
            if (data.length >= 6) { // Ensure correct format
                Attendance att = new Attendance(data);
                if (!attendanceRecords.contains(att)) {
                    attendanceRecords.add(att);
                }
            }
        }

        System.out.println("Employee and attendance records successfully loaded.");
    } catch (IOException e) {
        System.out.println("Error loading CSV files: " + e.getMessage());
        }
    }

    public static double calculateTotalHoursWorked(int employeeId) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm"); // Supports "8:59" and "08:59"
        double totalHoursWorked = 0;
    
        for (Attendance record : attendanceRecords) {
            if (record.employeeNumber == employeeId) {
                try {
                    if (record.logIn == null || record.logOut == null ||
                        record.logIn.trim().isEmpty() || record.logOut.trim().isEmpty()) {
                        System.out.println("Skipping missing log time for Employee ID: " + employeeId + " on " + record.date);
                        continue;
                    }
    
                    // Ensure times are trimmed
                    String logInStr = record.logIn.trim();
                    String logOutStr = record.logOut.trim();
    
                    // Parse times correctly
                    LocalTime logIn = LocalTime.parse(logInStr, timeFormatter);
                    LocalTime logOut = LocalTime.parse(logOutStr, timeFormatter);
    
                    // Calculate duration correctly
                    double hoursWorked = Duration.between(logIn, logOut).toMinutes() / 60.0;

                    // Ensure that only valid (non-negative) durations are counted
                    if (hoursWorked > 0) {
                        totalHoursWorked += hoursWorked;
                    } else {
                        System.out.println("Skipping invalid time duration for Employee ID: " + employeeId + " on " + record.date);
                    }
    
                } catch (Exception e) {
                    System.out.println("Error parsing time for Employee ID: " + employeeId + " on " + record.date +
                            ". Log In: " + record.logIn + ", Log Out: " + record.logOut);
                }
            }
        }
    
        return totalHoursWorked;
    }

    public static double calculateMonthlySalary(int employeeId, String monthYear) {
        double totalHoursWorked = 0;
        double overtimeHours = 0;
        double hourlyRate = 0;
    
        // Retrieve hourly rate from employee records
        boolean employeeFound = false;
        for (Employee emp : employees) {
            if (emp.getEmployeeNumber() == employeeId) { 
                hourlyRate = emp.getHourlyRate();
                employeeFound = true;
                break;
            }
        }
    
        if (!employeeFound) {
            System.out.println("Error: Employee ID not found.");
            return 0;
        }
    
        // Format input monthYear to match attendance records (M/YYYY)
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("M/yyyy");
        YearMonth inputMonthYear;
        try {
            inputMonthYear = YearMonth.parse(monthYear, monthYearFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid month/year format. Please use M/YYYY.");
            return 0;
        }
    
        for (Attendance record : attendanceRecords) {
            YearMonth recordMonthYear = YearMonth.from(record.getDate());
            if (record.getEmployeeNumber() == employeeId && recordMonthYear.equals(inputMonthYear)) {
    
                LocalDate date = record.getDate();
                DayOfWeek day = date.getDayOfWeek();
    
                // Skip Sundays
                if (day == DayOfWeek.SUNDAY) continue;
    
                // Office hours
                LocalTime startOfWork = LocalTime.of(8, 30);
                LocalTime endOfWork = LocalTime.of(17, 30);
    
                try {
                    LocalTime logIn = LocalTime.parse(record.getLogIn(), DateTimeFormatter.ofPattern("H:mm"));
                    LocalTime logOut = LocalTime.parse(record.getLogOut(), DateTimeFormatter.ofPattern("H:mm"));
    
                    // Adjust logIn if early
                    if (logIn.isBefore(startOfWork)) logIn = startOfWork;
    
                    // Calculate total hours worked
                    double dailyHours = Duration.between(logIn, logOut).toMinutes() / 60.0;
                    totalHoursWorked += Math.max(dailyHours, 0);
    
                    // Calculate overtime (only if arrived on time)
                    if (!logIn.isAfter(startOfWork) && logOut.isAfter(endOfWork)) {
                        double overtime = Duration.between(endOfWork, logOut).toMinutes() / 60.0;
                        overtimeHours += Math.max(overtime, 0);
                    }
    
                } catch (DateTimeParseException e) {
                    System.out.println("Error parsing time for Employee ID: " + employeeId + " on " + date);
                }
            }
        }
    
        // Compute final salary
        double regularPay = totalHoursWorked * hourlyRate;
        double overtimePay = overtimeHours * (hourlyRate * 1.25);
        double finalSalary = regularPay + overtimePay;
    
        // Ensure final salary is non-negative
        if (finalSalary < 0) {
            finalSalary = 0;
        }
    
        System.out.printf("Regular Hours: %.2f | Overtime Hours: %.2f | Hourly Rate: %.2f | Final Salary: %.2f%n", 
                          totalHoursWorked, overtimeHours, hourlyRate, finalSalary);
    
        return finalSalary;
    }    
    

    public static double calculatePhilhealth(double finalSalary) {
        double premiumRate = 0.03; // 3%
        double philhealthPremium;
    
        if (finalSalary <= 10000) {
            philhealthPremium = 300;
        } else if (finalSalary <= 59999.99) {
            philhealthPremium = Math.min(Math.max(finalSalary* premiumRate, 300), 1800);
        } else {
            philhealthPremium = 1800;
        }
    
        return philhealthPremium / 2; // Employee Share (50%)
    }

    public static double calculatePagIbig(double finalSalary) {
        double pagIbigRate = (finalSalary >= 1000 && finalSalary <= 1500) ? 0.01 : 0.02;
        double contribution = finalSalary * pagIbigRate;
    
        return Math.min(contribution, 100); // Apply max contribution cap
    }

    public static double calculateSSS(double finalSalary) {
        double[][] sssBrackets = {
            {0, 3249.99, 135.00}, {3250, 3749.99, 157.50}, {3750, 4249.99, 180.00},
            {4250, 4749.99, 202.50}, {4750, 5249.99, 225.00}, {5250, 5749.99, 247.50},
            {5750, 6249.99, 270.00}, {6250, 6749.99, 292.50}, {6750, 7249.99, 315.00},
            {7250, 7749.99, 337.50}, {7750, 8249.99, 360.00}, {8250, 8749.99, 382.50},
            {8750, 9249.99, 405.00}, {9250, 9749.99, 427.50}, {9750, 10249.99, 450.00},
            {10250, 10749.99, 472.50}, {10750, 11249.99, 495.00}, {11250, 11749.99, 517.50},
            {11750, 12249.99, 540.00}, {12250, 12749.99, 562.50}, {12750, 13249.99, 585.00},
            {13250, 13749.99, 607.50}, {13750, 14249.99, 630.00}, {14250, 14749.99, 652.50},
            {14750, 15249.99, 675.00}, {15250, 15749.99, 697.50}, {15750, 16249.99, 720.00},
            {16250, 16749.99, 742.50}, {16750, 17249.99, 765.00}, {17250, 17749.99, 787.50},
            {17750, 18249.99, 810.00}, {18250, 18749.99, 832.50}, {18750, 19249.99, 855.00},
            {19250, 19749.99, 877.50}, {19750, 20249.99, 900.00}, {20250, 20749.99, 922.50},
            {20750, 21249.99, 945.00}, {21250, 21749.99, 967.50}, {21750, 22249.99, 990.00},
            {22250, 22749.99, 1012.50}, {22750, 23249.99, 1035.00}, {23250, 23749.99, 1057.50},
            {23750, 24249.99, 1080.00}, {24250, 24749.99, 1102.50}, {24750, Double.MAX_VALUE, 1125.00}
        };
    
        for (double[] bracket : sssBrackets) {
            if (finalSalary >= bracket[0] && finalSalary <= bracket[1]) {
                return bracket[2];
            }
        }
        return 0; // fallback for invalid entry
    }

    public static double calculateWithholdingTax(double taxableIncome) {
        double tax = 0;
    
        if (taxableIncome <= 20832) {
            tax = 0; // No withholding tax
        } else if (taxableIncome < 33333) {
            tax = (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome < 66667) {
            tax = 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome < 166667) {
            tax = 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome < 666667) {
            tax = 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            tax = 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    
        return tax;
    }

    public static void calculateFinalMonthlySalary(int employeeId, String monthYear) {
        // Retrieve monthly salary
        double monthlySalary = calculateMonthlySalary(employeeId, monthYear);
    
        // Deduction calculations
        double sssDeduction = calculateSSS(monthlySalary);
        double philhealthDeduction = calculatePhilhealth(monthlySalary);
        double pagIbigDeduction = calculatePagIbig(monthlySalary);
        double totalDeductions = sssDeduction + philhealthDeduction + pagIbigDeduction;
    
        // Taxable income
        double taxableIncome = monthlySalary - totalDeductions;
        double withholdingTax = calculateWithholdingTax(taxableIncome);
    
        // Final salary after tax
        double finalSalary = taxableIncome - withholdingTax;
    
        // Display breakdown
        System.out.println("------- FINAL SALARY COMPUTATION -------");
        System.out.printf("Monthly Salary: %.2f%n", monthlySalary);
        System.out.println("------- Deductions -------");
        System.out.printf("SSS Deduction: %.2f%n", sssDeduction);
        System.out.printf("PhilHealth Deduction: %.2f%n", philhealthDeduction);
        System.out.printf("Pag-IBIG Deduction: %.2f%n", pagIbigDeduction);
        System.out.printf("TOTAL DEDUCTIONS: %.2f%n", totalDeductions);
        System.out.println("------- Tax Computation -------");
        System.out.printf("TAXABLE INCOME (Salary - Total Deductions): %.2f%n", taxableIncome);
        System.out.printf("WITHHOLDING TAX: %.2f%n", withholdingTax);
        System.out.println("------- Final Salary -------");
        System.out.printf("NET PAY (After Deductions & Tax): %.2f%n", finalSalary);
        System.out.println("--------------------------------------");
    }    

    public static void saveProcessedSalaries() {
        String filePath = "data/Processed_Salaries.csv";
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // CSV Header
            bw.write("Employee #,Last Name,First Name,Monthly Salary,SSS Deduction,Philhealth Deduction,Pag-IBIG Deduction,Total Deductions,Taxable Income,Withholding Tax,Final Salary");
            bw.newLine();
    
            for (Employee emp : employees) {
                // Get Monthly Salary
                double monthlySalary = calculateMonthlySalary(emp.employeeNumber, YearMonth.now().format(DateTimeFormatter.ofPattern("M/yyyy")));
    
                // Calculate Deductions
                double sss = calculateSSS(monthlySalary);
                double philhealth = calculatePhilhealth(monthlySalary);
                double pagibig = calculatePagIbig(monthlySalary);
                double totalDeductions = sss + philhealth + pagibig;
    
                // Calculate Tax
                double taxableIncome = monthlySalary - totalDeductions;
                double withholdingTax = calculateWithholdingTax(taxableIncome);
                double finalSalary = taxableIncome - withholdingTax;
    
                // Write to CSV
                bw.write(String.format("%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                        emp.employeeNumber, emp.lastName, emp.firstName, monthlySalary, sss, philhealth, pagibig,
                        totalDeductions, taxableIncome, withholdingTax, finalSalary));
                bw.newLine();
            }
    
            System.out.println("Processed salary data saved successfully.");
    
        } catch (IOException e) {
            System.out.println("Error saving processed salaries: " + e.getMessage());
        }
    }
    
    public static void displayEmployeeDataForUser(int employeeNumber) {
        // Find the employee's details
        Employee emp = null;
        for (Employee e : employees) {
            if (e.employeeNumber == employeeNumber) {
                emp = e;
                break;
            }
        }
    
        if (emp == null) {
            System.out.println("Error: Employee data not found.");
            return;
        }
    
        // Display Employee Details
        System.out.println("\n==================== YOUR EMPLOYEE DETAILS ====================");
        System.out.printf("Employee #: %d\nName: %s %s\nBirthday: %s\nAddress: %s\nPhone: %s\nStatus: %s\nPosition: %s\nSupervisor: %s\n",
                emp.employeeNumber, emp.firstName, emp.lastName, emp.birthday, emp.address, emp.phone, emp.status, emp.position, emp.supervisor);
    
        System.out.printf("Basic Salary: %.2f\nRice Subsidy: %.2f\nPhone Allowance: %.2f\nClothing Allowance: %.2f\nSemi-Monthly Rate: %.2f\nHourly Rate: %.2f\n",
                emp.basicSalary, emp.riceSubsidy, emp.phoneAllowance, emp.clothingAllowance, emp.semiMonthlyRate, emp.hourlyRate);
    
        // Display Attendance Records
        System.out.println("\n==================== YOUR ATTENDANCE RECORDS ====================");
        System.out.printf("%-12s %-8s %-8s\n", "Attendance Date", "Log In", "Log Out");
    
        boolean foundRecords = false;
        for (Attendance record : attendanceRecords) {
            if (record.employeeNumber == employeeNumber) {
                System.out.printf("%-12s %-8s %-8s\n", record.date, record.logIn, record.logOut);
                foundRecords = true;
            }
        }
    
        if (!foundRecords) {
            System.out.println("No attendance records found.");
        }
    }
    
    public static void displayProcessedSalaryForUser(int employeeNumber) {
        String filePath = "data/Processed_Salaries.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header row
    
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
    
                int empNum = Integer.parseInt(data[0].trim()); // Employee #
                if (empNum == employeeNumber) {
                    // Found the employee, display details
                    System.out.println("\n==================== YOUR PROCESSED SALARY ====================");
                    System.out.printf("Employee #: %d\nName: %s %s\n", empNum, data[1], data[2]);
                    System.out.printf("Monthly Salary: %.2f\nSSS Deduction: %.2f\nPhilhealth Deduction: %.2f\nPag-IBIG Deduction: %.2f\nTotal Deductions: %.2f\n",
                            Double.parseDouble(data[3]), Double.parseDouble(data[4]), Double.parseDouble(data[5]),
                            Double.parseDouble(data[6]), Double.parseDouble(data[7]));
                    System.out.printf("Taxable Income: %.2f\nWithholding Tax: %.2f\nFinal Salary: %.2f\n",
                            Double.parseDouble(data[8]), Double.parseDouble(data[9]), Double.parseDouble(data[10]));
                    
                    return; // Exit after finding the employee
                }
            }
    
            System.out.println("No processed salary record found for your Employee ID.");
    
        } catch (IOException e) {
            System.out.println("Error loading processed salary file: " + e.getMessage());
        }
    }

public static class Employee {
    private int employeeNumber;
    private String lastName, firstName, birthday, address, phone, supervisor;
    private String sss, philhealth, tin, pagibig;
    private String status, position, immediateSupervisor;
    private double basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, semiMonthlyRate, hourlyRate;

    // Main Constructor
    public Employee(int employeeNumber, String lastName, String firstName, String birthday, String address, 
                    String phoneNumber, String sssNumber, String philhealthNumber, String tinNumber, String pagibigNumber,
                    String status, String position, String immediateSupervisor, double basicSalary, double riceSubsidy, 
                    double phoneAllowance, double clothingAllowance, double semiMonthlyRate, double hourlyRate) {
        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phone = phoneNumber;
        this.sss = sssNumber;
        this.philhealth = philhealthNumber;
        this.tin = tinNumber;
        this.pagibig = pagibigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary; // Already parsed, don't parse again
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.semiMonthlyRate = semiMonthlyRate;
        this.hourlyRate = hourlyRate; // Already parsed, don't parse again
    }

    // Constructor to create an Employee from a CSV line
    public Employee(String[] data) {
        this(
            Integer.parseInt(data[0].trim()),   // Employee Number
            data[1].trim(),  // Last Name
            data[2].trim(),  // First Name
            data[3].trim(),  // Birthday
            data[4].trim(),  // Address
            data[5].trim(),  // Phone Number
            data[6].trim(),  // SSS Number
            data[7].trim(),  // PhilHealth Number
            data[8].trim(),  // TIN Number
            data[9].trim(),  // Pag-IBIG Number
            data[10].trim(), // Status
            data[11].trim(), // Position
            data[12].trim(), // Immediate Supervisor
            parseMoney(data[13]), // Basic Salary
            parseMoney(data[14]), // Rice Subsidy
            parseMoney(data[15]), // Phone Allowance
            parseMoney(data[16]), // Clothing Allowance
            parseMoney(data[17]), // Semi-Monthly Rate
            parseMoney(data[18])  // Hourly Rate
        );
    }

    // Method to safely parse money values
    private static double parseMoney(String value) {
        try {
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            System.out.println("Error parsing salary value: " + value);
            return 0.0; // Default to 0.0 if value is not a valid number
        }
    }

    // Getters
    public int getEmployeeNumber() { return employeeNumber; }
    public String getFullName() { return firstName + " " + lastName; }
    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getSemiMonthlyRate() { return semiMonthlyRate; }
    public double getHourlyRate() { return hourlyRate; }
}

    public static class Attendance {
        private int employeeNumber;
        private String lastName, firstName;
        private LocalDate date;
        private String logIn;
        private String logOut;
    
        // Constructor
        public Attendance(int employeeNumber, String lastName, String firstName, LocalDate date, String logIn, String logOut) {
            this.employeeNumber = employeeNumber;
            this.lastName = lastName;
            this.firstName = firstName;
            this.date = date;
            this.logIn = logIn;
            this.logOut = logOut;
        }
    
        // Constructor to create an Attendance object from a CSV row
        public Attendance(String[] data) {
            this(
                Integer.parseInt(data[0].trim()),  // Employee #
                data[1].trim(),  // Last Name
                data[2].trim(),  // First Name
                LocalDate.parse(data[3].trim(), DateTimeFormatter.ofPattern("M/d/yyyy")),  // Date
                data[4].trim(),  // Log In Time
                data[5].trim()   // Log Out Time
            );
        }
    
        // Getters
        public int getEmployeeNumber() { return employeeNumber; }
        public String getFullName() { return firstName + " " + lastName; }
        public LocalDate getDate() { return date; }
        public String getLogIn() { return logIn; }
        public String getLogOut() { return logOut; }
    }
}
