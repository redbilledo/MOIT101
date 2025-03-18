import java.io.*;
import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;

public class MotorPHPayrollMS2 {

    static List<Employee> employees = new ArrayList<>();
    static List<Attendance> attendanceRecords = new ArrayList<>();

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
    
        // Load employee data and attendance data at startup
        if (!loadEmployeeCSVs()) {
            System.out.println("Error loading employee data. Exiting program.");
            System.exit(1);
        }
    
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
    
                    if (isValidEmployee(empNumber) && password.equals("arbitrary")) {
                        System.out.println("Login successful! Redirecting to Employee Dashboard...");
                        employeeDashboard(s);
                    } else {
                        System.out.println("Invalid credentials. Please try again.");
                    }
                    break;
    
                case 2:
                    System.out.print("Enter Admin Username: ");
                    String adminUser = s.nextLine();
    
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
    
    public static void loadEmployeeDetails(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine(); // Skip header
    
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            int empNumber = Integer.parseInt(data[0].trim()); // Employee #
            String lastName = data[1].trim();
            String firstName = data[2].trim();
            String birthday = data[3].trim();
            String address = data[4].trim();
            String phone = data[5].trim();
            String sss = data[6].trim();
            String philhealth = data[7].trim();
            String tin = data[8].trim();
            String pagibig = data[9].trim();
            String status = data[10].trim();
            String position = data[11].trim();
            String supervisor = data[12].trim();
            int basicSalary = Integer.parseInt(data[13].trim());
            int riceSubsidy = Integer.parseInt(data[14].trim());
            int phoneAllowance = Integer.parseInt(data[15].trim());
            int clothingAllowance = Integer.parseInt(data[16].trim());
            int semiMonthlyRate = Integer.parseInt(data[17].trim());
            int hourlyRate = Integer.parseInt(data[18].trim());
    
            Employee emp = new Employee(empNumber, lastName, firstName, birthday, address, phone, sss, philhealth, tin, pagibig, status, position, supervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, semiMonthlyRate, hourlyRate);
            
            if (!employees.contains(emp)) {
                employees.add(emp);
            }
        }
    
        br.close();
    }
    
    public static void loadAttendanceRecords(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine(); // Skip header
    
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            int empNumber = Integer.parseInt(data[0].trim()); // Employee #
            String lastName = data[1].trim();
            String firstName = data[2].trim();
            String date = data[3].trim();
            String logIn = data[4].trim();
            String logOut = data[5].trim();
    
            Attendance record = new Attendance(data);
    
            if (!attendanceRecords.contains(record)) {
                attendanceRecords.add(record);
            }
        }
    
        br.close();
    }

    public static boolean isValidEmployee(int employeeNumber) {
        for (Employee emp : employees) {
            if (emp.employeeNumber == employeeNumber) {
                return true; // Employee exists
            }
        }
        return false; // Employee not found
    }    
    
    public static void adminDashboard(Scanner s) {
        int option = -1;
        while (option != 4) {
            System.out.println("\n----------ADMIN MENU----------");
            System.out.println("1. Display employee data");
            System.out.println("2. Calculate hours worked");
            System.out.println("3. Calculate monthly salary");
            System.out.println("4. Calculate final monthly salary with deductions");
            System.out.println("5. Exit menu");
            System.out.print("Choose an option > ");
            
            try {
                option = s.nextInt();
                s.nextLine(); // Consume newline
                switch (option) {
                    case 1:
                         System.out.println("Displaying all employee data...");
                        displayEmployeeData();
                        break;
                    case 2:
                        System.out.print("Enter Employee ID: ");
                        int empId = s.nextInt();
                        s.nextLine(); // Consume newline
                        System.out.print("Enter Date (YYYY-MM-DD): ");
                        String date = s.nextLine();
                    
                        double hoursWorked = calculateHoursWorked(empId, date);
                        if (hoursWorked > 0) {
                            System.out.printf("Employee %d worked %.2f hours on %s.%n", empId, hoursWorked, date);
                        }
                        break;
                    case 3:
                        System.out.print("Enter Employee ID: ");
                        int employeeId = s.nextInt();
                        s.nextLine(); // Consume newline
                        System.out.print("Enter Date (YYYY-MM-DD): ");
                        String dateInput = s.nextLine();
                        double salary = calculateMonthlySalary(employeeId, dateInput);
                        if (salary > 0) {
                            System.out.printf("Employee %d's gross salary for %s: %.2f%n", employeeId, dateInput, salary);
                        }
                        break;
                    case 4:
                         System.out.print("Enter Employee ID: ");
                        int empIdForSalary = s.nextInt();
                        s.nextLine(); // Consume newline
                        System.out.print("Enter Month & Year (M/YYYY, e.g., 6/2024): ");
                        String monthYear = s.nextLine();
                
                        calculateFinalMonthlySalary(empIdForSalary, monthYear);
                        saveProcessedSalaries();
                        System.out.println("Final salaries saved to data/Processed_Salaries.csv.");
                        break;
                    case 5:
                        System.out.println("Exiting admin menu...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            }
        }
    }

    public static boolean loadEmployeeCSVs() {
        try {
            loadEmployeeDetails("data/MotorPH_Employee_Details.csv");
            loadAttendanceRecords("data/MotorPH_Attendance_Record.csv");
            System.out.println("Employee data successfully loaded.");
            return true;
        } catch (Exception e) {
            System.out.println("Error loading CSV files: " + e.getMessage());
            return false;
        }
    }

    private static void loadCSV(String filePath, boolean isEmployeeFile) {
        try (InputStream inputStream = EmployeeFunction.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (isEmployeeFile) {
                    Employee emp = new Employee(data);
                    if (!employees.contains(emp)) {
                        employees.add(emp);
                    }
                } else {
                    Attendance att = new Attendance(data);
                    if (!attendanceRecords.contains(att)) {
                        attendanceRecords.add(att);
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Error loading CSV file: " + filePath + " - " + e.getMessage());
        }
    }

    public static void displayEmployeeData() {
        System.out.println("\n==================== EMPLOYEE DETAILS ====================");
        System.out.printf("%-10s %-15s %-15s %-12s %-30s %-12s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-8s %-10s %-10s %-10s %-15s %-10s\n",
                "Emp #", "Last Name", "First Name", "Birthday", "Address", "Phone", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
                "Status", "Position", "Supervisor", "Salary", "Rice", "Phone", "Clothing", "Semi-Monthly", "Hourly");
        
        for (Employee emp : employees) {
            System.out.printf("%-10d %-15s %-15s %-12s %-30s %-12s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-8d %-10d %-10d %-10d %-15d %-10d\n",
                    emp.employeeNumber, emp.lastName, emp.firstName, emp.birthday, emp.address, emp.phone, emp.sss, emp.philhealth, emp.tin, emp.pagibig,
                    emp.status, emp.position, emp.supervisor, emp.basicSalary, emp.riceSubsidy, emp.phoneAllowance, emp.clothingAllowance, emp.semiMonthlyRate, emp.hourlyRate);
        }
    
        System.out.println("\n==================== ATTENDANCE RECORDS ====================");
        System.out.printf("%-10s %-15s %-15s %-12s %-8s %-8s\n",
                "Emp #", "Last Name", "First Name", "Date", "Log In", "Log Out");
    
        for (Attendance record : attendanceRecords) {
            System.out.printf("%-10d %-15s %-15s %-12s %-8s %-8s\n",
                    record.employeeNumber, record.lastName, record.firstName, record.date, record.logIn, record.logOut);
        }
    }
    

    public static void employeeDashboard(Scanner s) {
        int option = -1;
        while (option != 3) {
            System.out.println("\n----------EMPLOYEE MENU----------");
            System.out.println("1. View my details");
            System.out.println("2. See monthly salary and deductions");
            System.out.println("3. Exit menu");
            System.out.print("Choose an option > ");
            
            try {
                option = s.nextInt();
                s.nextLine(); // Consume newline
                switch (option) {
                    case 1:
                        System.out.println("Displaying your employee details and attendance...");
                        displayEmployeeDataForUser(loggedInEmployeeNumber);
                        break;
                    case 2:
                        System.out.println("Retrieving your salary details...");
                        displayProcessedSalaryForUser(loggedInEmployeeNumber);
                        break;
                    case 3:
                        System.out.println("Exiting employee menu...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            }
        }
    }

    public static double calculateHoursWorked(int employeeId, String date) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
        for (Attendance record : attendanceRecords) {
            if (record.employeeNumber == employeeId && record.date.equals(date)) {
                try {
                    LocalTime logIn = LocalTime.parse(record.logIn, timeFormatter);
                    LocalTime logOut = LocalTime.parse(record.logOut, timeFormatter);
                    double hoursWorked = Duration.between(logIn, logOut).toMinutes() / 60.0;
    
                    return hoursWorked;
                } catch (Exception e) {
                    System.out.println("Error parsing time for Employee ID: " + employeeId);
                    return 0;
                }
            }
        }
        System.out.println("No attendance record found for Employee ID: " + employeeId + " on " + date);
        return 0;
    }

    public static double calculateMonthlySalary(int employeeId, String monthYear) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        double totalHoursWorked = 0;
        double totalOvertimeHours = 0;
        double hourlyRate = 0;

        // Get hourly rate from employee records
        for (Employee emp : employees) {
            if (emp.employeeNumber == employeeId) {
                hourlyRate = emp.hourlyRate;
                break;
            }   
        }

        if (hourlyRate == 0) {
            System.out.println("Employee ID not found.");
            return 0;
        }

        // Loop through attendance records and sum up valid work hours
        for (Attendance record : attendanceRecords) {
            if (record.employeeNumber == employeeId && record.date.contains(monthYear)) {
                try {
                    LocalDate date = LocalDate.parse(record.date, dateFormatter);
                    DayOfWeek day = date.getDayOfWeek();

                    // Skip Sundays
                    if (day == DayOfWeek.SUNDAY) continue;

                    LocalTime startOfWork = LocalTime.of(8, 30);
                    LocalTime endOfWork = LocalTime.of(17, 30);

                    LocalTime logIn = LocalTime.parse(record.logIn, timeFormatter);
                    LocalTime logOut = LocalTime.parse(record.logOut, timeFormatter);

                    // Ensure valid working hours
                    if (logIn.isBefore(startOfWork)) logIn = startOfWork;
                    if (logOut.isAfter(endOfWork)) logOut = endOfWork;

                    // Calculate valid work hours
                    double dailyHours = Duration.between(logIn, logOut).toMinutes() / 60.0;
                    totalHoursWorked += Math.max(dailyHours, 0);

                    // Check if eligible for overtime (arrived on time)
                    if (!logIn.isAfter(startOfWork) && logOut.isAfter(endOfWork)) {
                        double overtimeHours = Duration.between(endOfWork, logOut).toMinutes() / 60.0;
                        totalOvertimeHours += Math.max(overtimeHours, 0);
                    }

                } catch (Exception e) {
                    System.out.println("Error processing date: " + record.date);
                }
            }
        }

        // Compute gross salary + overtime bonus
        double grossMonthlySalary = totalHoursWorked * hourlyRate;
        double overtimeBonus = totalOvertimeHours * (hourlyRate * 1.25);
        double finalSalary = grossMonthlySalary + overtimeBonus;

        System.out.printf("Overtime Hours: %.2f (Bonus: %.2f)%n", totalOvertimeHours, overtimeBonus);
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
        double pagIbigContr = finalSalary * pagIbigRate;
    
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
                double monthlySalary = calculateMonthlySalary(emp.employeeNumber);
    
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
        for (AttendanceRecord record : attendanceRecords) {
            if (record.employeeNumber == employeeNumber) {
                System.out.printf("%-12s %-8s %-8s\n", record.attendanceDate, record.logIn, record.logOut);
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

static class Employee {
    int employeeNumber;
    String lastName, firstName, birthday, address, phoneNumber, sss, philhealth, tin, pagIbig, status, position, supervisor;
    int basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, semiMonthlyRate, hourlyRate;

    public Employee(int employeeNumber, String lastName, String firstName, String birthday, String address, String phone, String sss, String philhealth, String tin, String pagibig, String status, String position, String supervisor, int basicSalary, int riceSubsidy, int phoneAllowance, int clothingAllowance, int semiMonthlyRate, int hourlyRate) {
        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phone;
        this.sss = sss;
        this.philhealth = philhealth;
        this.tin = tin;
        this.pagIbig = pagibig;
        this.status = status;
        this.position = position;
        this.supervisor = supervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.semiMonthlyRate = semiMonthlyRate;
        this.hourlyRate = hourlyRate;
        }
    }
    
    static class Attendance {
        int employeeNumber;
        String lastName, firstName, date, logIn, logOut;
    
        public Attendance(String[] data) {
            this.employeeNumber = Integer.parseInt(data[0]);
            this.lastName = data[1];
            this.firstName = data[2];
            this.date = data[3];
            this.logIn = data[4];
            this.logOut = data[5];
        }
    }
}