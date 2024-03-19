package payrollSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class DetailsAndComputation {
    public static String employee_hourly_rate;
    public static String[] employee;

    public static void main(String[] args) {
        boolean conts = true;
        while (conts) {
            System.out.println("======= Start of Session ===========");
            define_data_type();

            if (employee.length != 0) {
                System.out.println("Employee ID: " + employee[0]);
                System.out.println("Name: " + employee[1] + ", " + employee[2]);
                System.out.println("Position: " + employee[11]);
                System.out.println("Hourly salary: " + employee[18]);
                System.out.println("Basic Salary: " + sanitize_data(employee[13]));

                System.out.println("============== Calculation ==============");

                // Enter specified date for computation
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    System.out.print("Enter YYYY/MM: ");
                    String dateInput = reader.readLine();
                    calculateDutyHours(dateInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No data found. Please try again");
            }
            System.out.println("======= End of Session ===========");
            System.out.println("**********************************");
        }
    }

    public static void reset_data() {
        employee_hourly_rate = "";
        employee = new String[0];
    }

    public static void define_data_type() {
        reset_data();
        System.out.print("Enter an Employee number: ");

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String userInput = inputReader.readLine();

            String employee_detail = get_employee_details(userInput);

            if (!employee_detail.equals("")) {
                String[] row = employee_detail.split(",");
                employee = row;
                employee_hourly_rate = row[18];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sanitize_data(String info) {
        return info.replace(";x;", ",").replace("\"", "");
    }

    public static String get_employee_details(String employee_id) {
        String file = System.getProperty("user.dir") + "/src/employee_details.csv";

        BufferedReader reader = null;
        String line = "";
        String employee_found = "";

        try {
            reader = new BufferedReader(new FileReader(file));

            while ((line = reader.readLine()) != null) {
                String repl = line.replaceAll(",(?!(([^\"]*\"){2})*[^\"]*$)", ";x;");
                String[] row = repl.split(",");

                if (Integer.parseInt(row[0]) == Integer.parseInt(employee_id)) {
                    employee_found = repl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return employee_found;
    }

    public static void calculateDutyHours(String yearMonth) {
        String file = System.getProperty("user.dir") + "/src/attendance_record.csv";
        BufferedReader reader = null;
        double totalTimeout = 0.0;
        double totalTimein = 0.0;
        boolean dateComplete = false;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String[] dateParts = parts[1].split("/");
                    if (dateParts.length == 3) {
                        String recordYearMonth = dateParts[2] + "/" + dateParts[0]; // Year/Month format
                        if (recordYearMonth.equals(yearMonth)) {
                            String[] timeInParts = parts[2].split(":");
                            String[] timeOutParts = parts[3].split(":");
                            int timeInHour = Integer.parseInt(timeInParts[0]);
                            int timeInMinute = Integer.parseInt(timeInParts[1]);
                            int timeOutHour = Integer.parseInt(timeOutParts[0]);
                            int timeOutMinute = Integer.parseInt(timeOutParts[1]);
                            totalTimein += timeInHour + (timeInMinute / 60.0);
                            totalTimeout += timeOutHour + (timeOutMinute / 60.0);
                            dateComplete = true;
                        } else {
                            break; //stop ng while-loop 
                        }
                    }
                }
            }

            if (dateComplete) {
                double monthlyDutyHours = totalTimeout - totalTimein;
                System.out.println("Total Duty Hours for " + yearMonth + ": " + monthlyDutyHours);
                
            // Calculate Gross Pay
            double hourlySalary = Double.parseDouble(employee_hourly_rate);
            double grossPay = monthlyDutyHours * hourlySalary;
            

            // Calculate Pagibig, Philhealth, and SSS
            double pagibig = grossPay * 0.02;
            double philhealth = grossPay * 0.025;
            double sss = 0.0;
            if (grossPay >= 22750 && grossPay < 23250) {
                sss = 1012.50;
            } else if (grossPay >= 23250 && grossPay < 23750) {
                sss = 1057.50;
            } else if (grossPay >= 23750 && grossPay < 24250) {
                sss = 1080.00;
            } else if (grossPay >= 24750 && grossPay < 25250) {
                sss = 1125.00;
            } else if (grossPay >= 29750) {
                sss = 1350.00;
            }


            // Calculate Net Pay
            double netPay = grossPay - pagibig - philhealth - sss;
            

            // Calculate Tax
            double tax = 0.0;
            if (netPay < 20833) {
                tax = 0.0;
            } else if (netPay >= 20833 && netPay <= 33332) {
                tax = (netPay - 20833) * 0.15;
            } else if (netPay >= 33332 && netPay <= 66666) {
                tax = (netPay - 33333) * 0.20;
            } else if (netPay >= 66667 && netPay <= 166666) {
            	tax = (netPay - 66667) * 0.25; 
            }
            
            System.out.println("Gross Pay: " + roundUp(grossPay));
            System.out.println("Net Pay: " + roundUp(netPay));
            System.out.println("Tax: " + roundUp(tax));
            System.out.println("------- Government Deduction -------");
            System.out.println("Pagibig: " + roundUp(pagibig));
            System.out.println("Philhealth: " + roundUp(philhealth));
            System.out.println("SSS: " + roundUp(sss));

            } else {
                System.out.println("No data found for the specified month and year.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String roundUp(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }
}
