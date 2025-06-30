import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-add-user-modal',
  templateUrl: './add-user-modal.component.html',
  styleUrls: ['./add-user-modal.component.css']
})
export class AddUserModalComponent implements OnChanges {
  @Input() user: User | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() userAdded = new EventEmitter<void>();

  newUser: any = {};
  uploadingImage = false;

  get profilePictureUrl(): string {
    if (!this.newUser.profilePicture) {
      return '';
    }
    if (this.newUser.profilePicture.startsWith('http')) {
      return this.newUser.profilePicture;
    }
    return 'http://localhost:8080' + this.newUser.profilePicture;
  }

  constructor(private userService: UserService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] && this.user) {
      this.newUser = { ...this.user };
      // Don't prefill password for edit
      this.newUser.password = '';
    } else if (!this.user) {
      this.newUser = {};
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      if (!this.user || !this.user.userId) {
        alert('User must be created or selected before uploading a profile picture.');
        return;
      }
      this.uploadingImage = true;
      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', this.user.userId);
      fetch('http://localhost:8080/api/upload/profile-picture', {
        method: 'POST',
        body: formData
      })
      .then(res => res.text())
      .then(data => {
        this.newUser.profilePicture = 'http://localhost:8080' + data;
        this.uploadingImage = false;
      })
      .catch(() => {
        alert('Image upload failed');
        this.uploadingImage = false;
      });
    }
  }

  submitAddUser() {
    if (this.user && this.user.userId) {
      // Edit mode
      const updatedUser = { ...this.newUser };
      if (!updatedUser.password) delete updatedUser.password; // Don't send empty password
      this.userService.update(this.user.userId, updatedUser).subscribe({
        next: () => {
          this.userAdded.emit();
        },
        error: err => {
          alert('Failed to update user: ' + (err.error || err.message));
        }
      });
    } else {
      // Add mode
      this.userService.create(this.newUser).subscribe({
        next: () => {
          this.userAdded.emit();
        },
        error: err => {
          alert('Failed to add user: ' + (err.error || err.message));
        }
      });
    }
  }
} 