import { Component, OnInit } from '@angular/core';
import { AnalyticsService } from '../analytics-service/analytics.service';


@Component({
  selector: 'app-expert-analytics',
  templateUrl: './expert-analytics.component.html',
  styleUrls: ['./expert-analytics.component.css']
})
export class ExpertAnalyticsComponent implements OnInit {

  public responses =  [];
  public errorMsg;
  public displayedColumns: string[] = ['domain','query','result','posResponse','negResponse'];
  constructor(private _analytics: AnalyticsService) { }

  ngOnInit() {
    this._analytics.changeURL("http://34.93.245.170:8099/api/v1/display");
    this._analytics.getResponses()
        .subscribe(data => this.responses=data,
                   error => this.errorMsg = error);
  }
}
