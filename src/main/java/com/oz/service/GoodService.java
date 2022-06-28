package com.oz.service;

import java.io.IOException;

public interface GoodService {
    Boolean buyGood(String goodId, String username, String password) throws IOException, InterruptedException;
}
