import { Component, Inject } from '@angular/core';
import { PingService } from './ping.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  progress: number | null = null;
  result: any = null;
  resultEntries: { name: string, status: string }[] = [];
  taskId: string | null = null;
  loading = false;
  error: string | null = null;

  constructor(@Inject(PingService) private pingService: PingService) {}

  start() {
    this.progress = 0;
    this.result = null;
    this.resultEntries = [];
    this.error = null;
    this.loading = true;
    this.pingService.startPingMission().subscribe({
      next: (res: { taskId: string | null; }) => {
        this.taskId = res.taskId;
        this.pollProgress();
      },
      error: () => {
        this.error = 'Failed to start ping mission.';
        this.loading = false;
      }
    });
  }

  pollProgress() {
    if (!this.taskId) return;
    this.pingService.getProgress(this.taskId).subscribe({
      next: (progress: number | null) => {
        this.progress = progress;
        if (progress !== null && progress < 100) {
          setTimeout(() => this.pollProgress(), 1000);
        } else if (progress !== null && progress >= 100) {
          this.fetchResult();
        }
      },
      error: () => {
        this.error = 'Failed to get progress.';
        this.loading = false;
      }
    });
  }

  fetchResult() {
    if (!this.taskId) return;
    this.pingService.getResult(this.taskId).subscribe({
      next: (res: any) => {
        this.result = res;
        this.resultEntries = this.formatResult(res);
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to fetch results.';
        this.loading = false;
      }
    });
  }

  formatResult(res: any): { name: string, status: string }[] {
    if (!res) return [];
    // If the backend returns { name: string: status: string }, just map directly
    return Object.entries(res).map(([name, status]: [string, any]) => ({
      name,
      status: typeof status === 'string' ? status : JSON.stringify(status)
    }));
  }
}