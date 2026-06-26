import { Component } from '@angular/core';
import { TopbarComponent } from "./layouts/topbar/topbar.component";
import { FooterComponent } from "./layouts/footer/footer.component";
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [TopbarComponent, FooterComponent, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'SpecZeta-web';
}
