.PHONY: db-up db-down up down build logs ps backend-run

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

up:
	docker compose up -d

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f

ps:
	docker compose ps

backend-run:
	set -a && . ./.env && set +a && cd backend && ./gradlew bootRun
