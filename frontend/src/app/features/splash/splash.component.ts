import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-splash',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './splash.component.html',
  styleUrls: ['./splash.component.scss']
})
export class SplashComponent implements OnInit {
  @Output() completed = new EventEmitter<void>();

  ngOnInit(): void {
    // Transition fluide vers l'écran suivant après 1.8s
    setTimeout(() => {
      this.completed.emit();
    }, 1800);
  }

  skip(): void {
    this.completed.emit();
  }
}
