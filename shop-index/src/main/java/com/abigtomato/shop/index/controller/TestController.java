package com.abigtomato.shop.index.controller;

import com.abigtomato.shop.index.service.TestService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
@Slf4j
@Api(tags = "redisson测试")
public class TestController {

    private TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping(value = "/test/lock")
    public String testLock() {
        return this.testService.testLock();
    }

    @GetMapping(value = "/v2/test/lock")
    public String testLockV2() {
        return this.testService.testLockV2();
    }

    @GetMapping(value = "/test/read")
    public String testRead() {
        return this.testService.testRead();
    }

    @GetMapping(value = "/test/write")
    public String testWrite() {
        return this.testService.testWrite();
    }

    @GetMapping(value = "/test/latch")
    public String testLatch() throws InterruptedException {
        return this.testService.testLatch();
    }

    @GetMapping(value = "/test/countdown")
    public String testCountdown() {
        return this.testService.testCountdown();
    }
}
