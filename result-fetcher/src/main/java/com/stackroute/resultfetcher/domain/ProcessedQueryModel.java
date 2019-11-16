package com.stackroute.resultfetcher.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedQueryModel {
    private String query;
    private String domain;
    private List<String> queryresult;
    private List<Map> constraints;
}
