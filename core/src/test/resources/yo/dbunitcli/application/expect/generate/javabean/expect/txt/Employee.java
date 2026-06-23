package yo.example;

/** 社員マスタ */
public class Employee {
    /** 社員ID */
    private Integer employeeId;
    /** 氏名 */
    private String employeeName;
    /** 給与 */
    private java.math.BigDecimal salary;

    public Integer getEmployeeId() {
        return this.employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }
    public String getEmployeeName() {
        return this.employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    public java.math.BigDecimal getSalary() {
        return this.salary;
    }

    public void setSalary(java.math.BigDecimal salary) {
        this.salary = salary;
    }
}