// src/app/services/notification.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { Notification } from '../models/notification.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private stompClient: Client | null = null;
  private isConnected = false;
  private connectionSubject = new BehaviorSubject<boolean>(false);
  private notificationSubject = new Subject<Notification>();
  
  private wsUrl: string = '';
  private apiUrl: string = '';

  constructor(private http: HttpClient) {
    // Initialize URLs based on environment
    this.initializeUrls();
    
    // Auto-connect if user is logged in
    this.initializeConnection();
  }

  private initializeUrls(): void {
    // Use specific notification URLs from environment if available
    if (environment.notificationsUrl) {
      this.apiUrl = environment.notificationsUrl;
    } else {
      // Fallback to constructing from base API URL
      this.apiUrl = environment.apiUrl + '/notifications';
    }
    
    if (environment.wsUrl) {
      this.wsUrl = environment.wsUrl;
    } else {
      // Fallback to constructing from base URL
      const baseUrl = environment.baseUrl ;
      this.wsUrl = baseUrl.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws-notifications';
    }
    
    console.log('NotificationService initialized:');
    console.log('  Environment:', environment.production ? 'Production' : 'Development');
    console.log('  WebSocket URL:', this.wsUrl);
    console.log('  API URL:', this.apiUrl);
  }

  private initializeConnection(): void {
    // Check if user is logged in and auto-connect
    const token = localStorage.getItem('token');
    if (token) {
      console.log('User is logged in, attempting to connect WebSocket...');
      this.connectWebSocket().catch(error => {
        console.log('Initial WebSocket connection failed, will retry:', error);
        // Set up auto-reconnect
        setTimeout(() => this.autoReconnect(), 5000);
      });
    } else {
      console.log('User not logged in, WebSocket connection will be established on login');
    }
  }

  // Public method to connect when user logs in
  connectOnLogin(): Promise<boolean> {
    console.log('User logged in, connecting WebSocket...');
    return this.connectWebSocket();
  }

  // WebSocket Connection Methods
  connectWebSocket(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      console.log('=== WEB SOCKET DEBUG ===');
      if (this.isConnected && this.stompClient?.connected) {
        console.log('WebSocket already connected');
        resolve(true);
        return;
      }

      console.log('Connecting to WebSocket at:', this.wsUrl);

      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No JWT token found');
        reject('No JWT token found');
        return;
      }

      console.log('Using JWT token:', token.substring(0, 20) + '...');

      // Disconnect existing connection if any
      if (this.stompClient) {
        this.stompClient.deactivate();
      }

      // Use raw WebSocket (no SockJS)
      this.stompClient = new Client({
        brokerURL: this.wsUrl,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        
        debug: (str: string) => {
          console.log('STOMP:', str);
        },
        
        onConnect: (frame: any) => {
          console.log('✅ Connected to WebSocket successfully');
          console.log('Connection frame:', frame);
          this.isConnected = true;
          this.connectionSubject.next(true);
          this.subscribeToUserNotifications();
          resolve(true);
        },
        
        onStompError: (frame: any) => {
          console.error('❌ STOMP error frame:', frame);
          this.isConnected = false;
          this.connectionSubject.next(false);
          reject(frame);
        },
        
        onWebSocketError: (error: any) => {
          console.error('❌ WebSocket connection error:', error);
          this.isConnected = false;
          this.connectionSubject.next(false);
          reject(error);
        },
        
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.isConnected = false;
          this.connectionSubject.next(false);
        }
      });

      this.stompClient.activate();
    });
  }

  private subscribeToUserNotifications(): void {
    if (!this.stompClient) {
      console.error('STOMP client not initialized');
      return;
    }

    // User-specific notifications
    const userDestination = `/user/queue/notifications`;
    
    this.stompClient.subscribe(userDestination, (message: any) => {
      try {
        const notification: Notification = JSON.parse(message.body);
        console.log('📨 Received user notification:', notification);
        this.notificationSubject.next(notification);
      } catch (error) {
        console.error('Error parsing user notification:', error);
      }
    });

    console.log(`✅ Subscribed to user notifications: ${userDestination}`);
  }

  disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate().then(() => {
        console.log('WebSocket disconnected');
        this.stompClient = null;
        this.isConnected = false;
        this.connectionSubject.next(false);
      });
    }
  }

  // Auto-reconnect method
  private autoReconnect(): void {
    if (!this.isConnected) {
      console.log('Attempting to reconnect WebSocket...');
      this.connectWebSocket().catch(error => {
        console.error('Reconnection failed:', error);
        // Retry after 10 seconds
        setTimeout(() => this.autoReconnect(), 10000);
      });
    }
  }

  // Real-time Observables
  getNotifications(): Observable<Notification> {
    return this.notificationSubject.asObservable();
  }

  getConnectionStatus(): Observable<boolean> {
    return this.connectionSubject.asObservable();
  }

  isWebSocketConnected(): boolean {
    return this.isConnected && this.stompClient?.connected === true;
  }

  // HTTP API Methods (Backend uses JWT from headers)
  getUserNotifications(
    page: number = 0,
    size: number = 5,
    unread: boolean = false
  ): Observable<{ content: Notification[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('unread', unread.toString());

    return this.http.get<{ content: Notification[]; totalElements: number; totalPages: number }>(
      `${this.apiUrl}/my-notifications`,
      { params }
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/my-unread-count`);
  }

  markAsRead(notificationId: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mark-all-read`, {});
  }

  deleteNotification(notificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }

  createNotification(notification: Partial<Notification>): Observable<Notification> {
    return this.http.post<Notification>(this.apiUrl, notification);
  }

  // Test method for basic WebSocket connection
  testWebSocketConnection(): void {
    console.log('Testing basic WebSocket connection...');
    console.log('WebSocket URL:', this.wsUrl);
    
    const testSocket = new WebSocket(this.wsUrl);
    
    testSocket.onopen = () => {
      console.log('Basic WebSocket connection successful!');
      testSocket.close();
    };
    
    testSocket.onerror = (error) => {
      console.error('Basic WebSocket connection failed:', error);
    };
    
    testSocket.onclose = (event) => {
      console.log('WebSocket closed:', event.code, event.reason);
    };

    // Timeout after 5 seconds
    setTimeout(() => {
      if (testSocket.readyState !== WebSocket.OPEN) {
        console.error('WebSocket connection timeout');
        testSocket.close();
      }
    }, 5000);
  }

  // Test method for backend API connectivity
  testBackendConnectivity(): void {
    console.log('Testing backend API connectivity...');
    console.log('API URL:', this.apiUrl);
    
    const healthUrl = environment.baseUrl + '/actuator/health';
    this.http.get(healthUrl).subscribe({
      next: (response) => {
        console.log('Backend API is reachable:', response);
      },
      error: (error) => {
        console.error('Backend API is not reachable:', error);
      }
    });
  }

  // WebSocket Real-time Methods (for sending messages to backend)
  sendRealTimeNotification(notification: Partial<Notification>): void {
    if (!this.stompClient || !this.isConnected) {
      // console.error('WebSocket not connected');
      return;
    }

    this.stompClient.publish({
      destination: '/app/notification',
      body: JSON.stringify(notification)
    });

    // console.log('Sent real-time notification:', notification);
  }

  markAsReadRealTime(notificationId: string): void {
    if (!this.stompClient || !this.isConnected) {
      // console.error('WebSocket not connected');
      return;
    }

    this.stompClient.publish({
      destination: '/app/notification/mark-read',
      body: JSON.stringify({ notificationId })
    });

    // console.log('Sent mark-as-read for notification:', notificationId);
  }

  // Utility method to check connection status
  getConnectionInfo(): string {
    if (!this.stompClient) {
      return 'Not initialized';
    }
    
    if (this.stompClient.connected) {
      return `Connected (Active: ${this.isConnected})`;
    } else {
      return 'Disconnected';
    }
  }
}