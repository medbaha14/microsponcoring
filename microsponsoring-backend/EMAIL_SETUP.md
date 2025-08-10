# Email Setup for Password Reset Feature

This document explains how to configure the email settings for the password reset feature using Mailtrap.

## Configuration

### 1. Mailtrap Setup

1. Go to [Mailtrap](https://mailtrap.io) and create an account
2. Create a new inbox for your project
3. Go to the inbox settings and copy the SMTP credentials

### 2. Environment Variables

Set the following environment variables in your `application.properties` or as system environment variables:

```properties
# Mailtrap Configuration
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password

# Password Reset Configuration
PASSWORD_RESET_TOKEN_EXPIRATION=3600000
```

### 3. Your Mailtrap Credentials

Based on your provided information:
- **Token Name**: admintoken
- **Token**: 5ce360bd1d8389e2fb4eebf05ba05285

You'll need to get the actual SMTP credentials from your Mailtrap inbox settings, not the API token.

### 4. Testing

1. Start the backend application
2. Navigate to the forgot password page in the frontend
3. Enter a valid email address
4. Check your Mailtrap inbox for the password reset email

## Security Notes

- The password reset token expires after 1 hour (3600000 milliseconds)
- Tokens are single-use only
- Old tokens are automatically deleted when a new one is created
- The system doesn't reveal whether an email exists or not for security reasons

## Troubleshooting

1. **Email not sending**: Check your Mailtrap credentials and network connectivity
2. **Token validation failing**: Ensure the database migration has run successfully
3. **Frontend not connecting**: Verify the backend is running on the correct port (8080)

## Database Migration

The password reset feature requires a new database table. The migration will run automatically when you start the application, or you can run it manually:

```bash
# If using Maven
mvn liquibase:update
``` 