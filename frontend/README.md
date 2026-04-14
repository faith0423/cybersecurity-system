# Cyber Incident Intelligence Angular Frontend

This frontend is designed for a Spring Boot backend secured with JWT.

## Expected backend endpoints

- `POST /api/auth/login`
- `GET /api/incidents`
- `POST /api/incidents`
- `DELETE /api/incidents/{id}`

## Run steps

1. Open the folder in VS Code.
2. Run `npm install`.
3. Start Angular with `npm start`.
4. Make sure Spring Boot is running on `http://localhost:8080`.
5. The Angular proxy forwards `/api/*` requests to Spring Boot.

## If your backend endpoints are different

Update these files:

- `src/app/services/auth.service.ts`
- `src/app/services/incident.service.ts`
- `proxy.conf.json`

## Typical JWT backend response for login

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@example.com",
  "role": "ADMIN",
  "name": "Admin User"
}
```
