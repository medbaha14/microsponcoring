# Configuration Guide for Microsponsoring Backend

## Overview
This guide explains all the configuration properties needed for your Spring Boot application to run successfully.

## Required Configuration Properties

### 1. Database Configuration
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/microsponsoring?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 2. JPA/Hibernate Configuration
```properties
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 3. Liquibase Configuration
```properties
# Liquibase Configuration
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### 4. Server Configuration
```properties
# Server Configuration
server.port=8080
```

### 5. JWT Configuration
```properties
# JWT Configuration
jwt.secret=your-secret-key-here-change-in-production
jwt.expiration=86400000
```

### 6. Checkout/Stripe Configuration
```properties
# Checkout/Stripe Configuration
checkout.secret.key=sk_test_your_stripe_secret_key_here
checkout.public.key=pk_test_your_stripe_public_key_here
```

### 7. File Upload Configuration
```properties
# File Upload Configuration
file.upload-dir=./uploads
```

### 8. Password Reset Configuration
```properties
# Password Reset Configuration
password.reset.token.expiration=3600000
```

### 9. Email Configuration
```properties
# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=bousnina.baha14@gmail.com
spring.mail.password=your_app_password_here
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 10. Logging Configuration
```properties
# Logging Configuration
logging.level.com.example.microsponsoringbackend=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Configuration Steps

### Step 1: Update Stripe Keys
Replace the placeholder Stripe keys with your actual keys:
```properties
checkout.secret.key=sk_test_your_actual_stripe_secret_key
checkout.public.key=pk_test_your_actual_stripe_public_key
```

**To get Stripe keys:**
1. Sign up at https://stripe.com
2. Go to Developers → API keys
3. Copy your publishable key and secret key

### Step 2: Update Email Configuration
For Gmail, you need to:
1. Enable 2-factor authentication on your Gmail account
2. Generate an App Password:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate a password for "Mail"
3. Replace `your_app_password_here` with the generated password

### Step 3: Update JWT Secret
Generate a secure random string for JWT:
```properties
jwt.secret=your-very-long-random-secret-key-here
```

## Environment Variables (Recommended for Production)

Instead of hardcoding sensitive values, use environment variables:

```bash
# Database
export SPRING_DATASOURCE_PASSWORD=your_mysql_password
export SPRING_DATASOURCE_USERNAME=your_mysql_username

# Stripe
export CHECKOUT_SECRET_KEY=sk_live_your_production_key
export CHECKOUT_PUBLIC_KEY=pk_live_your_production_key

# JWT
export JWT_SECRET=your_production_jwt_secret

# Email
export SPRING_MAIL_PASSWORD=your_gmail_app_password
```

Then update your `application.properties`:
```properties
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
checkout.secret.key=${CHECKOUT_SECRET_KEY:sk_test_your_stripe_secret_key_here}
checkout.public.key=${CHECKOUT_PUBLIC_KEY:pk_test_your_stripe_public_key_here}
jwt.secret=${JWT_SECRET:your-secret-key-here-change-in-production}
spring.mail.password=${SPRING_MAIL_PASSWORD:your_app_password_here}
```

## Testing Configuration

### 1. Test Database Connection
```bash
mysql -u root -p -e "USE microsponsoring; SHOW TABLES;"
```

### 2. Test Email Configuration
The application will test email configuration on startup. Check logs for any email-related errors.

### 3. Test Stripe Configuration
If you have test Stripe keys, the checkout functionality should work in test mode.

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure MySQL is running
   - Verify database exists
   - Check username/password

2. **Stripe Configuration Error**
   - Verify Stripe keys are correct
   - Ensure keys match your Stripe account (test vs live)

3. **Email Sending Failed**
   - Check Gmail app password
   - Verify 2FA is enabled
   - Check firewall/network settings

4. **JWT Configuration Error**
   - Ensure JWT secret is not empty
   - Use a long, random string

### Log Analysis
Enable debug logging to see detailed configuration information:
```properties
logging.level.org.springframework.boot.autoconfigure=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Security Notes

### Production Checklist
- [ ] Change default JWT secret
- [ ] Use strong MySQL password
- [ ] Use production Stripe keys
- [ ] Use environment variables for secrets
- [ ] Disable debug logging
- [ ] Use HTTPS
- [ ] Set appropriate CORS policies

### File Permissions
Ensure the upload directory is properly secured:
```bash
chmod 755 ./uploads
chown your_user:your_group ./uploads
```

## Next Steps

After configuring all properties:
1. Start your application: `mvn spring-boot:run`
2. Check logs for any remaining configuration errors
3. Test the application endpoints
4. Verify database tables are created by Liquibase
5. Test payment functionality with Stripe test keys
6. Test email functionality

## Support

If you encounter configuration issues:
1. Check the application logs for specific error messages
2. Verify all required properties are set
3. Test individual components (database, email, Stripe) separately
4. Use the debug logging to see what Spring Boot is trying to configure

