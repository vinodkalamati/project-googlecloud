package com.stackroute.datapopulator.domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private String uuid;
    private String type;
    private Map<String, String> properties;
}
