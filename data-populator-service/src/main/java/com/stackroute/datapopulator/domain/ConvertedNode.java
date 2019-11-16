package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertedNode {
    private String uuid;
    private List<String> type;
    private Map<String, String> properties;
}
