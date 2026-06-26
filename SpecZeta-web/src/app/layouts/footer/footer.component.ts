import { Component } from '@angular/core';
import { LoginComponent } from "../../pages/auth/login/login.component";

@Component({
  selector: 'app-footer',
  imports: [LoginComponent],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent {

}
