// src/app/components/notification-dropdown/notification-dropdown.component.ts
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Notification } from '../../../models/notification.model';
import { NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';
import { NotificationType } from '../../../models/notification-type.model';

@Component({
  selector: 'app-notification-dropdown',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-dropdown.component.html',
  styleUrls: ['./notification-dropdown.component.css'],
})
export class NotificationDropdownComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  isDropdownOpen = false;
  isConnected = false;
  showNotifications = false;

  currentTab: 'all' | 'unread' = 'all';
  filteredNotifications: Notification[] = [];
  currentPage = 0;
  pageSize = 5;
  loadingMore = false;
  allLoaded = false;

  private subscriptions: Subscription[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.initializeWebSocket();

    // Subscribe to real-time notifications - FIXED METHOD NAME
    this.subscriptions.push(
      this.notificationService.getNotifications().subscribe((notification) => {
        // Add to main list
        this.notifications.unshift(notification);
        this.updateUnreadCount();

        // Update filtered list based on current tab
        if (this.currentTab === 'all') {
          this.filteredNotifications.unshift(notification);
        } else if (this.currentTab === 'unread' && !notification.isRead) {
          this.filteredNotifications.unshift(notification);
        }
      })
    );

    // Subscribe to connection status
    this.subscriptions.push(
      this.notificationService.getConnectionStatus().subscribe((status) => {
        this.isConnected = status;
        if (status) {
          // console.log('WebSocket connected in dropdown');
        }
      })
    );
  }

  private async initializeWebSocket() {
    try {
      // REMOVED user parameter - backend uses JWT
      await this.notificationService.connectWebSocket();
    } catch (error) {
      // console.error('Failed to connect WebSocket in dropdown:', error);
    }
  }

  toggleDropdown(show?: boolean) {
    const wasOpen = this.showNotifications;
    this.showNotifications =
      show !== undefined ? show : !this.showNotifications;

    // Just opened → reset and load first page
    if (!wasOpen && this.showNotifications) {
      this.currentPage = 0;
      this.allLoaded = false;
      this.notifications = [];
      this.filteredNotifications = [];
      this.loadNotifications(this.currentTab === 'unread');
    }

    // Just closed → mark all as read
    if (wasOpen && !this.showNotifications) {
      this.markAllAsRead();
    }
  }

  markAsRead(notificationId: string) {
    const notification = this.notifications.find(
      (n) => n.id === notificationId
    );
    if (notification && !notification.isRead) {
      // Optimistic update
      notification.isRead = true;
      this.updateUnreadCount();

      // Send to backend - FIXED: Only HTTP call needed (WebSocket is for receiving)
      this.notificationService.markAsRead(notificationId).subscribe({
        error: (error) => {
          // Revert optimistic update on error
          if (notification) {
            notification.isRead = false;
            this.updateUnreadCount();
          }
          // console.error('Failed to mark as read:', error);
        },
      });
    }
  }
  markAllAsRead() {
    const unreadNotifications = this.notifications.filter((n) => !n.isRead);
    unreadNotifications.forEach((n) => (n.isRead = true));
    this.updateUnreadCount();
    this.applyFilter();

    this.notificationService.markAllAsRead().subscribe({
      error: () => {
        unreadNotifications.forEach((n) => (n.isRead = false));
        this.updateUnreadCount();
        this.applyFilter();
      },
    });
  }

  deleteNotification(notificationId: string) {
    this.notificationService.deleteNotification(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(
          (n) => n.id !== notificationId
        );
        this.updateUnreadCount();
      },
      error: (error) => {
        // console.error('Failed to delete notification:', error);
      },
    });
  }

  private updateUnreadCount() {
    this.unreadCount = this.notifications.filter((n) => !n.isRead).length;
  }

  reconnectWebSocket() {
    this.initializeWebSocket();
  }

  ngOnDestroy() {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.notificationService.disconnectWebSocket();
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    const target = event.target as HTMLElement;
    const notificationContainer = document.querySelector(
      '.notification-container'
    );

    if (notificationContainer && !notificationContainer.contains(target)) {
      this.toggleDropdown(false);
      this.isDropdownOpen = false;
    }
  }

  // Add this method to test the connection
  testConnection() {
    this.notificationService.testWebSocketConnection();
  }

  switchTab(tab: 'all' | 'unread') {
    if (this.currentTab === tab) return; // do nothing if same tab

    this.currentTab = tab;

    // Reset state
    this.currentPage = 0;
    this.allLoaded = false;
    this.notifications = [];
    this.filteredNotifications = [];

    // Fetch from backend immediately with correct filter
    this.loadNotifications(this.currentTab === 'unread');
  }

  private applyFilter() {
    if (this.currentTab === 'all') {
      this.filteredNotifications = [...this.notifications];
    } else if (this.currentTab === 'unread') {
      this.filteredNotifications = this.notifications.filter((n) => !n.isRead);
    }
  }

  getNotificationIcon(type?: NotificationType): string {
    switch (type) {
      case NotificationType.OUTGOING:
        return 'assets/sponsor-notif-icon.png';
      case NotificationType.INCOMING:
        return 'assets/received-notif-icon.png';
      default:
        return 'assets/notif-icon.png';
    }
  }

  loadNotifications(unread: boolean = false) {
    // console.log('this.allLoaded', this.allLoaded);
    if (this.loadingMore || this.allLoaded) return;

    this.loadingMore = true;
    this.notificationService
      .getUserNotifications(this.currentPage, this.pageSize, unread)
      .subscribe({
        next: (page) => {
          if (page.content.length < this.pageSize) {
            this.allLoaded = true;
          }
          this.notifications.push(...page.content);
          this.applyFilter();
          this.currentPage++;
          this.loadingMore = false;
        },
        error: () => (this.loadingMore = false),
      });
  }

  onScroll(event: any) {
    const element = event.target;
    const threshold = 60; // ≈ one item height + margin (50px + 10px buffer)

    if (
      element.scrollHeight - element.scrollTop - element.clientHeight <=
      threshold
    ) {
      // console.log('Scrolled to bottom, loading more...');
      this.loadNotifications(this.currentTab === 'unread');
    }
  }
}
