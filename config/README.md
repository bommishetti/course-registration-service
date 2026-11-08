# External configuration

Copy the appropriate `application-{profile}.yml.example` to `application-{profile}.yml`.
Pass `--spring.config.additional-location=optional:file:./config/` when starting the JAR.
Supply DB_URL, DB_USERNAME and DB_PASSWORD through environment variables or a secrets manager.
Never commit real credentials.
