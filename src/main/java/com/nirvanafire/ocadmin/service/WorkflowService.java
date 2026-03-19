package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.entity.ApprovalRequest;
import com.nirvanafire.ocadmin.entity.ApprovalTask;
import com.nirvanafire.ocadmin.entity.ProcessDefinition;

import java.util.List;
import java.util.Map;

public interface WorkflowService {

    /**
     * 部署BPMN流程
     */
    ProcessDefinition deployProcess(String processName, String processKey, String bpmnXml, String description);

    /**
     * 获取流程定义列表
     */
    List<ProcessDefinition> getProcessDefinitions();

    /**
     * 删除流程定义
     */
    void deleteProcessDefinition(Long id);

    /**
     * 更新流程定义（基本信息）
     */
    ProcessDefinition updateProcessDefinition(Long id, String processName, String processKey, String description);

    /**
     * 保存流程定义（更新XML）
     */
    ProcessDefinition saveProcessDefinition(Long id, String processName, String processKey, String bpmnXml, String description);

    /**
     * 提交审核申请
     */
    ApprovalRequest submitRequest(Long applicantId, String applicantName, String applicantEmail,
                                   String title, String processKey, Map<String, Object> formData);

    /**
     * 获取我的申请记录
     */
    List<ApprovalRequest> getMyRequests(Long applicantId);

    /**
     * 获取申请详情
     */
    ApprovalRequest getRequest(Long requestId);

    /**
     * 获取我的待审核任务
     */
    List<ApprovalTask> getMyTasks(Long assigneeId);

    /**
     * 获取任务详情
     */
    ApprovalTask getTask(String taskId);

    /**
     * 完成审核任务
     */
    ApprovalTask completeTask(String taskId, Long assigneeId, String assigneeName, String action, String comment);
}
