# ⚙️ Advanced Asynchronous Job Scheduler

A feature-rich, scalable, and extensible **Asynchronous Job Scheduler** built with **Spring Boot** and **MySQL**, designed to trigger and manage background tasks via REST APIs.

---

## ✨ Features

* ✅ Trigger single or multiple jobs asynchronously
* 📊 Real-time job progress tracking
* 🔁 Retry mechanism with configurable attempts
* ❌ Job cancellation (before or during execution)
* 🧾 Execution history with logs and result messages
* 🕒 Timestamps: start time, end time
* 📂 Job categorization by type
* ⚡ Parallel execution with `@Async`
* 📌 Planned: search, pagination, cron-based jobs, email alerts

---

## 📐 Technologies

* Java 17
* Spring Boot
* Spring Data JPA
* MySQL
* Maven

---

## 🔗 API Endpoints

| Method | Endpoint             | Description                 |
| ------ | -------------------- | --------------------------- |
| POST   | `/jobs/trigger`      | Trigger a single async job  |
| POST   | `/jobs/trigger-bulk` | Trigger multiple async jobs |
| GET    | `/jobs/{id}`         | Get job by ID               |
| GET    | `/jobs/all`          | Fetch all jobs              |
| POST   | `/jobs/{id}/cancel`  | Cancel a job                |

---

## 🧪 Example Payloads

**Trigger Single Job**

```json
{
  "jobName": "SendInvoiceEmail",
  "jobType": "EMAIL",
  "parameters": "{\"invoiceId\":123}",
  "maxRetries": 3
}
```

**Trigger Multiple Jobs**

```json
[
  {
    "jobName": "GenerateReport",
    "jobType": "REPORT",
    "parameters": "{\"type\":\"monthly\"}"
  },
  {
    "jobName": "CleanupLogs",
    "jobType": "MAINTENANCE",
    "parameters": "{}"
  }
]
```

---

## 🏃 Run Locally

### Prerequisites

* Java 17+
* MySQL
* Maven

### Setup

1. Clone the repo:

```bash
git clone https://github.com/AnimeshRoy415/async-job-scheduler.git
cd async-job-scheduler
```

2. Configure DB in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/job_scheduler_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

3. Run the app:

```bash
mvn clean install
mvn spring-boot:run
```

---

## 📁 Sample Project Structure

```
├── controller/
├── service/
├── repository/
├── model/
├── config/
├── utils/
└── Application.java
```

---

## 🧠 Job Status Lifecycle

* `PENDING`
* `RUNNING`
* `SUCCESS`
* `FAILED`
* `CANCELLED`

---

## 📬 Contact

**Animesh Roy**
📧 [animesh.roy.415@gmail.com](mailto:animesh.roy.415@gmail.com)
🌐 [GitHub Profile](https://github.com/AnimeshRoy415)

