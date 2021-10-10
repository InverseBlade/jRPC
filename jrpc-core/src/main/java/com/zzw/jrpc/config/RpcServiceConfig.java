package com.zzw.jrpc.config;

import com.zzw.jrpc.base.utils.Tools;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RpcServiceConfig {

    String version;

    String group;

    Object service;

    public String getServiceName() {
        return Tools.genRpcServiceName(getInterfaceName(), version, group);
    }

    public String getInterfaceName() {
        return service.getClass().getInterfaces()[0].getCanonicalName().replace("/", "");
    }

}
