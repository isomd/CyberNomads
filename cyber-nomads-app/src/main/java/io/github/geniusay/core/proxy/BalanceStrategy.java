package io.github.geniusay.core.proxy;

import io.github.geniusay.core.proxy.pojo.IpProxy;

import java.util.concurrent.CopyOnWriteArrayList;

public interface BalanceStrategy {
    IpProxy balance(CopyOnWriteArrayList<IpProxy> proxyPool);
}
