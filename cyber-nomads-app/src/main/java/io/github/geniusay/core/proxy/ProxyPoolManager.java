package io.github.geniusay.core.proxy;

import io.github.geniusay.core.proxy.pojo.IpProxy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProxyPoolManager {
    private final CopyOnWriteArrayList<IpProxy> proxyPool;

    private final BalanceStrategy balance;

    public ProxyPoolManager(List<IpProxy> proxies){
        this.proxyPool = new CopyOnWriteArrayList<>(proxies);
        this.balance = new RoundRobinBalance();
    }

    public ProxyPoolManager(List<IpProxy> proxies, BalanceStrategy balance) {
        this.proxyPool = new CopyOnWriteArrayList<>(proxies);
        this.balance = balance;
    }

    public ProxyPoolManager(BalanceStrategy balance) {
        this.proxyPool = new CopyOnWriteArrayList<>();
        this.balance = balance;
    }

    public ProxyPoolManager() {
        proxyPool = new CopyOnWriteArrayList<>();
        this.balance = new RoundRobinBalance();
    }

    public void addProxy(IpProxy proxy) {
        proxyPool.add(proxy);
    }

    public void removeProxy(IpProxy proxy) {
        proxyPool.remove(proxy);
    }

    public void addAll(List<IpProxy> proxies) {
        proxyPool.addAll(proxies);
    }

    public void removeAll(List<IpProxy> proxies) {
        proxyPool.removeAll(proxies);
    }

    public void clear(){
        proxyPool.clear();
    }

    public IpProxy getProxy(){
        if(this.proxyPool.isEmpty()) return null;
        return balance.balance(this.proxyPool);
    }

    // 轮询
    static class RoundRobinBalance implements BalanceStrategy {
        private AtomicInteger index = new AtomicInteger(0);

        @Override
        public IpProxy balance(CopyOnWriteArrayList<IpProxy> proxyPool) {
            int size = proxyPool.size(), newIndex = index.getAndIncrement() % size;
            IpProxy proxy;
            while((proxy = proxyPool.get(newIndex)) == null || !proxy.isValid()){
                size = proxyPool.size();
                newIndex = index.getAndIncrement() % size;
            }
            return proxy;
        }
    }
    // 随机
    static class RandomBalance implements BalanceStrategy {
        @Override
        public IpProxy balance(CopyOnWriteArrayList<IpProxy> proxyPool) {
            return proxyPool.get(ThreadLocalRandom.current().nextInt(proxyPool.size()));
        }
    }
}
