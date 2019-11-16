import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import {HttpClientModule} from '@angular/common/http';
import {PapaParseModule} from 'ngx-papaparse';

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { FormsModule } from "@angular/forms";
import { ExpertLoginComponent } from './expert-login/expert-login.component';
import { ExpertRegisterComponent } from './expert-register/expert-register.component';
import { ExpertDashboardComponent } from './expert-dashboard/expert-dashboard.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MovieDomainSearchComponent } from './movie-domain-search/movie-domain-search.component';
import { MedicalDomainSearchComponent } from './medical-domain-search/medical-domain-search.component';
import { SearchResultComponent } from './search-result/search-result.component';
import { WebSocketService } from './websocket-service/websocket.service';
import { ExpertAnalyticsComponent } from './expert-analytics/expert-analytics.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ExpertAnalyticsMovieComponent } from './expert-analytics-movie/expert-analytics-movie.component';
import { ExpertAnalyticsMedicalComponent } from './expert-analytics-medical/expert-analytics-medical.component';
import { ExpertAnalyticsGraphComponent } from './expert-analytics-graph/expert-analytics-graph.component';
import { ExpertValidateDashboardComponent, DialogOverviewExampleDialog } from './expert-validate-dashboard/expert-validate-dashboard.component';
import {MatDialogModule} from '@angular/material/dialog';
import { MatFormFieldModule, MatInputModule,MatIconModule} from '@angular/material';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@NgModule({
  declarations: [AppComponent, ExpertValidateDashboardComponent, DialogOverviewExampleDialog, ExpertLoginComponent, ExpertRegisterComponent, ExpertDashboardComponent, DashboardComponent, MovieDomainSearchComponent, MedicalDomainSearchComponent, SearchResultComponent, ExpertAnalyticsComponent, ExpertAnalyticsMovieComponent, ExpertAnalyticsMedicalComponent, ExpertAnalyticsGraphComponent],
  imports: [BrowserModule, MatProgressSpinnerModule, AppRoutingModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatIconModule, FormsModule, HttpClientModule, PapaParseModule, BrowserAnimationsModule, MatTableModule, MatButtonModule, MatCardModule],
  providers: [WebSocketService],
  bootstrap: [AppComponent],
  entryComponents: [DialogOverviewExampleDialog]
})
export class AppModule {}
