package com.nirvanafire.ocadmin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nirvanafire.ocadmin.common.exception.BusinessException;
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
        Integer newVersion = 1;
        ProcessDefinition existing = processDefinitionRepository.findTopByProcessKeyAndStatusOrderByVersionDesc(processKey, 1).orElse(null);
        if (existing != null) {
            newVersion = existing.getVersion() + 1;
        }

        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processKey);

        if (bpmnXml != null && bpmnXml.contains("<?xml")) {
            builder.addString(processKey + ".bpmn20.xml", bpmnXml);
        } else {
            builder.addString(processKey + ".bpmn20.xml", bpmnXml);
        }

        Deployment deployment = builder.deploy();

        ProcessDefinition processDef = ProcessDefinition.builder()
                .processKey(processKey)
                .processName(processName)
                .description(description)
                .flowableDefinitionId(deployment.getId())
                .xml(bpmnXml)
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
        // 部署新的 BPMN 流程到 Flowable
        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(processName)
                .key(processKey);

        if (bpmnXml != null && bpmnXml.contains("<?xml")) {
            builder.addString(processKey + ".bpmn20.xml", bpmnXml);
        } else {
            builder.addString(processKey + ".bpmn20.xml", bpmnXml);
        }

        Deployment deployment = builder.deploy();

        if (id != null) {
            // 更新已有流程
            ProcessDefinition existing = processDefinitionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("流程定义不存在"));

            existing.setProcessName(processName);
            existing.setProcessKey(processKey);
            existing.setDescription(description);
            existing.setFlowableDefinitionId(deployment.getId());
            existing.setXml(bpmnXml);

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
                    .xml(bpmnXml)
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
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalRequest getRequest(Long requestId) {
        return approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
    }

    @Override
    public List<ApprovalTask> getMyTasks(Long assigneeId) {
        return approvalTaskRepository.findAll().stream()
                .filter(t -> assigneeId.equals(t.getAssigneeId()) && "PENDING".equals(t.getTaskStatus()))
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalTask getTask(String taskId) {
        return approvalTaskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
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
}
