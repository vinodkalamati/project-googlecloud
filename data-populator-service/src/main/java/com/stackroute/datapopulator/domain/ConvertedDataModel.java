package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertedDataModel {
    private String userid;
    private String query;
    private String domain;
    private List<ConvertedNode> nodes;
    private List<Relationship> relationship;
}
