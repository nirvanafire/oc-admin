package com.nirvanafire.ocadmin.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送审核通知给审核人
     *
     * @param approverEmail  审核人邮箱
     * @param approverName   审核人姓名
     * @param requestTitle   申请标题
     * @param applicantName  申请人姓名
     * @param taskId         任务ID
     */
    void sendApprovalNotification(String approverEmail, String approverName,
                                   String requestTitle, String applicantName, String taskId);

    /**
     * 发送审核结果通知给申请人
     *
     * @param applicantEmail 申请人邮箱
     * @param applicantName  申请人姓名
     * @param requestTitle   申请标题
     * @param result         审核结果（通过/拒绝）
     * @param comment        审核意见
     */
    void sendApprovalResultNotification(String applicantEmail, String applicantName,
                                         String requestTitle, String result, String comment);
}
