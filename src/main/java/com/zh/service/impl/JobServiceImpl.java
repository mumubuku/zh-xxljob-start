package com.zh.service.impl;


import com.zh.config.XxlJobProperties;
import com.zh.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mumu
 */
@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private XxlJobProperties xxlJobProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private String sessionCookie; // 保存 Session Cookie

    /**
     * 使用用户名和密码登录获取 Session Cookie
     */
    private void login() {
        String url = xxlJobProperties.getAdminAddresses() + "/login";

        // 构造登录请求体
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
        loginParams.add("userName", xxlJobProperties.getUsername());
        loginParams.add("password", xxlJobProperties.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(loginParams, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 从响应头中提取 JSESSIONID
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (cookie.contains("JSESSIONID")) {
                        sessionCookie = cookie;
                        break;
                    }
                }
            }
            System.out.println("登录成功，获取到 Session Cookie: " + sessionCookie);
        } else {
            throw new RuntimeException("登录失败，无法获取 Session Cookie");
        }
    }

    /**
     * 创建包含 Session Cookie 的请求头
     */
    private HttpHeaders createHeadersWithSessionCookie() {
        if (sessionCookie == null) {
            login();  // 如果 sessionCookie 为空则执行登录
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);  // 添加 sessionCookie 到 Cookie 中
        return headers;
    }

    /**
     * 创建包含 accessToken 的请求头
     */
    private HttpHeaders createHeadersWithAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("XXL-JOB-ACCESS-TOKEN", xxlJobProperties.getAccessToken());
        return headers;
    }

    /**
     * 根据认证模式创建请求头
     */
    private HttpHeaders createHeaders() {
        if ("session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            return createHeadersWithSessionCookie();
        } else {
            return createHeadersWithAccessToken();
        }
    }

    @Override
    public boolean addJob(Map<String, Object> jobConfig) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/add";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobConfig, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 如果使用 session 模式且 session 过期则重新登录
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(jobConfig, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }


    @Override
    public boolean updateJob(Map<String, Object> jobConfig) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/update";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobConfig, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 如果使用 session 模式且 session 过期则重新登录
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(jobConfig, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean removeJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/remove";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(requestBody, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean startJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/start";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(requestBody, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean stopJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/stop";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(requestBody, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean triggerJob(int jobId, String executorParam) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/trigger";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", jobId);
        requestBody.put("executorParam", executorParam);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(requestBody, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public Map<String, Object> getJobList(int start, int length) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/pageList";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("start", start);
        requestBody.put("length", length);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(requestBody, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, Map.class);
        }

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
