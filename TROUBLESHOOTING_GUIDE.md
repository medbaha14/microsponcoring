# Security Dashboard Troubleshooting Guide

## 🚨 **API Connection Issues**

### **Problem: API calls going to wrong URL**
- **Error**: `HttpErrorResponse {status: 200, statusText: 'OK', url: 'http://localhost:4200/api/...'}`
- **Cause**: Angular is making API calls to the development server (port 4200) instead of the backend (port 8080)
- **Solution**: Fixed by updating component to use `environment.apiUrl`

### **Current Configuration**
```typescript
// environment.ts (Development)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:80/api',  // ✅ Correct backend URL
  // ... other URLs
};

// environment.pod.ts (Kubernetes)
export const environment = {
  production: true,
  apiUrl: '/api',  // ✅ Relative URL for Kubernetes
  // ... other URLs
};
```

## 🔧 **How to Fix**

### **1. Verify Backend is Running**
```bash
# Check if Spring Boot backend is running on port 8080
curl http://localhost:80/actuator/health
# Should return: {"status":"UP"}
```

### **2. Check Angular Environment**
```bash
# In Angular project directory
cd microsponsoring-frontend
# Verify environment.ts has correct apiUrl
cat src/environments/environment.ts
```

### **3. Test API Connection**
- Open Security Dashboard in browser
- Click "Test Connection" button
- Check browser console for connection status
- Look for connection status indicator in bottom-right corner

### **4. Verify CORS Configuration**
Backend should allow requests from `http://localhost:4200`:

```java
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
public class SecurityController {
    // ... controller methods
}
```

## 🐛 **Debug Steps**

### **Step 1: Check Browser Console**
```javascript
// Look for these logs:
Security Dashboard initialized
Environment: {production: false, apiUrl: "http://localhost:80/api"}
API URL: http://localhost:80/api
Loading security data from: http://localhost:80/api/security/dashboard
```

### **Step 2: Check Network Tab**
- Open Developer Tools → Network tab
- Refresh Security Dashboard
- Look for failed requests to `/api/...`
- Verify requests are going to `localhost:8080`

### **Step 3: Test Individual Endpoints**
```bash
# Test security dashboard endpoint
curl http://localhost:80/api/security/dashboard

# Test system metrics endpoint
curl http://localhost:80/api/system/metrics

# Test user stats endpoint
curl http://localhost:80/api/users/stats
```

## 🚀 **Quick Fixes**

### **Fix 1: Restart Backend**
```bash
# Stop Spring Boot application
# Start it again
./mvnw spring-boot:run
```

### **Fix 2: Clear Browser Cache**
- Hard refresh: `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)
- Clear browser cache and cookies
- Try incognito/private browsing mode

### **Fix 3: Check Angular Dev Server**
```bash
# Stop Angular dev server
# Start it again
ng serve
```

### **Fix 4: Verify Port Configuration**
```bash
# Check if port 8080 is available
netstat -an | grep 8080

# Check if Spring Boot is using correct port
# In application.properties:
server.port=8080
```

## 📋 **Common Issues & Solutions**

### **Issue 1: CORS Error**
```
Access to XMLHttpRequest at 'http://localhost:80/api/...' 
from origin 'http://localhost:4200' has been blocked by CORS policy
```
**Solution**: Add CORS configuration to Spring Boot backend

### **Issue 2: Backend Not Running**
```
Failed to connect to localhost:8080
```
**Solution**: Start Spring Boot backend application

### **Issue 3: Wrong Port**
```
Connection refused on port 8080
```
**Solution**: Check if backend is running on different port

### **Issue 4: Environment Not Loading**
```
environment is undefined
```
**Solution**: Check Angular build configuration and environment files

## 🔍 **Advanced Debugging**

### **Enable Angular Debug Mode**
```typescript
// In component
console.log('Environment loaded:', environment);
console.log('API URL resolved:', environment?.apiUrl);
console.log('Current location:', window.location.href);
```

### **Check Angular Build Output**
```bash
# Build with verbose logging
ng build --verbose

# Check if environment files are included
ls -la dist/microsponsoring-frontend/
```

### **Verify Environment File Replacement**
```json
// angular.json
"fileReplacements": [
  {
    "replace": "src/environments/environment.ts",
    "with": "src/environments/environment.pod.ts"
  }
]
```

## 📞 **Getting Help**

### **1. Check Console Logs**
- Open browser Developer Tools
- Look for error messages
- Check Network tab for failed requests

### **2. Verify Backend Status**
```bash
# Test backend health
curl http://localhost:80/actuator/health
```

### **3. Test API Endpoints**
```bash
# Test each endpoint individually
curl http://localhost:80/api/security/dashboard
curl http://localhost:80/api/system/metrics
curl http://localhost:80/api/users/stats
```

### **4. Check Environment Configuration**
- Verify `environment.ts` has correct `apiUrl`
- Ensure backend is running on port 8080
- Check CORS configuration

## ✅ **Success Indicators**

When everything is working correctly, you should see:

1. **Console Logs**:
   ```
   Security Dashboard initialized
   Environment: {production: false, apiUrl: "http://localhost:80/api"}
   Loading security data from: http://localhost:80/api/security/dashboard
   ```

2. **Network Requests**:
   - Successful requests to `localhost:8080`
   - No CORS errors
   - Proper response data

3. **Dashboard Display**:
   - Build information shows correctly
   - Sample data loads (when backend is unavailable)
   - Connection status indicator shows backend URL

4. **No Errors**:
   - No console errors
   - No network request failures
   - Dashboard loads with sample data

## 🎯 **Next Steps**

1. **Verify Backend**: Ensure Spring Boot is running on port 8080
2. **Test Connection**: Use "Test Connection" button in dashboard
3. **Check Console**: Look for environment and API URL logs
4. **Verify CORS**: Ensure backend allows requests from Angular dev server
5. **Test Endpoints**: Verify each API endpoint responds correctly

If issues persist, check the backend logs and ensure all required endpoints are implemented.





