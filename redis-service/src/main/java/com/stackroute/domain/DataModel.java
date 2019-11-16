package com.stackroute.domain;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@RedisHash("DataModel")
public class DataModel implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    @Id
    private String key;
    private List<String> value;


}
