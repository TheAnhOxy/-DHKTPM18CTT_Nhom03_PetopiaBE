package com.pet.controller.web;

import com.pet.service.ServiceManagement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceManagement serviceManagement;
    @GetMapping
    public Object getAllServices() {
        return serviceManagement.getAllServices("", 0, 100);
    }
//    Get by id
    @GetMapping("/{id}")
    public Object getServiceById(@PathVariable String id) {
        return serviceManagement.getServiceById(id);
    }
}
