import {Component, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {MatCard, MatCardContent} from "@angular/material/card";
import {MatButton} from "@angular/material/button";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AsyncPipe} from "@angular/common";

interface Entry {
  id: number, access: string
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatCard, MatCardContent, MatButton, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  private host!: string;

  public entries$!: Observable<Entry[]>;

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.host = window.location.protocol + "//" + window.location.host;
    // this.host = "https://d1kri7a42xlln6.cloudfront.net";
    this.entries$ = this.http.get<Entry[]>(this.host + "/visit");
  }

  onClick(): void {
    this.http.post<void>(this.host + "/visit", {}).subscribe(() => {
      this.entries$ = this.http.get<Entry[]>(this.host + "/visit"); //reinitializeaza lista (refresh)
    });
  }

}
