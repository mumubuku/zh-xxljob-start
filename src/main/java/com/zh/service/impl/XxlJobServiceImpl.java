package com.zh.service.impl;


import com.zh.service.XxlJobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XxlJobServiceImpl implements XxlJobService {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddress;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("XXL-JOB-ACCESS-TOKEN", accessToken);
        return headers;
    }

    @Override
    public boolean addJob(Map<String, Object> jobConfig) {
        String url = adminAddress + "/jobinfo/add";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobConfig, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean updateJob(Map<String, Object> jobConfig) {
        String url = adminAddress + "/jobinfo/update";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobConfig, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean removeJob(int jobId) {
        String url = adminAddress + "/jobinfo/remove";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean startJob(int jobId) {
        String url = adminAddress + "/jobinfo/start";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean stopJob(int jobId) {
        String url = adminAddress + "/jobinfo/stop";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean triggerJob(int jobId, String executorParam) {
        String url = adminAddress + "/jobinfo/trigger";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        requestBody.put("executorParam", executorParam);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public Map<String, Object> getJobList(int start, int length) {
        String url = adminAddress + "/jobinfo/pageList";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("start", start);
        requestBody.put("length", length);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }



    @Override
    public boolean checkJobExists(String jobDesc, String executorHandler) {
        Map<String, Object> jobList = getJobList(0, 100);  // 假设最多检查前100个任务
        if (jobList == null) return false;

        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobList.get("data");
        for (Map<String, Object> job : jobs) {
            String desc = (String) job.get("jobDesc");
            String handler = (String) job.get("executorHandler");
            if (jobDesc.equals(desc) && executorHandler.equals(handler)) {
                return true;
            }
        }
        return false;
    }
}
