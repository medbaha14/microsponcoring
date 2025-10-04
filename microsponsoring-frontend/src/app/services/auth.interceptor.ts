import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    console.log('AuthInterceptor - Token:', token);
    console.log('AuthInterceptor - Request URL:', req.url);
    
    const isPublicRecognition = req.url.includes(
      '/api/recognition-benefits/company/'
    );
    
    if (token && !isPublicRecognition) {
      const cloned = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('AuthInterceptor - Added Authorization header');
      return next.handle(cloned);
    } else {
      console.log('AuthInterceptor - No token found or public recognition');
      return next.handle(req);
    }
  }
}
