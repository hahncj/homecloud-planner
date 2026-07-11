.PHONY: db-up db-down up down logs

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f
