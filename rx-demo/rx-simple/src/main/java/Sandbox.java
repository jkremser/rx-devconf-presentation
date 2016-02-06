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

import java.util.Random;
import java.util.stream.Stream;

import rx.Observable;

/**
 * @author Jirka Kremser
 */
public class Sandbox {

    public static void main(String[] args) {
        Stream.of(3, 1, 4, 1)
                .flatMap(x -> Stream.of(x, x))
                .map(x -> x * 2)
                .filter(x -> x < new Random().nextInt(9)).forEach(System.out::println);
        Observable.just(3, 1, 4, 1)
                .flatMap(x -> Observable.just(x, x))
                .map(x -> x * 2)
                .filter(x -> x < new Random().nextInt(9)).forEach(System.out::println);
//        Observable.interval(1, TimeUnit.SECONDS)
//                .subscribe(System.out::println);
    }
}