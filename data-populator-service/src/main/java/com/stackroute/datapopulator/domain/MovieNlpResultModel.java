package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieNlpResultModel {
    private String userid;
    private String query;
    private String domain;
    private String movieName;
    private List<String> casts=new ArrayList<>();
    private List<String> producers= new ArrayList<>();
    private List<String> directors= new ArrayList<>();
    private List<String> collection= new ArrayList<>();
    private List<String> releaseDate= new ArrayList<>();
    private List<String> productionHouse= new ArrayList<>();
    private List<String> country= new ArrayList<>();
}
