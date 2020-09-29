package com.nizkiyd.receiver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

@RestController
class ServiceDiscoveryController{

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/services")
    public Optional<URI> serviceURL(){
        return discoveryClient.getInstances("backend-example")
                .stream()
                .map(instance -> instance.getUri())
                .findFirst();
    }
}