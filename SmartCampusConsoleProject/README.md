# Smart Campus Resource Booking System — Console Project

This is a menu-driven Java 17 console application using JDBC and MySQL. It is separate from the Spring Boot web/API project.

## Features
- Add, view, update, and delete campus resources.
- Create booking requests for a resource, date, and time.
- Detect overlapping booking requests for the same resource.
- View bookings and search by date.
- Approve, reject, or cancel booking requests.
- Automatically creates the required MySQL tables.

## Requirements
- JDK 17+
- Eclipse IDE with Maven support
- MySQL Server

## Configure MySQL
Open `src/main/java/com/campus/console/Database.java` and replace `CHANGE_ME` with your MySQL password. The connection uses username `root` and database `smart_campus_db`.

If needed, run this once in MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS smart_campus_db;
```

## Import into Eclipse
1. Extract this ZIP.
2. In Eclipse choose `File > Import > Maven > Existing Maven Projects`.
3. Select the extracted `SmartCampusConsoleProject` folder containing `pom.xml`.
4. Ensure the project is checked and click Finish.
5. Wait for Maven dependencies to download.
6. Open `src/main/java > com.campus.console > SmartCampusConsoleApplication.java`.
7. Right-click and choose `Run As > Java Application`.
8. Use the Eclipse Console to enter menu numbers.

## Menu
1 Add resource
2 View resources
3 Update resource
4 Delete resource
5 Create booking
6 View bookings
7 Search bookings by date
8 Update booking status
0 Exit

Dates must use `YYYY-MM-DD`; times must use `HH:MM` (24-hour clock).

This is a console-based educational project. It does not include a browser UI or user authentication.
