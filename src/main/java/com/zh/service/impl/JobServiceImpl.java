package com.zh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zh.config.XxlJobProperties;
import com.zh.model.JobConfig;
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
                    sessionCookie = cookie;
                    break;
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
    public boolean addJob(JobConfig jobConfig) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/add";

        // 将 JobConfig 转换为 MultiValueMap 以便以表单方式提交
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("jobGroup", String.valueOf(jobConfig.getJobGroup()));  // 使用 JobConfig 中的 jobGroup

        // 检查并添加 JobConfig 中的其他参数
        if (jobConfig.getJobDesc() != null) formParams.add("jobDesc", jobConfig.getJobDesc());
        if (jobConfig.getExecutorHandler() != null) formParams.add("executorHandler", jobConfig.getExecutorHandler());
        if (jobConfig.getScheduleType() != null) formParams.add("scheduleType", jobConfig.getScheduleType().getValue());
        if (jobConfig.getScheduleConf() != null) formParams.add("scheduleConf", jobConfig.getScheduleConf());
        if (jobConfig.getExecutorRouteStrategy() != null) formParams.add("executorRouteStrategy", jobConfig.getExecutorRouteStrategy().getValue());
        if (jobConfig.getExecutorBlockStrategy() != null) formParams.add("executorBlockStrategy", jobConfig.getExecutorBlockStrategy().getValue());
        if (jobConfig.getGlueType() != null) formParams.add("glueType", jobConfig.getGlueType().getValue());
        if (jobConfig.getAuthor() != null) formParams.add("author", jobConfig.getAuthor());
        if (jobConfig.getMisfireStrategy() != null) formParams.add("misfireStrategy", jobConfig.getMisfireStrategy().getValue());
        formParams.add("executorTimeout", String.valueOf(jobConfig.getExecutorTimeout()));
        formParams.add("executorFailRetryCount", String.valueOf(jobConfig.getExecutorFailRetryCount()));

        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 设置 Content-Type 为表单格式

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 如果响应状态不是 OK 且使用 session 模式，则重新登录
        if (response.getStatusCode() != HttpStatus.OK && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(formParams, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        // 解析返回的 JSON 响应
        try {
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                int code = jsonResponse.path("code").asInt();
                String msg = jsonResponse.path("msg").asText();

                // 如果 code 不是 200，则返回失败
                if (code != 200) {
                    System.out.println("Error: " + msg);
                    return false;
                }
            } else {
                System.out.println("Request failed with status: " + response.getStatusCode());
                return false;
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing response: " + e.getMessage());
            return false;
        }

        return true; // 成功返回 true
    }




    @Override
    public boolean updateJob(int jobId,JobConfig jobConfig) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/update";

        // 将 JobConfig 转换为 MultiValueMap 以便以表单方式提交
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 添加 jobId
        formParams.add("jobGroup", String.valueOf(jobConfig.getJobGroup()));  // 使用 JobConfig 中的 jobGroup
        formParams.add("jobDesc", jobConfig.getJobDesc()); // 任务描述
        formParams.add("executorHandler", jobConfig.getExecutorHandler()); // 执行器处理器
        formParams.add("scheduleType", jobConfig.getScheduleType().getValue()); // 调度类型
        formParams.add("scheduleConf", jobConfig.getScheduleConf()); // 调度配置
        formParams.add("executorRouteStrategy", jobConfig.getExecutorRouteStrategy().getValue()); // 执行器路由策略
        formParams.add("executorBlockStrategy", jobConfig.getExecutorBlockStrategy().getValue()); // 执行器阻塞策略
        formParams.add("glueType", jobConfig.getGlueType().getValue()); // Glue 类型
        formParams.add("author", jobConfig.getAuthor()); // 作者
        formParams.add("misfireStrategy", jobConfig.getMisfireStrategy().getValue()); // 任务过期策略
        formParams.add("executorTimeout", String.valueOf(jobConfig.getExecutorTimeout())); // 执行超时时间
        formParams.add("executorFailRetryCount", String.valueOf(jobConfig.getExecutorFailRetryCount())); // 重试次数
        formParams.add("failStrategy", jobConfig.getFailStrategy().getValue()); // 任务失败策略

        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 设置 Content-Type 为表单格式

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 如果响应状态不是 OK 且使用 session 模式，则重新登录
        if (response.getStatusCode() != HttpStatus.OK && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(formParams, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        // 解析返回的 JSON 响应
        try {
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                int code = jsonResponse.path("code").asInt();
                String msg = jsonResponse.path("msg").asText();

                // 如果 code 不是 200，则返回失败
                if (code != 200) {
                    System.out.println("Error: " + msg);
                    return false;
                }
            } else {
                System.out.println("Request failed with status: " + response.getStatusCode());
                return false;
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing response: " + e.getMessage());
            return false;
        }

        return true; // 成功返回 true
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

        // 将参数封装为表单格式
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("start", String.valueOf(start));
        formParams.add("length", String.valueOf(length));
        formParams.add("jobGroup", String.valueOf(getJobGroupIdByAppname(xxlJobProperties.getAppname())));
        formParams.add("triggerStatus", String.valueOf("1"));

        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // 设置 Content-Type 为表单格式

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        // 如果认证失败并且使用 session 认证模式，则重新登录
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            HttpEntity<MultiValueMap<String, String>> sessionEntity = new HttpEntity<>(formParams, createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, sessionEntity, Map.class);
        }

        return response.getBody();
    }




    public Integer getJobGroupIdByAppname(String appname) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobgroup/pageList";

        // 构建请求头
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());

        // 发起请求，获取执行器组列表
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        // 如果需要使用 session 认证，检测是否有未授权的响应并进行二次登录
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(createHeadersWithSessionCookie());
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        }

        // 处理响应数据
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> jobGroups = (List<Map<String, Object>>) response.getBody().get("data");

            // 遍历 jobGroups 查找匹配的 appname
            for (Map<String, Object> group : jobGroups) {
                if (appname.equals(group.get("appname"))) {
                    return (Integer) group.get("id"); // 返回找到的 jobGroupId
                }
            }
        }

        throw new IllegalStateException("未找到匹配的 jobGroup，appname=" + appname);
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
