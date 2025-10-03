# Security Dashboard Troubleshooting Guide

## 🚨 **Current Issue: 403 Forbidden Errors**

The Security Dashboard is receiving **403 Forbidden** responses from the backend API endpoints. This indicates an authentication/authorization problem.

## 🔍 **Root Cause Analysis**

### **1. Authentication Required**
- **Problem**: All Security Dashboard endpoints require a valid JWT token
- **Solution**: User must be logged in with a valid session

### **2. Admin Role Required**
- **Problem**: Security Dashboard endpoints require `ROLE_ADMIN` authority
- **Solution**: User must have `userType: 'ADMIN'` in their profile

### **3. Missing Backend Endpoints**
- **Problem**: Some endpoints like `/api/system/metrics` don't exist yet
- **Solution**: Create missing backend endpoints or use fallback data

## 🔧 **How to Fix**

### **Step 1: Check Authentication Status**
1. **Open Browser Console** (F12 → Console)
2. **Look for these messages**:
   ```
   Security Dashboard initialized
   Authentication check passed. User: [username] Role: ADMIN
   ```
3. **If you see authentication errors**, you need to log in

### **Step 2: Login as Admin User**
1. **Navigate to**: `/login`
2. **Use admin credentials**:
   - Username: `admin` (or your admin username)
   - Password: Your admin password
3. **Verify user type**: Check that `userType` is `ADMIN`

### **Step 3: Check Backend Status**
1. **Click "Check Backend" button** in the Security Dashboard
2. **Look for response**:
   - ✅ **Success**: Backend is accessible
   - ❌ **Error**: Backend connection issues

### **Step 4: Verify JWT Token**
1. **Open Browser Console** (F12 → Console)
2. **Check localStorage**:
   ```javascript
   localStorage.getItem('token')  // Should return a JWT string
   localStorage.getItem('userType')  // Should return 'ADMIN'
   ```

## 🚀 **Quick Fix Commands**

### **If Backend is Not Running**:
```bash
# Navigate to backend directory
cd microsponsoring-backend

# Start the Spring Boot application
mvn spring-boot:run
```

### **If Database Issues**:
```bash
# Check MySQL service
net start mysql

# Or use the setup script
setup-database.bat
```

### **If Frontend Issues**:
```bash
# Navigate to frontend directory
cd microsponsoring-frontend

# Install dependencies
npm install

# Start development server
ng serve
```

## 📋 **Expected Behavior**

### **When NOT Authenticated**:
- ❌ Red authentication warning
- ❌ "Go to Login" button
- ❌ No dashboard content visible
- ❌ No API calls made

### **When Authenticated as Admin**:
- ✅ Green user info bar
- ✅ "Check Backend" button
- ✅ Full dashboard content visible
- ✅ API calls with JWT token

### **When Backend is Accessible**:
- ✅ API calls succeed
- ✅ Real data loads
- ✅ No 403 errors

### **When Backend is NOT Accessible**:
- ❌ API calls fail with 403/connection errors
- ❌ Fallback sample data shows
- ❌ Error messages in console

## 🔐 **Authentication Flow**

```
1. User visits /dashboard/admin/security
2. Component checks localStorage for token
3. Component checks userType === 'ADMIN'
4. If both pass → Load dashboard content
5. If either fails → Show authentication warning
6. API calls include: Authorization: Bearer <token>
```

## 🛠️ **Backend Endpoints Required**

The Security Dashboard needs these endpoints:

### **✅ Existing**:
- `GET /api/security/dashboard` - Security overview
- `GET /api/security/vulnerabilities` - Vulnerability list
- `POST /api/security/scan` - Run security scan
- `POST /api/security/fix` - Fix vulnerabilities

### **❌ Missing**:
- `GET /api/system/metrics` - System resource usage
- `GET /api/users/stats` - User statistics
- `GET /api/security/alerts` - Security alerts
- `POST /api/admin/force-logout-all` - Admin actions
- `POST /api/admin/refresh-system` - Admin actions

## 🎯 **Next Steps**

1. **Login as admin user**
2. **Check backend connectivity**
3. **Verify JWT token is valid**
4. **Create missing backend endpoints** (if needed)
5. **Test Security Dashboard functionality**

## 📞 **Need Help?**

If you're still experiencing issues:

1. **Check browser console** for error messages
2. **Verify backend is running** on port 8080
3. **Confirm admin user exists** in database
4. **Check JWT token expiration** (24 hours)
5. **Review backend logs** for authentication errors

---

**Last Updated**: January 2025
**Version**: 1.0.0




