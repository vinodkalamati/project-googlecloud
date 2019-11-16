package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalNlpResultModel {
    private String userid;
    private String query;
    private String domain;
    private String disease;
    private List<String> symptoms=new ArrayList<>();
    private List<String> medicine=new ArrayList<>();
    private List<String> causes=new ArrayList<>();
    private List<String> deathCount=new ArrayList<>();
    private List<String> treatment=new ArrayList<>();
}
