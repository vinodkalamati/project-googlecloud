package com.stackroute.datapopulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {
    private String uuid;
    private String relation;
    private String sourcenode;
    private String destnode;
    private Map<String, String> properties;
}
