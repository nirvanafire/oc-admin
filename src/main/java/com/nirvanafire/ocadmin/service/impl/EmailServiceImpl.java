package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private static final String FROM = "noreply@example.com";

    @Override
    @Async
    public void sendApprovalNotification(String approverEmail, String approverName,
                                          String requestTitle, String applicantName, String taskId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM);
            message.setTo(approverEmail);
            message.setSubject("【审核通知】您有一项新的审核任务待处理");
            message.setText(buildApprovalNotificationContent(approverName, requestTitle, applicantName, taskId));
            mailSender.send(message);
            log.info("审核通知邮件已发送至: {}", approverEmail);
        } catch (Exception e) {
            log.error("发送审核通知邮件失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendApprovalResultNotification(String applicantEmail, String applicantName,
                                                 String requestTitle, String result, String comment) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM);
            message.setTo(applicantEmail);
            message.setSubject("【审核结果通知】您的申请已处理完成");
            message.setText(buildApprovalResultContent(applicantName, requestTitle, result, comment));
            mailSender.send(message);
            log.info("审核结果通知邮件已发送至: {}", applicantEmail);
        } catch (Exception e) {
            log.error("发送审核结果通知邮件失败: {}", e.getMessage(), e);
        }
    }

    private String buildApprovalNotificationContent(String approverName, String requestTitle,
                                                      String applicantName, String taskId) {
        return String.format("""
            您好，%s：

            您有一项新的审核任务待处理。

            申请标题：%s
            申请人：%s
            任务ID：%s

            请登录系统及时处理。

            此邮件由系统自动发送，请勿回复。
            """, approverName, requestTitle, applicantName, taskId);
    }

    private String buildApprovalResultContent(String applicantName, String requestTitle,
                                               String result, String comment) {
        return String.format("""
            您好，%s：

            您的审核申请已处理完成。

            申请标题：%s
            审核结果：%s
            审核意见：%s

            此邮件由系统自动发送，请勿回复。
            """, applicantName, requestTitle, result, comment != null ? comment : "无");
    }
}
