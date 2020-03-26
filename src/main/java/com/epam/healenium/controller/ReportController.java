package com.epam.healenium.controller;

import com.epam.healenium.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/healenium/report")
@RequiredArgsConstructor
public class ReportController {

    @Value("${app.url.report}")
    private String reportUrl;

    private final ReportService reportService;

    @GetMapping("/{uid}")
    public ModelAndView get(@PathVariable String uid) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate(uid));
        return modelAndView;
    }

    @PostMapping("/init")
    public String init() {
        return reportService.initialize();
    }

    @PostMapping("/build")
    public String build(@RequestHeader("sessionKey") String key) {
        return Paths.get(reportUrl, key).toString();
    }

}