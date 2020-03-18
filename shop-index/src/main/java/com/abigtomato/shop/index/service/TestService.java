package com.abigtomato.shop.index.service;

public interface TestService {

    String testLock();

    String testLockV2();

    String testRead();

    String testWrite();

    String testLatch() throws InterruptedException;

    String testCountdown();
}
