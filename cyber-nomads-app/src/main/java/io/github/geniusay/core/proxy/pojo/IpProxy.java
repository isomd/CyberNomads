package io.github.geniusay.core.proxy.pojo;

import lombok.Data;
import lombok.Getter;

@Data
public class IpProxy {
    private String ip;

    private Integer port;

    private boolean valid = true;

    public IpProxy(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
