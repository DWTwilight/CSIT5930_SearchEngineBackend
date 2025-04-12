package com.hkust.csit5930.searchengine.response;

public record ResponseWrapper<T, U>(T data, U meta) {
}
