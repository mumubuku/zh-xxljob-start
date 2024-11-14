package com.zh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zh.config.XxlJobProperties;
import com.zh.model.JobConfig;
import com.zh.model.TriggerStatus;
import com.zh.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private XxlJobProperties xxlJobProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private String sessionCookie;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * @Description: 新增定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobConfig:
     * @return boolean
     **/
    @Override
    public boolean addJob(JobConfig jobConfig) {
        return executeJobAction("/jobinfo/add", buildJobFormParams(jobConfig));
    }


    /**
     * @Description: 更新定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobConfig:
     * @return boolean
     **/
    @Override
    public boolean updateJob(int jobId, JobConfig jobConfig) {
        MultiValueMap<String, String> formParams = buildJobFormParams(jobConfig);
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction("/jobinfo/update", formParams);
    }


    /**
     * @Description: 移除定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobId
     * @return boolean
     **/
    @Override
    public boolean removeJob(int jobId) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction("/jobinfo/remove", formParams);
    }

    /**
     * @Description: 开启定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobId
     * @return boolean
     **/
    @Override
    public boolean startJob(int jobId) {
        return triggerJobAction(jobId, "/jobinfo/start");
    }

    /**
     * @Description: 关闭定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobId
     * @return boolean
     **/
    @Override
    public boolean stopJob(int jobId) {
        return triggerJobAction(jobId, "/jobinfo/stop");
    }

    /**
     * @Description: 手动触发定时任务方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param jobId
     * @return boolean
     **/
    @Override
    public boolean triggerJob(int jobId, String executorParam) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/trigger";
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        formParams.add("executorParam", executorParam);
        return executeJobAction(url, formParams);
    }

    /**
     * @Description: 获取定时任务列表
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param
     * @return boolean
     **/
    @Override
    public Map<String, Object> getJobList(TriggerStatus triggerStatus) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobinfo/pageList";
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("jobGroup", String.valueOf(getJobGroupIdByAppname(xxlJobProperties.getAppname())));
        formParams.add("triggerStatus", triggerStatus.getValue());

        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }

    /**
     * @Description: 检查定时任务是否已经存在的接口
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param
     * @return boolean
     **/
    @Override
    public boolean checkJobExists(String jobDesc, String executorHandler) {
        Map<String, Object> jobList = getJobList(TriggerStatus.TRIGGERED);  // 假设最多检查前100个任务
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


    /**
     * @Description: 通过appName获取group id的方法
     * @Author: tianyunzhao
     * @Date: 2024/11/14 10:14
     * @param
     * @return boolean
     **/
    @Override
    public Integer getJobGroupIdByAppname(String appname) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobgroup/pageList";

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(new LinkedMultiValueMap<>(), createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        // Assuming the response body contains a key "jobGroups" that holds the list of job groups
        List<Map<String, Object>> jobGroups = (List<Map<String, Object>>) response.getBody().get("data");

        for (Map<String, Object> jobGroup : jobGroups) {
            if (jobGroup.get("appname").equals(appname)) {
                return (Integer) jobGroup.get("id");
            }
        }

        throw new RuntimeException("无法找到与 appname " + appname + " 对应的 jobGroupId");
    }






    private boolean executeJobAction(String actionUrl, MultiValueMap<String, String> formParams) {
        String url = xxlJobProperties.getAdminAddresses() + actionUrl;
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);
        ResponseEntity<String> response = sendRequest(url, entity);

        return handleResponse(response);
    }

    private boolean triggerJobAction(int jobId, String actionUrl) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction(actionUrl, formParams);
    }

    private boolean handleResponse(ResponseEntity<String> response) {
        try {
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                int code = jsonResponse.path("code").asInt();
                String msg = jsonResponse.path("msg").asText();
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
        return true;
    }

    private ResponseEntity<String> sendRequest(String url, HttpEntity<?> entity) {
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 处理会话过期（需要重新登录）
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())) {
            login();
            entity = new HttpEntity<>(entity.getBody(), createHeadersWithSessionCookie());
            response = restTemplate.postForEntity(url, entity, String.class);
        }

        return response;
    }

    private MultiValueMap<String, String> buildJobFormParams(JobConfig jobConfig) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("jobGroup", String.valueOf(jobConfig.getJobGroup()));
        addIfNotNull(formParams, "jobDesc", jobConfig.getJobDesc());
        addIfNotNull(formParams, "executorHandler", jobConfig.getExecutorHandler());
        addIfNotNull(formParams, "scheduleType", jobConfig.getScheduleType() != null ? jobConfig.getScheduleType().getValue() : null);
        addIfNotNull(formParams, "scheduleConf", jobConfig.getScheduleConf());
        addIfNotNull(formParams, "executorRouteStrategy", jobConfig.getExecutorRouteStrategy() != null ? jobConfig.getExecutorRouteStrategy().getValue() : null);
        addIfNotNull(formParams, "executorBlockStrategy", jobConfig.getExecutorBlockStrategy() != null ? jobConfig.getExecutorBlockStrategy().getValue() : null);
        addIfNotNull(formParams, "glueType", jobConfig.getGlueType() != null ? jobConfig.getGlueType().getValue() : null);
        addIfNotNull(formParams, "author", jobConfig.getAuthor());
        addIfNotNull(formParams, "misfireStrategy", jobConfig.getMisfireStrategy() != null ? jobConfig.getMisfireStrategy().getValue() : null);
        formParams.add("executorTimeout", String.valueOf(jobConfig.getExecutorTimeout()));
        formParams.add("executorFailRetryCount", String.valueOf(jobConfig.getExecutorFailRetryCount()));

        return formParams;
    }

    private void addIfNotNull(MultiValueMap<String, String> formParams, String key, String value) {
        if (value != null) {
            formParams.add(key, value);
        }
    }

    private void login() {
        String url = xxlJobProperties.getAdminAddresses() + "/login";
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
        loginParams.add("userName", xxlJobProperties.getUsername());
        loginParams.add("password", xxlJobProperties.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(loginParams, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            sessionCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            System.out.println("登录成功，获取到 Session Cookie: " + sessionCookie);
        } else {
            throw new RuntimeException("登录失败，无法获取 Session Cookie");
        }
    }

    private HttpHeaders createHeadersWithSessionCookie() {
        if (sessionCookie == null) login();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);
        return headers;
    }

    private HttpHeaders createHeadersWithAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("XXL-JOB-ACCESS-TOKEN", xxlJobProperties.getAccessToken());
        return headers;
    }

    private HttpHeaders createHeaders() {
        return "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())
                ? createHeadersWithSessionCookie()
                : createHeadersWithAccessToken();
    }


}
