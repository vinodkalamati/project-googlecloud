package com.stackroute.resultfetcher.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultModel {
    private String[] result;
    private String query;
    private String status;
    private String[] suggestions;
}
