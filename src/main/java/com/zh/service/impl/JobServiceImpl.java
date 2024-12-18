package com.zh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.zh.config.XxlJobProperties;
import com.zh.model.JobConfig;
import com.zh.model.ScheduleType;
import com.zh.model.TriggerStatus;
import com.zh.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private XxlJobProperties xxlJobProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private String sessionCookie;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 新增任务
    @Override
    public Integer addJob(JobConfig jobConfig) {
        return executeJobAction("/jobinfo/add", buildJobFormParams(jobConfig));
    }


    // Method to add a single execution job
    public Integer addSingleExecutionJob(Date triggerTime, String executorParam, String executorHandler) {
        int customId = Math.abs(UUID.randomUUID().hashCode()); // Generate unique task ID as int
        XxlJobHelper.log("Adding single execution job, task ID: " + customId);
        Boolean isExist = this.checkJobExists(String.valueOf(customId));
        Assert.isTrue(isExist, "Task with custom ID " + customId + " already exists, please modify");
        Date now = new Date();
        boolean after = triggerTime.after(now);
        Assert.isTrue(after, "The execution time must be greater than the current time");

        JobConfig jobConfig = new JobConfig();
        jobConfig.setJobDesc("Single execution job");
        jobConfig.setAuthor(String.valueOf(customId));
        jobConfig.setScheduleType(ScheduleType.CRON);
        String cron = getCron(triggerTime);
        jobConfig.setScheduleConf(cron);
        jobConfig.setExecutorHandler(executorHandler);
        jobConfig.setExecutorParam(executorParam);

        Integer jobId = addJob(jobConfig);
        XxlJobHelper.log("Successfully added single execution job, task ID: " + jobId);
        return jobId;
    }

    public static String getCron(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
        String formatTimeStr = "";
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }

        return formatTimeStr;
    }

    // 更新任务
    @Override
    public boolean updateJob(int jobId, JobConfig jobConfig) {
        MultiValueMap<String, String> formParams = buildJobFormParams(jobConfig);
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction("/jobinfo/update", formParams) != null;
    }

    // 移除任务
    @Override
    public boolean removeJob(int jobId) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction("/jobinfo/remove", formParams) != null;
    }

    // 开启任务
    @Override
    public boolean startJob(int jobId) {
        return triggerJobAction(jobId, "/jobinfo/start");
    }

    // 停止任务
    @Override
    public boolean stopJob(int jobId) {
        return triggerJobAction(jobId, "/jobinfo/stop");
    }

    // 手动触发任务
    @Override
    public boolean triggerJob(int jobId, String executorParam) {
        String url =   "/jobinfo/trigger";
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        formParams.add("executorParam", executorParam);
        return executeJobAction(url, formParams) != null;
    }

    // 获取任务列表
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

    // 通过 appName 获取 jobGroupId
    @Override
    public Integer getJobGroupIdByAppname(String appname) {
        String url = xxlJobProperties.getAdminAddresses() + "/jobgroup/pageList";

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(new LinkedMultiValueMap<>(), createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        List<Map<String, Object>> jobGroups = (List<Map<String, Object>>) response.getBody().get("data");

        for (Map<String, Object> jobGroup : jobGroups) {
            if (jobGroup.get("appname").equals(appname)) {
                return (Integer) jobGroup.get("id");
            }
        }

        throw new RuntimeException("无法找到与 appname " + appname + " 对应的 jobGroupId");
    }


    @Override
    public boolean checkJobExists(String id) {
        Map<String, Object> jobList = getJobList(TriggerStatus.TRIGGERED);
        if (jobList == null) return false;

        List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobList.get("data");
        for (Map<String, Object> job : jobs) {
            String resultId = (String) job.get("id");

            if (id.equals(resultId) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkJobExists(String jobDesc, String executorHandler) {
        Map<String, Object> jobList = getJobList(TriggerStatus.TRIGGERED);
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

    // 统一处理任务操作，返回 jobId
    private Integer executeJobAction(String actionUrl, MultiValueMap<String, String> formParams) {
        String url = xxlJobProperties.getAdminAddresses() + actionUrl;
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);
        ResponseEntity<String> response = sendRequest(url, entity);

        return extractJobIdFromResponse(response);
    }

    // 触发任务操作
    private boolean triggerJobAction(int jobId, String actionUrl) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", String.valueOf(jobId));
        return executeJobAction(actionUrl, formParams) != null;
    }

    // 提取响应中的 jobId
    private Integer extractJobIdFromResponse(ResponseEntity<String> response) {
        try {
            if (response.getStatusCodeValue() >= 200 && response.getStatusCodeValue() < 300) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                int code = jsonResponse.path("code").asInt();
                String msg = jsonResponse.path("msg").asText();
                if (code != 200) {
                    System.out.println("Error: " + msg);
                    return null;
                }
                // 提取 content 字段中的 jobId
                return jsonResponse.path("content").asInt();
            } else {
                System.out.println("Request failed with status: " + response.getStatusCode());
                return null;
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing response: " + e.getMessage());
            return null;
        }
    }

    // 发送请求并处理会话过期情况
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

    // 登录方法获取 sessionCookie
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

    // 创建带 Session Cookie 的 Headers
    private HttpHeaders createHeadersWithSessionCookie() {
        if (sessionCookie == null) login();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);
        return headers;
    }

    // 创建带 Access Token 的 Headers
    private HttpHeaders createHeadersWithAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("XXL-JOB-ACCESS-TOKEN", xxlJobProperties.getAccessToken());
        return headers;
    }

    // 创建 Headers
    private HttpHeaders createHeaders() {
        return "session".equalsIgnoreCase(xxlJobProperties.getAuthMode())
                ? createHeadersWithSessionCookie()
                : createHeadersWithAccessToken();
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
}
