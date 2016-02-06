/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.URLEncoder;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;

/**
 * @author Jirka Kremser
 */
public class StockFetcher {

    private static final String URL = "https://finance.yahoo.com/webservice/v1/symbols/%s/quote?format=json";
    private static final OkHttpClient client = new OkHttpClient();


    public static StockValue fetch(String code) {
        try {
            Request request = new Request.Builder()
                    .url(String.format(URL, URLEncoder.encode(code, "UTF-8")))
                    .addHeader("Connection", "Close")
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonData);
            double price = jsonNode.get("list").get("resources").get(0).get("resource").get("fields").get("price")
                    .asDouble();
            response.body().close();
            return new StockValue(code, price);
        } catch (IOException e) {
            // cannot happen
            System.out.println("error");
        }

        return null;
    }

    public static Observable<StockValue> fetchAsync(String code) {
        try {
            Request request = new Request.Builder()
                    .url(String.format(URL, URLEncoder.encode(code, "UTF-8")))
                    .addHeader("Connection", "Close")
                    .get()
                    .build();

            return Observable.create(subscriber -> {
                client.newCall(request).enqueue(new Callback() {
                    @Override public void onResponse(Call call, Response response) throws IOException {
                        String jsonData = response.body().string();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(jsonData);
                        double price =
                                jsonNode.get("list").get("resources").get(0).get("resource").get("fields").get("price")
                                        .asDouble();
                        subscriber.onNext(new StockValue(code, price));
                        response.body().close();
                    }

                    @Override public void onFailure(Call call, IOException e) {
                        subscriber.onError(e);
                    }
                });
            });

        } catch (IOException e) {
            // cannot happen
            System.out.println("error");
        }

        return null;
    }

    private static void simulation1() {
        Observable<StockValue> observable = Observable.create(subscriber -> {
                    while (true) {
                        Stream.of("RHT", "GOOG", "IBM", "ORCL", "VMW", "AAPL")
                                .map(StockFetcher::fetch)
                                .forEach(subscriber::onNext);
                    }
                }
        );

        observable.subscribe(System.out::println);
    }

    private static void simulation2() {
        Observable<StockValue> observable = Observable.create(subscriber -> {
                    while (true) {
                        Stream.of("RHT", "GOOG", "IBM", "ORCL", "VMW", "AAPL")
                                .map(StockFetcher::fetchAsync)
                                .reduce(Observable::merge)
                                .get()
                                .forEach(subscriber::onNext);
                    }
                }

        );

        observable.subscribe(System.out::println);
    }

    public static void main(String[] args) {
        simulation2();
    }
}
