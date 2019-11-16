import { Component, OnInit } from '@angular/core';
import {MovieSearchService} from '../movie_search_service/movie-search.service';
import { Router } from '@angular/router';
import {WebSocketService} from '../websocket-service/websocket.service';

@Component({
  selector: 'app-movie-domain-search',
  templateUrl: './movie-domain-search.component.html',
  styleUrls: ['./movie-domain-search.component.css']
})
export class MovieDomainSearchComponent implements OnInit {

  constructor( private movieSearchService: MovieSearchService, private route:Router,private webSocketService: WebSocketService) { }
  notifications:any;
  results:String;
  ngOnInit() {
    localStorage.clear();
    let stompClient =this.webSocketService.connect();
    stompClient.connect({},frame =>{
                                 stompClient.subscribe('/topic/notification',notifications=>{
                                   this.notifications=JSON.parse(notifications.body);
                                   localStorage.setItem('query',this.notifications.query);
                                   localStorage.setItem('status',this.notifications.status);
                                   localStorage.setItem('result',this.notifications.result.join(':'));
                                   if(this.notifications.suggestions!=null){
                                    localStorage.setItem('suggestion',this.notifications.suggestions.join(':'));
                                   }
                                   this.results="results";
                                   this.route.navigateByUrl('/search-result');
                                 })
                               });
  }
  flag:boolean=false;
  userSearch(searchQuery){
    console.log(searchQuery);
    this.flag=true;
    localStorage.setItem('domain', "movie");
    this.movieSearchService.userSearchService(searchQuery)
     .subscribe(data=>{
      console.log(data);
      // let stompClient =this.webSocketService._connect();
      // stompClient._connect({},frame =>{
      //   stompClient.subscribe('/topic/notification',notifications=>{
      //     this.notifications=JSON.parse(notifications.body);
      //     localStorage.setItem('result', this.notifications.join(':'));
      //     console.log(this.notifications);
      //     this.results="results";
      //     this.route.navigateByUrl('/search-result');
      //   })
      // });

     },error=>{
       console.log(error);
       this.route.navigateByUrl('/movie-domain');
     });


  }


}
