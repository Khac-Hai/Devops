package com.quickbite.hr;

import com.quickbite.hr.model.Employee;
import com.quickbite.hr.repository.EmployeeRepository;
import com.quickbite.hr.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HrServiceApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Test context loads and save/find employee")
    void testEmployeeDatabaseIntegration() {
        Employee employee = new Employee("Nguyen Van A", "nguyenvana@quickbite.com", "Engineering", 1500.0);
        Employee saved = employeeService.saveEmployee(employee);

        assertNotNull(saved.getId(), "Employee ID should not be null after saving");

        Optional<Employee> found = employeeService.getEmployeeById(saved.getId());
        assertTrue(found.isPresent(), "Saved employee should be found");
        assertEquals("Nguyen Van A", found.get().getFullName());
        assertEquals("nguyenvana@quickbite.com", found.get().getEmail());

        List<Employee> all = employeeService.getAllEmployees();
        assertFalse(all.isEmpty(), "Employee list should not be empty");
    }
}
