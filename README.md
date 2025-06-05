
<img width="328" alt="Screenshot 2025-06-03 at 11 23 59 PM" src="https://github.com/user-attachments/assets/10369e97-8c7f-4490-a508-06c76c59cd31" />
<img width="276" alt="Screenshot 2025-06-03 at 11 24 35 PM" src="https://github.com/user-attachments/assets/bcc3366d-b82f-4ede-9588-2e183eb97ab9" />

# IVC Database Management System

---

## 1. Introduction

This project is a comprehensive implementation of a database management system for the fictional IV College (IVC). The system is engineered to manage student, course, and enrollment data, providing dedicated interfaces for both students and administrative staff. It is built using Java with a JDBC connection to an Oracle DBMS backend, ensuring transactional integrity, data security, and robust performance.

---

## 2. System Architecture & Design Philosophy

The system is designed following a classic three-tier logical architecture to separate concerns and enhance maintainability.

- **Presentation Layer (Interfaces):** The `GoldInterface` (for students) and `RegistrarInterface` (for staff) handle all user interaction. These Java classes are responsible for parsing user commands, validating input syntax, and invoking the appropriate methods in the business logic layer. A key design choice was to implement visually distinct, color-coded CLI themes for each interface to improve usability and minimize operator error.

- **Business Logic Layer (Database Manager):** The `DatabaseManager` class serves as the core of the application. It encapsulates all business rules and database operations (SQL queries and transactions). By centralizing data access logic here, we ensure that all interactions with the database are consistent and adhere to the rules specified in the project document (e.g., enrollment limits, prerequisite checking, PIN security).

- **Data Access Layer (JDBC):** Low-level communication with the Oracle DBMS is handled by the Oracle JDBC driver. This layer is responsible for establishing connections, executing SQL statements prepared by the `DatabaseManager`, and managing `ResultSet` objects.

---

## 3. Data Model & Schema Implementation

The database schema was designed to accurately model the entities and relationships described in the project specification.

- **Core Entities:**

  - `student`: Stores perm number, name, address, major, and the hashed PIN.
  - `course`: Stores unique course numbers and their corresponding titles.
  - `major_managedby`: Defines majors, the department that manages them, and the number of required electives.
  - `department`: Stores department information.

- **Relational Entities:**
  - `courseoffering_offeredin`: Represents a specific instance of a course being offered in a given quarter (`yr_qtr`). It links a `course` to a time, professor, and enrollment capacity.
  - `takes_courses`: The central enrollment table, linking a `student` to a `courseoffering_offeredin`. It records the student's grade for that course. This table is the source of truth for both past and current enrollments.
  - `prerequisites`: A self-referencing link on the `course` table, defining prerequisite dependencies.
  - `mandatory` & `elective`: Link courses to majors, defining graduation requirements.

All primary key, foreign key, and check constraints were implemented directly in the database using SQL `CREATE TABLE` statements (`schema.sql`) to enforce data integrity at the lowest level.

---

## 4. Key Technical Implementations

### 4.1. Transactional Integrity

All database modifications are handled as atomic transactions to prevent data corruption. For complex operations involving multiple `UPDATE`, `INSERT`, or `DELETE` statements (e.g., `dropCourse`, `enterGradesFromFile`), the system explicitly manages transaction boundaries:

1.  `connection.setAutoCommit(false);` is called to initiate the transaction block.
2.  All SQL statements within the transaction are executed.
3.  If all operations succeed, `connection.commit();` is called to make the changes permanent.
4.  If any operation fails or a business rule is violated, `connection.rollback();` is called to discard all changes within the transaction, leaving the database in its original state.

### 4.2. Security: PIN Hashing

As per the project specification, student PINs are treated as highly sensitive data. To implement this:

- A one-way cryptographic hash function (**SHA-256**) is used to secure PINs.
- The `hashPin(String pin)` method in `DatabaseManager` computes the SHA-256 hash of a given plaintext PIN.
- The `student` table stores **only the hashed PIN**, never the plaintext version.
- **Verification (`verifyPin`):** To verify a PIN, the user-supplied PIN is hashed, and the result is compared against the stored hash in the database. This prevents exposing the original PIN.
- **Modification (`setPin`):** The `setPin` method first calls `verifyPin` to authorize the change, then stores the hash of the _new_ PIN. Direct database queries cannot read or reverse-engineer the PIN.

### 4.3. Algorithmic Implementations

- **Requirements Check (`requirementsCheck`):** This function is implemented using `Set` data structures for efficiency. It fetches mandatory and elective courses for a student's major into two sets. A third set is populated with all "passed" courses (including those in the current quarter, as specified). By using `Set.removeAll()` and filtering operations, the system efficiently calculates the remaining mandatory courses and the required number of electives.

- **Study Plan Generation (`makePlan`):** This feature is implemented as a greedy algorithm. It iterates through the next 12 academic quarters, starting from the one after `CURRENT_QTR`. In each quarter, it:
  1.  Identifies all available courses whose prerequisites have been met (by checking against a dynamically updated set of "passed" courses).
  2.  Greedily selects up to 5 courses, prioritizing remaining mandatory courses first, followed by electives if needed.
  3.  Adds the selected courses to the "passed" set for the next iteration, simulating their completion.

---

