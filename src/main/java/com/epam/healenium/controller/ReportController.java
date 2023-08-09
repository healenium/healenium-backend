package com.epam.healenium.controller;

import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping()
    public ModelAndView get() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate());
        return modelAndView;
    }

    @PostMapping("/init")
    public String init() {
        return reportService.initialize();
    }

    @PostMapping("/init/{uid}")
    public String initById(@PathVariable String uid) {
        String key = reportService.initialize(uid);
        return Paths.get(reportUrl, key).toString();
    }

    @PostMapping("/build")
    public String build(@RequestHeader("sessionKey") String key) {
        return Paths.get(reportUrl, key).toString();
    }

    @GetMapping("/data/{uid}")
    public RecordDto getRecord(@PathVariable String uid) {
        return reportService.generate(uid);
    }

    @GetMapping("/data")
    public RecordDto getRecords() {
        return reportService.generate();
    }
}