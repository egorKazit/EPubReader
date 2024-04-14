package com.yk.common.model.remote;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Book {

    private final String id;
    private String name;
    private String author;
    private String genre;
    private String annotation;
    private String url;

}
