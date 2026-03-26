package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.ApprovalRequestDTO;
import com.nirvanafire.ocadmin.dto.SubmitRequestResponse;
import com.nirvanafire.ocadmin.dto.TaskDTO;
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
    SubmitRequestResponse submitRequest(Long applicantId, String applicantName, String applicantEmail,
                                   String title, String processKey, Map<String, Object> formData);

    /**
     * 获取我的申请记录
     */
    List<ApprovalRequest> getMyRequests(Long applicantId);

    /**
     * 获取申请详情
     */
    ApprovalRequestDTO getRequest(Long requestId);

    /**
     * 获取我的待审核任务
     * @param assigneeId 审核人ID
     * @param taskStatus 任务状态筛选（null或空表示所有非COMPLETED状态，PENDING/REJECTED/CANCELLED）
     */
    List<TaskDTO> getMyTasks(Long assigneeId, String taskStatus);

    /**
     * 获取任务详情
     */
    TaskDTO getTask(String taskId);

    /**
     * 完成审核任务
     */
    ApprovalTask completeTask(String taskId, Long assigneeId, String assigneeName, String action, String comment);

    /**
     * 检查流程是否已部署到Flowable引擎
     */
    boolean isProcessDeployed(Long id);

    /**
     * 部署指定流程定义（使用已保存的BPMN XML）
     */
    ProcessDefinition deployDefinition(Long id);

    /**
     * 批量部署流程定义
     */
    List<ProcessDefinition> batchDeployDefinition(List<Long> ids);

    /**
     * 获取Flowable引擎中所有已部署的流程定义（调试用）
     */
    List<Map<String, Object>> getAllDeployedProcesses();

    /**
     * 撤销申请（仅当审核人未处理时可撤销）
     */
    ApprovalRequest cancelRequest(Long requestId, Long applicantId);

    /**
     * 撤回申请（仅当申请被拒绝时可撤回，逻辑删除）
     */
    ApprovalRequest withdrawRequest(Long requestId, Long applicantId);

    /**
     * 获取申请的任务记录（审核历史）
     */
    List<TaskDTO> getRequestTasks(Long requestId);
}
