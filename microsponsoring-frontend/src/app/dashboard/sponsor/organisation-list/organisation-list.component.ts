import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { User } from '../../../models/user.model';
import { CommonModule } from '@angular/common';
import { OrganisationCardComponent } from '../organisation-card/organisation-card.component';

@Component({
  selector: 'app-organisation-list',
  standalone: true,
  imports: [CommonModule, OrganisationCardComponent],
  templateUrl: './organisation-list.component.html',
  styleUrl: './organisation-list.component.css'
})
export class OrganisationListComponent implements OnInit {
  users: User[] = [];

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.userService.getAllByRole('organisation_nonprofit').subscribe(users => this.users = users);
  }
}
