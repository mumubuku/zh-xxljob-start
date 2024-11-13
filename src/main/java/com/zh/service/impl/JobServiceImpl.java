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
        formParams.add("jobGroup", String.valueOf(jobConfig.getJobGroup()));

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
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送请求
        ResponseEntity<String> response = sendRequest(url, entity);

        // 解析响应
        return handleResponse(response);
    }




    @Override
    public boolean updateJob(int jobId, JobConfig jobConfig) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/update";
        // 将 JobConfig 转换为 MultiValueMap 以便以表单方式提交
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 添加 jobId
        formParams.add("jobGroup", String.valueOf(jobConfig.getJobGroup()));
        formParams.add("jobDesc", jobConfig.getJobDesc());
        formParams.add("executorHandler", jobConfig.getExecutorHandler());
        formParams.add("scheduleType", jobConfig.getScheduleType().getValue());
        formParams.add("scheduleConf", jobConfig.getScheduleConf());
        formParams.add("executorRouteStrategy", jobConfig.getExecutorRouteStrategy().getValue());
        formParams.add("executorBlockStrategy", jobConfig.getExecutorBlockStrategy().getValue());
        formParams.add("glueType", jobConfig.getGlueType().getValue());
        formParams.add("author", jobConfig.getAuthor());
        formParams.add("misfireStrategy", jobConfig.getMisfireStrategy().getValue());
        formParams.add("executorTimeout", String.valueOf(jobConfig.getExecutorTimeout()));
        formParams.add("executorFailRetryCount", String.valueOf(jobConfig.getExecutorFailRetryCount()));
        formParams.add("failStrategy", jobConfig.getFailStrategy().getValue());
        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);
        // 发送请求
        ResponseEntity<String> response = sendRequest(url, entity);
        // 解析响应
        return handleResponse(response);
    }


    @Override
    public boolean removeJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/remove";

        // 使用 MultiValueMap 来构造表单数据
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 将 jobId 转为 String 并添加到表单参数中

        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // 设置 Content-Type 为 x-www-form-urlencoded

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送请求
        ResponseEntity<String> response = sendRequest(url, entity);

        // 解析响应并返回
        return handleResponse(response);
    }


    @Override
    public boolean startJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/start";

        // 使用 MultiValueMap 构造表单数据
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 将 jobId 转为 String 并添加到表单参数中

        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // 设置 Content-Type 为 x-www-form-urlencoded

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送请求
        ResponseEntity<String> response = sendRequest(url, entity);


        // 解析响应并返回
        return handleResponse(response);
    }


    @Override
    public boolean stopJob(int jobId) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/stop";

        // 使用 MultiValueMap 构造表单数据
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 将 jobId 转为 String 并添加到表单参数中

        // 设置请求头
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // 设置 Content-Type 为 x-www-form-urlencoded

        // 构建 HTTP 实体对象
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        // 发送请求
        ResponseEntity<String> response = sendRequest(url, entity);


        // 解析响应并返回
        return handleResponse(response);
    }


    @Override
    public boolean triggerJob(int jobId, String executorParam) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/trigger";

        // 使用 MultiValueMap 构造表单数据
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));  // 将 jobId 转为 String 并添加到表单参数中
        formParams.add("executorParam", executorParam); // 添加 executorParam 参数

        // 创建表单实体
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, createHeaders());

        // 使用 sendRequest 方法发送请求
        ResponseEntity<String> response = sendRequest(url, entity);

        // 解析响应并返回
        return handleResponse(response);
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

        return response.getBody();
    }



    // 初始化 ObjectMapper 用于解析 JSON 字符串
    ObjectMapper objectMapper = new ObjectMapper();

    public Integer getJobGroupIdByAppname(String appname) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobgroup/pageList";

        // 使用 MultiValueMap 构造表单数据（如果需要表单参数）
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        // 如果需要可以添加表单参数，这里暂时没有显式的参数需求，您可以根据实际需求修改

        // 构建请求头
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, createHeaders());

        // 使用 sendRequest 方法发送请求
        ResponseEntity<String> response = sendRequest(url, entity);

        Map<String, Object> responseBody = null;
        try {
            // 将响应字符串转换为 Map
            responseBody = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            // 如果转换失败，抛出异常
            throw new IllegalStateException("响应内容转换为 Map 失败", e);
        }

        // 处理响应数据
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> jobGroups = (List<Map<String, Object>>) responseBody.get("data");

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


    private boolean handleResponse(ResponseEntity<String> response) {
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



    private ResponseEntity<String> sendRequest(String url, HttpEntity<?> entity) {
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(entity.getBody(), createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response;
    }








}
