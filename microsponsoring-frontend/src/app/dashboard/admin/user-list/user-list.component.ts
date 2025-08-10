import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { User } from '../../../models/user.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  selectedUser: User | null = null;
  showDetailsModal = false;
  showAddUserModal = false;
  selectedEditUser: User | null = null;
  detailsModeSponsor: 'view' | 'edit' | 'add' = 'view';
  detailsModeOrganisation: 'view' | 'edit' | 'add' = 'view';
  showSponsorModal = false;
  showOrganisationModal = false;
  selectedSponsor: any = null;
  selectedOrganisation: any = null;

  // Pagination properties
  currentPage: number = 1;
  pageSize: number = 10;
  totalPages: number = 1;
  allUsers: User[] = [];

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getAll().subscribe({
      next: (users: User[]) => {
        this.allUsers = users;
        this.updatePagination();
      },
      error: (error: any) => {
        console.error('Error loading users:', error);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'Failed to load users. Please try again.'
        });
      }
    });
  }

  updatePagination() {
    this.totalPages = Math.ceil(this.allUsers.length / this.pageSize);
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.users = this.allUsers.slice(startIndex, endIndex);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  blockUser(user: User) {
    if (user.userId) {
      this.userService.block(user.userId).subscribe(() => {
        this.loadUsers();
      });
    }
  }

  editUser(user: User) {
    this.selectedEditUser = user;
    this.showAddUserModal = true;
  }

  addUser() {
    this.showAddUserModal = true;
  }

  closeAddUserModal() {
    this.showAddUserModal = false;
    this.selectedEditUser = null;
  }

  onUserAdded() {
    this.closeAddUserModal();
    this.loadUsers();
  }

  showDetails(user: User) {
    this.selectedUser = user;
    this.showDetailsModal = true;
  }

  closeDetails() {
    this.selectedUser = null;
    this.showDetailsModal = false;
  }

  confirmBlock(user: User) {
    Swal.fire({
      title: 'Are you sure?',
      text: `Block user ${user.username}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, block',
    }).then((result: any) => {
      if (result.isConfirmed && user.userId) {
        this.userService.block(user.userId).subscribe(() => {
          this.loadUsers();
        });
      }
    });
  }

  confirmDeblock(user: User) {
    Swal.fire({
      title: 'Are you sure?',
      text: `Unblock user ${user.username}?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, unblock',
    }).then((result: any) => {
      if (result.isConfirmed && user.userId) {
        this.userService.deblock(user.userId).subscribe(() => {
          this.loadUsers();
        });
      }
    });
  }

  deleteUser(user: User) {
    Swal.fire({
      title: 'Are you sure?',
      text: `Delete user ${user.username}? This action cannot be undone.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete',
      confirmButtonColor: '#d33',
    }).then((result: any) => {
      if (result.isConfirmed && user.userId) {
        this.userService.delete(user.userId).subscribe(() => {
          this.loadUsers();
          Swal.fire('Deleted!', 'User has been deleted.', 'success');
        });
      }
    });
  }

  openSponsorModal(mode: 'view' | 'edit' | 'add', sponsor?: any , user?:User) {
    this.detailsModeSponsor = mode;
    this.selectedSponsor = sponsor || null;
    this.showSponsorModal = true;
    console.log("user",user);
    
    this.selectedEditUser = user?? null;
  }
  openOrganisationModal(mode: 'view' | 'edit' | 'add', organisation?: any , user?:User) {
    this.detailsModeOrganisation = mode;
    this.selectedOrganisation = organisation || null;
    this.showOrganisationModal = true;
    this.selectedEditUser = user?? null;
  }
  closeSponsorModal() {
    this.showSponsorModal = false;
    this.selectedSponsor = null;
  }
  closeOrganisationModal() {
    this.showOrganisationModal = false;
    this.selectedOrganisation = null;
  }
}
