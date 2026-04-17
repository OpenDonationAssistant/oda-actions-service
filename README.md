# ODA Actions Service
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OpenDonationAssistant/oda-actions-service)
![Sonar Tech Debt](https://img.shields.io/sonar/tech_debt/OpenDonationAssistant_oda-actions-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Violations](https://img.shields.io/sonar/violations/OpenDonationAssistant_oda-actions-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Tests](https://img.shields.io/sonar/tests/OpenDonationAssistant_oda-actions-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Coverage](https://img.shields.io/sonar/coverage/OpenDonationAssistant_oda-actions-service?server=https%3A%2F%2Fsonarcloud.io)

## Running with Docker

### Using Docker Run

```bash
docker run -d \
  --name oda-actions-service \
  -p 8080:8080 \
  -e JDBC_URL=jdbc:postgresql://postgres-host:5432/actions \
  -e JDBC_USER=postgres \
  -e JDBC_PASSWORD=your-password \
  ghcr.io/opendonationassistant/actions-service:latest
```

### Using Docker Compose

```yaml
services:
  oda-actions-service:
    image: ghcr.io/opendonationassistant/actions-service:latest
    ports:
      - "8080:8080"
    environment:
      - JDBC_URL=jdbc:postgresql://postgres:5432/actions
      - JDBC_USER=postgres
      - JDBC_PASSWORD=postgres
    depends_on:
      - postgres

  postgres:
    image: postgres:17
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_DB=actions
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JDBC_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost/postgres?currentSchema=actions` |
| `JDBC_USER` | Database username | `postgres` |
| `JDBC_PASSWORD` | Database password | `postgres` |

### Building Locally

```bash
mvn clean package
docker build -t oda-actions-service .
```

