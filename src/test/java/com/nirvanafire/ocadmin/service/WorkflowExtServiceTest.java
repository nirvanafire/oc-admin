package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.WorkflowOperationDTO;
import com.nirvanafire.ocadmin.dto.WorkflowStatisticsDTO;
import com.nirvanafire.ocadmin.entity.*;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.impl.WorkflowExtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WorkflowExtService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WorkflowExtServiceTest {

    @Mock
    private ApprovalOperationRepository approvalOperationRepository;
    @Mock
    private ApprovalCcRepository approvalCcRepository;
    @Mock
    private ApprovalReminderRepository approvalReminderRepository;
    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private ApprovalTaskRepository approvalTaskRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkflowExtServiceImpl workflowExtService;

    private ApprovalTask testTask;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = SysUser.builder()
                .id(1L)
                .username("testuser")
                .nickname("测试用户")
                .build();

        testTask = ApprovalTask.builder()
                .id(1L)
                .taskId("task123")
                .requestId(1L)
                .assigneeId(1L)
                .assigneeName("测试用户")
                .taskStatus("PENDING")
                .build();
    }

    @Test
    void executeOperation_Approve_Success() {
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(approvalOperationRepository.save(any(ApprovalOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(1L);
        dto.setOperationType("approve");
        dto.setComment("同意");

        assertDoesNotThrow(() -> {
            workflowExtService.executeOperation(1L, "testuser", dto);
        });

        verify(approvalTaskRepository).save(any(ApprovalTask.class));
        verify(approvalOperationRepository).save(any(ApprovalOperation.class));
    }

    @Test
    void executeOperation_Reject_Success() {
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(approvalOperationRepository.save(any(ApprovalOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(1L);
        dto.setOperationType("reject");
        dto.setComment("材料不全");

        assertDoesNotThrow(() -> {
            workflowExtService.executeOperation(1L, "testuser", dto);
        });

        verify(approvalTaskRepository).save(any(ApprovalTask.class));
    }

    @Test
    void executeOperation_NotOwner() {
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(1L);
        dto.setOperationType("approve");

        assertThrows(BusinessException.class, () -> {
            workflowExtService.executeOperation(2L, "otheruser", dto);
        });
    }

    @Test
    void executeOperation_AlreadyCompleted() {
        testTask.setTaskStatus("COMPLETED");
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(1L);
        dto.setOperationType("approve");

        assertThrows(BusinessException.class, () -> {
            workflowExtService.executeOperation(1L, "testuser", dto);
        });
    }

    @Test
    void executeOperation_Transfer_Success() {
        SysUser targetUser = SysUser.builder()
                .id(2L)
                .username("targetuser")
                .nickname("目标用户")
                .build();

        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(approvalOperationRepository.save(any(ApprovalOperation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        WorkflowOperationDTO dto = new WorkflowOperationDTO();
        dto.setTaskId(1L);
        dto.setOperationType("transfer");
        dto.setTargetUserId(2L);
        dto.setTargetUserName("目标用户");

        assertDoesNotThrow(() -> {
            workflowExtService.executeOperation(1L, "testuser", dto);
        });

        verify(approvalTaskRepository).save(any(ApprovalTask.class));
    }

    @Test
    void addCc_Success() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(1L)
                .title("测试申请")
                .build();

        SysUser user1 = SysUser.builder().id(2L).username("user1").nickname("用户1").build();
        SysUser user2 = SysUser.builder().id(3L).username("user2").nickname("用户2").build();

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user2));
        when(approvalCcRepository.save(any(ApprovalCc.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> {
            workflowExtService.addCc(1L, List.of(2L, 3L));
        });

        verify(approvalCcRepository, times(2)).save(any(ApprovalCc.class));
    }

    @Test
    void remind_Success() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(1L)
                .applicantId(1L)
                .status("PENDING")
                .build();

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalReminderRepository.getMaxReminderCount(1L)).thenReturn(0);
        when(approvalTaskRepository.findByRequestIdAndTaskStatus(1L, "PENDING")).thenReturn(List.of());
        when(approvalReminderRepository.save(any(ApprovalReminder.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> {
            workflowExtService.remind(1L, "testuser", 1L);
        });

        verify(approvalReminderRepository).save(any(ApprovalReminder.class));
    }

    @Test
    void remind_AlreadyCompleted() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(1L)
                .applicantId(1L)
                .status("COMPLETED")
                .build();

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BusinessException.class, () -> {
            workflowExtService.remind(1L, "testuser", 1L);
        });
    }

    @Test
    void remind_ExceedLimit() {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(1L)
                .applicantId(1L)
                .status("PENDING")
                .build();

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalReminderRepository.getMaxReminderCount(1L)).thenReturn(3);

        assertThrows(BusinessException.class, () -> {
            workflowExtService.remind(1L, "testuser", 1L);
        });
    }

    @Test
    void getOverviewStatistics_Success() {
        ApprovalRequest req1 = ApprovalRequest.builder().id(1L).applicantId(1L).status("PENDING").build();
        ApprovalRequest req2 = ApprovalRequest.builder().id(2L).applicantId(1L).status("COMPLETED").build();
        ApprovalRequest req3 = ApprovalRequest.builder().id(3L).applicantId(1L).status("COMPLETED").build();
        ApprovalRequest req4 = ApprovalRequest.builder().id(4L).applicantId(1L).status("REJECTED").build();

        when(approvalRequestRepository.findByApplicantId(1L)).thenReturn(List.of(req1, req2, req3, req4));

        WorkflowStatisticsDTO result = workflowExtService.getOverviewStatistics(1L);

        assertNotNull(result);
        assertEquals(4L, result.getTotalRequests());
        assertEquals(1L, result.getPendingRequests());
        assertEquals(2L, result.getApprovedRequests());
        assertEquals(1L, result.getRejectedRequests());
    }

    @Test
    void getOperationHistory_Success() {
        ApprovalOperation op1 = ApprovalOperation.builder()
                .id(1L)
                .requestId(1L)
                .operatorId(1L)
                .operatorName("用户A")
                .operationType("approve")
                .comment("同意")
                .createTime(LocalDateTime.now())
                .build();

        ApprovalOperation op2 = ApprovalOperation.builder()
                .id(2L)
                .requestId(1L)
                .operatorId(2L)
                .operatorName("用户B")
                .operationType("reject")
                .comment("拒绝")
                .createTime(LocalDateTime.now())
                .build();

        when(approvalOperationRepository.findByRequestIdOrderByCreateTimeDesc(1L))
                .thenReturn(List.of(op1, op2));

        List<Object> result = workflowExtService.getOperationHistory(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
