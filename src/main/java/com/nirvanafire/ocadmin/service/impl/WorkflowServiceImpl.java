package com.nirvanafire.ocadmin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.TaskDTO;
import com.nirvanafire.ocadmin.entity.ApprovalRequest;
import com.nirvanafire.ocadmin.entity.ApprovalTask;
import com.nirvanafire.ocadmin.entity.ProcessDefinition;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.ApprovalNodeRepository;
import com.nirvanafire.ocadmin.repository.ApprovalRequestRepository;
import com.nirvanafire.ocadmin.repository.ApprovalTaskRepository;
import com.nirvanafire.ocadmin.repository.ProcessDefinitionRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.EmailService;
import com.nirvanafire.ocadmin.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalTaskRepository approvalTaskRepository;
    private final ApprovalNodeRepository approvalNodeRepository;
    private final UserRepository userRepository;
    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProcessDefinition deployProcess(String processName, String processKey, String bpmnXml, String description) {
        // 验证 BPMN XML 格式
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new BusinessException("BPMN 流程XML不能为空");
        }

        // 规范化 BPMN XML，确保 process id 与 processKey 一致
        String normalizedXml = normalizeBpmnXml(bpmnXml, processKey);

        Integer newVersion = 1;
        ProcessDefinition existing = processDefinitionRepository.findTopByProcessKeyAndStatusOrderByVersionDesc(processKey, 1).orElse(null);
        if (existing != null) {
            newVersion = existing.getVersion() + 1;
        }

        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processKey);

        builder.addString(processKey + ".bpmn20.xml", normalizedXml);

        Deployment deployment = builder.deploy();

        // 验证部署是否成功创建了流程定义
        org.flowable.engine.repository.ProcessDefinition flowableDef =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .singleResult();
        if (flowableDef == null) {
            throw new BusinessException("流程部署失败：BPMN XML 格式无效，请检查流程图是否包含开始事件和结束事件");
        }

        ProcessDefinition processDef = ProcessDefinition.builder()
                .processKey(processKey)
                .processName(processName)
                .description(description)
                .flowableDefinitionId(deployment.getId())
                .xml(normalizedXml)  // 保存规范化后的 XML
                .version(newVersion)
                .status(1)
                .build();

        processDef = processDefinitionRepository.save(processDef);
        log.info("流程部署成功: processKey={}, version={}, deploymentId={}",
                processKey, newVersion, deployment.getId());

        return processDef;
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return processDefinitionRepository.findByStatus(1).stream()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProcessDefinition(Long id) {
        ProcessDefinition processDef = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        processDef.setStatus(0);
        processDefinitionRepository.save(processDef);

        approvalNodeRepository.deleteByProcessDefinitionId(id);

        log.info("流程定义已禁用: id={}, processKey={}", id, processDef.getProcessKey());
    }

    @Override
    @Transactional
    public ProcessDefinition updateProcessDefinition(Long id, String processName, String processKey, String description) {
        ProcessDefinition processDef = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        processDef.setProcessName(processName);
        processDef.setProcessKey(processKey);
        processDef.setDescription(description);

        processDef = processDefinitionRepository.save(processDef);
        log.info("流程定义已更新: id={}, processKey={}", id, processKey);

        return processDef;
    }

    @Override
    @Transactional
    public ProcessDefinition saveProcessDefinition(Long id, String processName, String processKey, String bpmnXml, String description) {
        // 验证 BPMN XML 格式
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new BusinessException("BPMN 流程XML不能为空");
        }

        // 规范化 BPMN XML，确保 process id 与 processKey 一致
        String normalizedXml = normalizeBpmnXml(bpmnXml, processKey);

        // 部署新的 BPMN 流程到 Flowable
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processKey);

        builder.addString(processKey + ".bpmn20.xml", normalizedXml);

        Deployment deployment = builder.deploy();

        // 验证部署是否成功创建了流程定义
        org.flowable.engine.repository.ProcessDefinition flowableDef =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .singleResult();
        if (flowableDef == null) {
            throw new BusinessException("流程部署失败：BPMN XML 格式无效，请检查流程图是否包含开始事件和结束事件");
        }

        // 验证 Flowable 使用的 key 是否与我们的 processKey 一致
        if (!flowableDef.getKey().equals(processKey)) {
            log.warn("Flowable processKey 不匹配: expected={}, actual={}", processKey, flowableDef.getKey());
        }

        if (id != null) {
            // 更新已有流程
            ProcessDefinition existing = processDefinitionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("流程定义不存在"));

            existing.setProcessName(processName);
            existing.setProcessKey(processKey);
            existing.setDescription(description);
            existing.setFlowableDefinitionId(deployment.getId());
            existing.setXml(normalizedXml);  // 保存规范化后的 XML

            existing = processDefinitionRepository.save(existing);
            log.info("流程定义已保存（更新）: id={}, processKey={}, deploymentId={}", id, processKey, deployment.getId());
            return existing;
        } else {
            // 新建流程
            Integer newVersion = 1;
            ProcessDefinition existing = processDefinitionRepository.findTopByProcessKeyAndStatusOrderByVersionDesc(processKey, 1).orElse(null);
            if (existing != null) {
                newVersion = existing.getVersion() + 1;
            }

            ProcessDefinition processDef = ProcessDefinition.builder()
                    .processKey(processKey)
                    .processName(processName)
                    .description(description)
                    .flowableDefinitionId(deployment.getId())
                    .xml(normalizedXml)  // 保存规范化后的 XML
                    .version(newVersion)
                    .status(1)
                    .build();

            processDef = processDefinitionRepository.save(processDef);
            log.info("流程定义已保存（新建）: processKey={}, version={}, deploymentId={}", processKey, newVersion, deployment.getId());
            return processDef;
        }
    }

    @Override
    @Transactional
    public ApprovalRequest submitRequest(Long applicantId, String applicantName, String applicantEmail,
                                         String title, String processKey, Map<String, Object> formData) {
        ProcessDefinition processDef = processDefinitionRepository
                .findTopByProcessKeyAndStatusOrderByVersionDesc(processKey, 1)
                .orElseThrow(() -> new BusinessException("流程不存在: " + processKey));

        log.info("提交申请: processKey={}, 数据库记录xml长度={}, flowableDefinitionId={}",
                processKey,
                processDef.getXml() != null ? processDef.getXml().length() : 0,
                processDef.getFlowableDefinitionId());

        // 输出 BPMN XML 中的用户任务配置（用于调试）
        if (processDef.getXml() != null && processDef.getXml().contains("userTask")) {
            String xmlSnippet = extractUserTaskInfo(processDef.getXml());
            log.info("BPMN XML 中的用户任务配置: {}", xmlSnippet);
        }

        // 验证流程是否已部署到 Flowable 引擎
        org.flowable.engine.repository.ProcessDefinition flowableDef =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey)
                        .latestVersion()
                        .singleResult();
        log.info("Flowable引擎查询结果: flowableDef={}", flowableDef != null ? "id=" + flowableDef.getId() + ", key=" + flowableDef.getKey() + ", version=" + flowableDef.getVersion() : "null");

        // 如果流程在数据库中存在但引擎中不存在，尝试自动部署
        if (flowableDef == null && processDef.getXml() != null && !processDef.getXml().trim().isEmpty()) {
            log.info("流程在数据库中存在但引擎中不存在，尝试自动部署: processKey={}", processKey);
            try {
                deployDefinition(processDef.getId());
                // 重新查询 Flowable 流程定义
                flowableDef = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey)
                        .latestVersion()
                        .singleResult();
                log.info("自动部署成功: processKey={}, newFlowableDef={}", processKey, flowableDef != null ? flowableDef.getId() : "null");
            } catch (Exception e) {
                log.error("自动部署失败: processKey={}, error={}", processKey, e.getMessage());
                throw new BusinessException("申请提交失败：流程未部署，请联系系统管理员处理");
            }
        }

        if (flowableDef == null) {
            throw new BusinessException("申请提交失败：流程未部署，请联系系统管理员部署流程后重试");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", applicantId);
        variables.put("applicantName", applicantName);
        variables.put("applicantEmail", applicantEmail);
        variables.put("title", title);

        String businessKey = "REQUEST_" + System.currentTimeMillis();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);

        String formDataJson = null;
        try {
            formDataJson = objectMapper.writeValueAsString(formData);
        } catch (JsonProcessingException e) {
            log.error("表单数据序列化失败", e);
        }

        ApprovalRequest request = ApprovalRequest.builder()
                .processInstanceId(processInstance.getId())
                .businessKey(businessKey)
                .processKey(processKey)
                .title(title)
                .applicantId(applicantId)
                .applicantName(applicantName)
                .applicantEmail(applicantEmail)
                .currentNode("")
                .currentNodeName("提交申请")
                .status("PENDING")
                .formData(formDataJson)
                .build();

        request = approvalRequestRepository.save(request);

        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        for (Task task : tasks) {
            ApprovalTask approvalTask = createApprovalTask(request.getId(), task);
            approvalTaskRepository.save(approvalTask);

            if (approvalTask.getAssigneeEmail() != null) {
                emailService.sendApprovalNotification(
                        approvalTask.getAssigneeEmail(),
                        approvalTask.getAssigneeName(),
                        title,
                        applicantName,
                        task.getId()
                );
            }
        }

        log.info("审核申请已提交: requestId={}, processInstanceId={}", request.getId(), processInstance.getId());
        return request;
    }

    @Override
    public List<ApprovalRequest> getMyRequests(Long applicantId) {
        return approvalRequestRepository.findAll().stream()
                .filter(r -> r.getApplicantId().equals(applicantId))
                .filter(r -> !"WITHDRAWN".equals(r.getStatus()))
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalRequest getRequest(Long requestId) {
        return approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
    }

    @Override
    public List<TaskDTO> getMyTasks(Long assigneeId, String taskStatus) {
        // 1. 从数据库获取该用户被分配的任务
        List<ApprovalTask> assignedTasks;
        if (taskStatus == null || taskStatus.isEmpty()) {
            // 默认只显示待审核和被拒绝的任务
            assignedTasks = approvalTaskRepository.findAll().stream()
                    .filter(t -> assigneeId.equals(t.getAssigneeId())
                            && ("PENDING".equals(t.getTaskStatus()) || "REJECTED".equals(t.getTaskStatus())))
                    .collect(Collectors.toList());
        } else {
            // 按指定状态筛选
            assignedTasks = approvalTaskRepository.findAll().stream()
                    .filter(t -> assigneeId.equals(t.getAssigneeId()) && taskStatus.equals(t.getTaskStatus()))
                    .collect(Collectors.toList());
        }

        // 2. 从 Flowable 获取该用户参与的任务（assignee 或 candidate）
        // 按 assignee 查询
        List<Task> assigneeTasks = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(assigneeId))
                .list();

        // 按 candidate user 查询
        List<Task> candidateUserTasks = taskService.createTaskQuery()
                .taskCandidateUser(String.valueOf(assigneeId))
                .list();

        // 合并并去重
        Set<String> processedTaskIds = assignedTasks.stream()
                .map(ApprovalTask::getTaskId)
                .collect(Collectors.toSet());

        List<Task> allFlowableTasks = new java.util.ArrayList<>(assigneeTasks);
        for (Task task : candidateUserTasks) {
            if (!processedTaskIds.contains(task.getId())) {
                allFlowableTasks.add(task);
            }
        }

        // 3. 对于 Flowable 中有但我们的 ApprovalTask 表中没有的任务，创建或更新记录
        for (Task flowableTask : allFlowableTasks) {
            boolean existsInDb = assignedTasks.stream()
                    .anyMatch(t -> t.getTaskId().equals(flowableTask.getId()));
            if (!existsInDb) {
                // 查找是否已存在对应的 ApprovalTask
                List<ApprovalTask> existingTasks = approvalTaskRepository.findAll().stream()
                        .filter(t -> flowableTask.getId().equals(t.getTaskId()))
                        .collect(Collectors.toList());

                ApprovalTask taskToAdd;
                if (existingTasks.isEmpty()) {
                    // 创建新的 ApprovalTask 记录
                    ApprovalTask newTask = new ApprovalTask();
                    newTask.setTaskId(flowableTask.getId());
                    newTask.setRequestId(null); // 需要通过 processInstanceId 查找
                    newTask.setTaskStatus("PENDING");

                    // 优先使用 assignee，如果没有则设置当前用户为 assignee
                    String assignee = flowableTask.getAssignee();
                    if (assignee != null) {
                        try {
                            Long taskAssigneeId = Long.parseLong(assignee);
                            SysUser user = userRepository.findById(taskAssigneeId).orElse(null);
                            if (user != null) {
                                newTask.setAssigneeId(user.getId());
                                newTask.setAssigneeName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                                newTask.setAssigneeEmail(user.getEmail());
                            }
                        } catch (NumberFormatException e) {
                            // assignee 不是数字ID，设置当前用户为 assignee
                            newTask.setAssigneeId(assigneeId);
                            SysUser user = userRepository.findById(assigneeId).orElse(null);
                            if (user != null) {
                                newTask.setAssigneeName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                            }
                        }
                    } else {
                        // 没有 assignee，设置当前用户为 assignee
                        newTask.setAssigneeId(assigneeId);
                        SysUser user = userRepository.findById(assigneeId).orElse(null);
                        if (user != null) {
                            newTask.setAssigneeName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                        }
                    }

                    approvalTaskRepository.save(newTask);
                    taskToAdd = newTask;
                } else {
                    taskToAdd = existingTasks.get(0);
                }
                assignedTasks.add(taskToAdd);
            }
        }

        // 4. 转换为 TaskDTO，包含申请标题
        return assignedTasks.stream()
                .sorted((a, b) -> {
                    if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                    if (a.getCreateTime() == null) return 1;
                    if (b.getCreateTime() == null) return -1;
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .map(this::toTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO getTask(String taskId) {
        ApprovalTask task = approvalTaskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        return toTaskDTO(task);
    }

    @Override
    @Transactional
    public ApprovalTask completeTask(String taskId, Long assigneeId, String assigneeName, String action, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        ApprovalTask approvalTask = approvalTaskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务记录不存在"));

        if (!"PENDING".equals(approvalTask.getTaskStatus())) {
            throw new BusinessException("任务已处理");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("action", action);
        variables.put("comment", comment);
        variables.put("assigneeId", assigneeId);
        variables.put("assigneeName", assigneeName);

        approvalTask.setAction(action);
        approvalTask.setComment(comment);
        approvalTask.setTaskStatus("COMPLETED");
        approvalTask.setCompleteTime(LocalDateTime.now());

        ApprovalRequest approvalRequest = approvalRequestRepository.findById(approvalTask.getRequestId())
                .orElseThrow(() -> new BusinessException("申请记录不存在"));

        if ("APPROVE".equals(action)) {
            taskService.complete(taskId, variables);

            List<Task> nextTasks = taskService.createTaskQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .list();

            if (nextTasks.isEmpty()) {
                approvalRequest.setStatus("APPROVED");
                approvalRequest.setCompleteTime(LocalDateTime.now());
                approvalRequestRepository.save(approvalRequest);

                emailService.sendApprovalResultNotification(
                        approvalRequest.getApplicantEmail(),
                        approvalRequest.getApplicantName(),
                        approvalRequest.getTitle(),
                        "通过",
                        comment
                );
            } else {
                for (Task nextTask : nextTasks) {
                    ApprovalTask nextApprovalTask = createApprovalTask(approvalRequest.getId(), nextTask);
                    approvalTaskRepository.save(nextApprovalTask);

                    if (nextApprovalTask.getAssigneeEmail() != null) {
                        emailService.sendApprovalNotification(
                                nextApprovalTask.getAssigneeEmail(),
                                nextApprovalTask.getAssigneeName(),
                                approvalRequest.getTitle(),
                                approvalRequest.getApplicantName(),
                                nextTask.getId()
                        );
                    }
                }
            }
        } else if ("REJECT".equals(action)) {
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "Rejected by " + assigneeName);

            approvalRequest.setStatus("REJECTED");
            approvalRequest.setCompleteTime(LocalDateTime.now());
            approvalRequestRepository.save(approvalRequest);

            emailService.sendApprovalResultNotification(
                    approvalRequest.getApplicantEmail(),
                    approvalRequest.getApplicantName(),
                    approvalRequest.getTitle(),
                    "拒绝",
                    comment
            );
        }

        approvalTask = approvalTaskRepository.save(approvalTask);
        log.info("任务已完成: taskId={}, action={}", taskId, action);

        return approvalTask;
    }

    private ApprovalTask createApprovalTask(Long requestId, Task task) {
        ApprovalTask approvalTask = new ApprovalTask();
        approvalTask.setTaskId(task.getId());
        approvalTask.setRequestId(requestId);
        approvalTask.setTaskStatus("PENDING");

        if (task.getAssignee() != null) {
            try {
                Long assigneeId = Long.parseLong(task.getAssignee());
                SysUser user = userRepository.findById(assigneeId).orElse(null);
                if (user != null) {
                    approvalTask.setAssigneeId(user.getId());
                    approvalTask.setAssigneeName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    approvalTask.setAssigneeEmail(user.getEmail());
                }
            } catch (NumberFormatException e) {
                log.warn("assigneeId格式错误: {}", task.getAssignee());
            }
        }

        return approvalTask;
    }

    /**
     * 将 ApprovalTask 转换为 TaskDTO，包含申请标题和申请说明
     */
    private TaskDTO toTaskDTO(ApprovalTask task) {
        String requestTitle = null;
        String formData = null;
        if (task.getRequestId() != null) {
            ApprovalRequest request = approvalRequestRepository.findById(task.getRequestId()).orElse(null);
            if (request != null) {
                requestTitle = request.getTitle();
                formData = request.getFormData();
            }
        }

        return TaskDTO.builder()
                .id(task.getId())
                .taskId(task.getTaskId())
                .requestId(task.getRequestId())
                .requestTitle(requestTitle)
                .formData(formData)
                .assigneeId(task.getAssigneeId())
                .assigneeName(task.getAssigneeName())
                .assigneeEmail(task.getAssigneeEmail())
                .action(task.getAction())
                .comment(task.getComment())
                .taskStatus(task.getTaskStatus())
                .createTime(task.getCreateTime())
                .completeTime(task.getCompleteTime())
                .build();
    }

    @Override
    public boolean isProcessDeployed(Long id) {
        ProcessDefinition processDef = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));
        log.debug("检查流程部署状态: id={}, processKey={}, xml长度={}",
                id, processDef.getProcessKey(),
                processDef.getXml() != null ? processDef.getXml().length() : 0);
        org.flowable.engine.repository.ProcessDefinition flowableDef =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processDef.getProcessKey())
                        .latestVersion()
                        .singleResult();
        log.debug("Flowable 流程定义查询结果: flowableDef={}", flowableDef != null ? flowableDef.getId() : "null");
        return flowableDef != null;
    }

    @Override
    @Transactional
    public ProcessDefinition deployDefinition(Long id) {
        ProcessDefinition processDef = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        log.info("开始部署流程: id={}, processKey={}, xml长度={}",
                id, processDef.getProcessKey(),
                processDef.getXml() != null ? processDef.getXml().length() : 0);

        if (processDef.getXml() == null || processDef.getXml().trim().isEmpty()) {
            log.error("流程XML为空: id={}", id);
            throw new BusinessException("流程XML为空，请先设计流程再部署");
        }

        // 规范化 BPMN XML，确保 process id 与 processKey 一致
        String xml = normalizeBpmnXml(processDef.getXml(), processDef.getProcessKey());

        // 部署到 Flowable 引擎
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processDef.getProcessName())
                .key(processDef.getProcessKey());

        builder.addString(processDef.getProcessKey() + ".bpmn20.xml", xml);

        Deployment deployment = builder.deploy();
        log.info("Flowable 部署完成: deploymentId={}", deployment.getId());

        // 验证部署是否成功
        org.flowable.engine.repository.ProcessDefinition flowableDef =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .singleResult();
        if (flowableDef == null) {
            log.error("Flowable 验证失败，未找到流程定义: deploymentId={}", deployment.getId());
            throw new BusinessException("流程部署失败：BPMN XML 格式无效，请检查流程图是否包含开始事件和结束事件");
        }

        // 验证 Flowable 使用的 key 是否与我们的 processKey 一致
        if (!flowableDef.getKey().equals(processDef.getProcessKey())) {
            log.warn("Flowable processKey 不匹配: expected={}, actual={}", processDef.getProcessKey(), flowableDef.getKey());
        }

        log.info("Flowable 流程定义创建成功: flowableDefinitionId={}, key={}, version={}",
                flowableDef.getId(), flowableDef.getKey(), flowableDef.getVersion());

        // 更新流程定义的部署ID
        processDef.setFlowableDefinitionId(deployment.getId());
        processDef = processDefinitionRepository.save(processDef);

        log.info("流程部署成功: id={}, processKey={}, deploymentId={}", id, processDef.getProcessKey(), deployment.getId());
        return processDef;
    }

    /**
     * 规范化 BPMN XML，确保 process id 与指定的 processKey 一致
     */
    private String normalizeBpmnXml(String xml, String processKey) {
        log.info("开始规范化 BPMN XML: processKey={}, 原始XML长度={}", processKey, xml != null ? xml.length() : 0);

        if (xml == null || xml.isEmpty()) {
            return xml;
        }

        String normalized = xml;

        // 打印前500个字符用于调试
        log.info("BPMN XML 前500字符: {}", normalized.substring(0, Math.min(500, normalized.length())));

        // 提取原始 process id 用于日志 - 匹配各种格式
        String originalProcessId = null;
        // 尝试匹配 <bpmn:process, <bpmn2:process, 或 <process
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("<(?:bpmn2?:)?process[^>]*id=[\"']([^\"']+)[\"']");
        java.util.regex.Matcher matcher1 = pattern1.matcher(normalized);
        if (matcher1.find()) {
            originalProcessId = matcher1.group(1);
        }
        log.info("原始 BPMN process id: {}", originalProcessId);

        // 替换 bpmn:process, bpmn2:process, 或 process 的 id 属性
        // 使用更灵活的正则，处理各种属性顺序和引号格式
        normalized = normalized.replaceAll(
            "(<(?:bpmn2?:)?process[^>]*?)id=[\"'][^\"']+[\"']",
            "$1id=\"" + processKey + "\""
        );

        // 替换 targetNamespace
        normalized = normalized.replaceAll(
            "(targetNamespace=)[\\\"'][^\\\"']+[\\\"']",
            "$1\"" + processKey + "\""
        );

        // 验证替换结果
        String newProcessId = null;
        matcher1 = pattern1.matcher(normalized);
        if (matcher1.find()) {
            newProcessId = matcher1.group(1);
        }
        log.info("规范化后 BPMN process id: {}", newProcessId);

        // 打印规范化后的前500个字符
        log.info("规范化后 XML 前500字符: {}", normalized.substring(0, Math.min(500, normalized.length())));

        log.debug("BPMN XML 规范化完成: processKey={}", processKey);
        return normalized;
    }

    @Override
    @Transactional
    public List<ProcessDefinition> batchDeployDefinition(List<Long> ids) {
        return ids.stream()
                .map(this::deployDefinition)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAllDeployedProcesses() {
        List<org.flowable.engine.repository.ProcessDefinition> definitions =
                repositoryService.createProcessDefinitionQuery()
                        .list();

        return definitions.stream()
                .map(def -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", def.getId());
                    info.put("key", def.getKey());
                    info.put("name", def.getName());
                    info.put("version", def.getVersion());
                    info.put("deploymentId", def.getDeploymentId());
                    info.put("resourceName", def.getResourceName());
                    return info;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApprovalRequest cancelRequest(Long requestId, Long applicantId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));

        // 验证是否是申请人
        if (!request.getApplicantId().equals(applicantId)) {
            throw new BusinessException("只有申请人可以撤销申请");
        }

        // 验证申请状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("当前状态不允许撤销");
        }

        // 检查是否有审核人已处理任务
        List<ApprovalTask> tasks = approvalTaskRepository.findAll().stream()
                .filter(t -> t.getRequestId().equals(requestId))
                .collect(Collectors.toList());

        boolean hasApprovedTask = tasks.stream()
                .anyMatch(t -> "COMPLETED".equals(t.getTaskStatus()) && "APPROVE".equals(t.getAction()));

        if (hasApprovedTask) {
            throw new BusinessException("审核人已处理任务，无法撤销");
        }

        // 检查是否有待处理的任务
        boolean hasPendingTask = tasks.stream()
                .anyMatch(t -> "PENDING".equals(t.getTaskStatus()));

        if (hasPendingTask) {
            // 删除 Flowable 中的流程实例
            if (request.getProcessInstanceId() != null) {
                try {
                    runtimeService.deleteProcessInstance(
                            request.getProcessInstanceId(),
                            "Cancelled by applicant"
                    );
                } catch (Exception e) {
                    log.warn("删除Flowable流程实例失败: processInstanceId={}, error={}",
                            request.getProcessInstanceId(), e.getMessage());
                }
            }

            // 将任务标记为已取消
            for (ApprovalTask task : tasks) {
                if ("PENDING".equals(task.getTaskStatus())) {
                    task.setTaskStatus("CANCELLED");
                    approvalTaskRepository.save(task);
                }
            }
        }

        // 更新申请状态
        request.setStatus("CANCELLED");
        request.setCompleteTime(LocalDateTime.now());
        request = approvalRequestRepository.save(request);

        log.info("申请已撤销: requestId={}, applicantId={}", requestId, applicantId);
        return request;
    }

    @Override
    @Transactional
    public ApprovalRequest withdrawRequest(Long requestId, Long applicantId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));

        // 验证是否是申请人
        if (!request.getApplicantId().equals(applicantId)) {
            throw new BusinessException("只有申请人可以撤回申请");
        }

        // 验证申请状态 - 只有已拒绝的申请可以撤回
        if (!"REJECTED".equals(request.getStatus())) {
            throw new BusinessException("只有已拒绝的申请可以撤回");
        }

        // 逻辑删除 - 将申请状态改为 WITHDRAWN
        request.setStatus("WITHDRAWN");
        request.setCompleteTime(LocalDateTime.now());
        request = approvalRequestRepository.save(request);

        log.info("申请已撤回: requestId={}, applicantId={}", requestId, applicantId);
        return request;
    }

    /**
     * 从 BPMN XML 中提取用户任务配置信息（用于调试）
     */
    private String extractUserTaskInfo(String xml) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "<bpmn(?:2)?:userTask[^>]*>.*?</bpmn(?:2)?:userTask>",
                java.util.regex.Pattern.DOTALL
            );
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            StringBuilder info = new StringBuilder();
            int count = 0;
            while (matcher.find() && count < 5) {
                String userTask = matcher.group();
                // 提取 id, name, assignee, candidateUsers, candidateGroups
                String id = extractAttribute(userTask, "id");
                String name = extractAttribute(userTask, "name");
                String assignee = extractCamundaAttribute(userTask, "assignee");
                String candidateUsers = extractCamundaAttribute(userTask, "candidateUsers");
                String candidateGroups = extractCamundaAttribute(userTask, "candidateGroups");
                info.append(String.format("[UserTask id=%s, name=%s, assignee=%s, candidateUsers=%s, candidateGroups=%s] ",
                    id, name, assignee, candidateUsers, candidateGroups));
                count++;
            }
            return info.toString();
        } catch (Exception e) {
            return "解析失败: " + e.getMessage();
        }
    }

    private String extractAttribute(String xml, String attrName) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(attrName + "=[\"']([^\"']*)[\"']");
        java.util.regex.Matcher m = p.matcher(xml);
        return m.find() ? m.group(1) : "null";
    }

    private String extractCamundaAttribute(String xml, String attrName) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("camunda:" + attrName + "=[\"']([^\"']*)[\"']");
        java.util.regex.Matcher m = p.matcher(xml);
        return m.find() ? m.group(1) : "null";
    }
}
