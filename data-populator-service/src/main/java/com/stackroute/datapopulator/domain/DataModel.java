package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataModel {
    private String userid;
    private String query;
    private List<Node> nodes;
    private String domain;
    private List<Relationship> relationship;
}
