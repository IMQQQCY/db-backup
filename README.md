# DB Backup Server

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-blue" alt="Vue 3">
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
  <img src="https://img.shields.io/badge/Docker-Ready-blue" alt="Docker">
</p>

A lightweight, self-hosted MySQL database automatic backup service with a modern web UI. Supports scheduled backups, multiple storage backends (local/NFS), email notifications, and flexible retention policies.

## Features

- **Multi-DataSource Management** — Configure and manage multiple MySQL database connections
- **Flexible Backup Types** — Full database or partial table backup
- **Backup Content Options** — Structure + Data or Structure-only (DDL) backup
- **Scheduled Backups** — Cron-based scheduling with Quartz, includes expression testing and common templates
- **Storage Backends** — Local storage or NFS remote storage (dynamic mount/unmount)
- **Retention Policies** — Configurable retention days, automatic cleanup of expired backups
- **Email Notifications** — Success/failure notifications via SMTP
- **Modern Web UI** — Vue 3 + Element Plus with gradient design, responsive layout
- **Docker Ready** — One-command deployment with Docker Compose
- **Timezone Aware** — Proper Asia/Shanghai timezone support in containers

## Architecture

```
┌─────────────────┐     ┌──────────────────────────────┐
│   Vue 3 Web UI  │────▶│   Spring Boot Backend (8080) │
│  (Element Plus) │     │                              │
└─────────────────┘     │  ┌────────────────────────┐  │
                        │  │  Quartz Scheduler      │  │
                        │  │  (Cron-based tasks)    │  │
                        │  └────────────────────────┘  │
                        │  ┌────────────────────────┐  │
                        │  │  mysqldump executor    │  │
                        │  │  (--single-transaction)│  │
                        │  └────────────────────────┘  │
                        │  ┌────────────────────────┐  │
                        │  │  Storage (Local / NFS) │  │
                        │  └────────────────────────┘  │
                        └──────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 8, Spring Boot 2.7.18, MyBatis-Plus 3.5.5 |
| Scheduler | Quartz |
| Database | MySQL (metadata storage) |
| Backup Tool | mysqldump + Gzip compression |
| Frontend | Vue 3, Vite 5, Element Plus |
| Deployment | Docker, Docker Compose |

## Quick Start

### Prerequisites

- Docker & Docker Compose
- A MySQL instance for metadata storage (the backup service's own database)

### 1. Clone the repository

```bash
git clone https://github.com/your-username/db-backup-server.git
cd db-backup-server
```

### 2. Configure environment

Edit `docker-compose.yml` to set your metadata database connection:

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/db_backup?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
  - SPRING_DATASOURCE_USERNAME=root
  - SPRING_DATASOURCE_PASSWORD=your-password
```

### 3. Initialize the database

Execute `db-backup-server/src/main/resources/schema.sql` on your MySQL instance to create the required tables.

### 4. Build and run

```bash
docker-compose up -d --build
```

### 5. Access the UI

Open your browser and visit: `http://localhost:8080`

## Screenshots

The web UI provides the following pages:

- **Dashboard** — Overview with statistics cards and recent backup history
- **DataSource Management** — Add/edit/test MySQL connections
- **Backup Tasks** — Create scheduled tasks with cron expression testing
- **Backup History** — View all backup records with status indicators
- **NFS Configuration** — Manage NFS mount points
- **Mail Configuration** — SMTP settings for notifications

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Metadata database JDBC URL | `jdbc:mysql://localhost:3306/db_backup` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `root` |
| `BACKUP_TEMP_DIR` | Temporary directory for backup files | `./backup-temp` |

### Backup Options

| Option | Description |
|--------|-------------|
| Backup Type | `FULL` (entire database) or `PARTIAL` (selected tables) |
| Backup Content | `STRUCTURE_DATA` (structure + data) or `STRUCTURE` (DDL only) |
| Storage Type | `LOCAL` (local filesystem) or `NFS` (remote NFS mount) |
| Cron Expression | Standard 6-field cron (sec min hour day month weekday) |
| Retain Days | Number of days to keep backup files |

## Development

### Backend

```bash
cd db-backup-server
mvn clean package -DskipTests
java -jar target/db-backup-server-1.0.0.jar
```

### Frontend

```bash
cd db-backup-web
npm install
npm run dev    # Development server at http://localhost:5173
npm run build  # Production build
```

## Project Structure

```
db-backup-server/
├── db-backup-server/          # Backend (Spring Boot)
│   ├── src/main/java/com/backup/server/
│   │   ├── controller/        # REST API controllers
│   │   ├── service/           # Business logic
│   │   ├── entity/            # Database entities
│   │   ├── mapper/            # MyBatis-Plus mappers
│   │   ├── dto/               # Data transfer objects
│   │   ├── enums/             # Enumerations
│   │   ├── config/            # Configuration classes
│   │   └── util/              # Utilities (MySqlDumpUtil, NfsMountUtil)
│   ├── src/main/resources/
│   │   ├── application.yml    # Application config
│   │   └── schema.sql         # Database schema
│   └── Dockerfile
├── db-backup-web/             # Frontend (Vue 3)
│   ├── src/
│   │   ├── views/             # Page components
│   │   ├── api/               # API request modules
│   │   └── router/            # Vue Router config
│   └── package.json
└── docker-compose.yml
```

## FAQ

**Q: Why does backup fail with "View references invalid table" error?**

A: The backup tool uses `--force` flag to skip problematic views/tables. If you still encounter issues, check that the backup user has sufficient privileges on all views.

**Q: How to backup to NFS?**

A: Configure NFS in the web UI (NFS Configuration page), then select NFS as storage type when creating a backup task. The service will dynamically mount/unmount NFS during backup execution.

**Q: Can I run it without Docker?**

A: Yes. Build the backend JAR and run it directly with Java 8+. Make sure `mysqldump` is available in your system PATH.

## License

This project is licensed under the MIT License.
