package io.github.geniusay.core.proxy;

import io.github.geniusay.core.proxy.pojo.IpProxy;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class ProxyRequest {
    @Resource
    private ProxyPoolManager proxyPoolManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);

    private final Headers EMPTY_HEADERS = Headers.of();

    public String get(String url){
        return get(url, EMPTY_HEADERS);
    }

    public String get(String url, Headers headers){
        return get(url, headers, Collections.emptyMap());
    }

    public String get(String url, Map<String, String> params){
        return get(url, EMPTY_HEADERS, params);
    }

    public String get(String url, Headers headers, Map<String, String> params){
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();

        if(params != null && !params.isEmpty()){
            for(Map.Entry<String, String> entry : params.entrySet()){
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return get(urlBuilder.build(), headers);
    }

    public String post(String url){
        return post(url,EMPTY_HEADERS, EMPTY_BODY);
    }

    public String post(String url, String requestBody){
        return post(url, EMPTY_HEADERS, requestBody);
    }

    public String post(String url, Headers headers){
        return post(url, headers, EMPTY_BODY);
    }

    public String post(String url, Headers headers, String requestBody){
        return post(url, headers, RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody));
    }

    public String get(HttpUrl url, Headers headers){
        Callable<String> callable = () -> {
            OkHttpClient client = getOkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Future<String> future = threadPoolExecutor.submit(callable);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String url, Headers headers, RequestBody requestBody){
        OkHttpClient client = getOkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private OkHttpClient getOkHttpClient(){
        IpProxy ipProxy = proxyPoolManager.getProxy();
        if(ipProxy == null){
            throw new RuntimeException("No proxy found");
        }
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(ipProxy.getIp(), ipProxy.getPort())
        );
        return new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
    }
}
