package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URL;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SessionDto {

    private URL addressOfRemoteServer;
    private String sessionId;
    private Map<String, Object> sessionCapabilities;

}
