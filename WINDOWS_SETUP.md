# Windows Setup

The active application uses Java 21, Spring Boot, React, and Vite. The original C++ sources and their setup instructions are preserved in [`archive/cpp/`](archive/cpp/).

## Prerequisites

- JDK 21
- Maven 3.9.9 or newer (or the bundled `maven\bin\mvn.cmd`, if present)
- Node.js 20.19 or newer with npm
- MySQL 8 or a compatible MySQL server

Check the installed tools:

```powershell
java -version
mvn -version
node --version
npm --version
```

If you use the bundled Maven, replace `mvn` in the commands below with `.\maven\bin\mvn.cmd`.

## Configure and Run

From the repository root, set the MySQL connection values in PowerShell:

```powershell
$env:MYSQL_HOST = "localhost"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DB = "dpi_db"
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "<your-mysql-password>"
```

Start the backend:

```powershell
mvn -f backend\pom.xml spring-boot:run
```

In a second PowerShell terminal, start the frontend:

```powershell
npm --prefix frontend install
npm --prefix frontend run dev
```

Open the dashboard at `http://127.0.0.1:5173`. Vite proxies `/api` requests to `http://localhost:8080`. The legacy JSP pages are `http://127.0.0.1:8080/legacy/report` and `http://127.0.0.1:8080/legacy/rules`.

## Build and Test

Build the frontend and package the executable Spring Boot WAR:

```powershell
npm --prefix frontend run build
mvn -f backend\pom.xml package
java -jar backend\target\dpi-backend-1.0.0.war
```

Run backend tests and validate the frontend build:

```powershell
mvn -f backend\pom.xml test
npm --prefix frontend run build
```

The backend listens on port 8080. For troubleshooting MySQL, ports, frontend proxying, and JSP rendering, see the troubleshooting section in [`README.md`](README.md#10-building-and-running).
