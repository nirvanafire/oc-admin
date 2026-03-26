package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.TaskDTO;
import com.nirvanafire.ocadmin.entity.Task;
import com.nirvanafire.ocadmin.repository.TaskRepository;
import com.nirvanafire.ocadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskDTO create(Long userId, String username, TaskDTO dto) {
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .creatorId(userId)
                .creatorName(username)
                .assigneeId(dto.getAssigneeId())
                .assigneeName(dto.getAssigneeName())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority() != null ? dto.getPriority() : "NORMAL")
                .status("PENDING")
                .boardColumn("TODO")
                .build();
        task = taskRepository.save(task);
        return toDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO update(Long userId, Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new BusinessException("任务不存在"));
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        task = taskRepository.save(task);
        return toDTO(task);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new BusinessException("任务不存在"));
        if (!task.getCreatorId().equals(userId)) throw new BusinessException("无权操作");
        taskRepository.delete(task);
    }

    @Override
    public Page<TaskDTO> list(Long userId, String status, Pageable pageable) {
        Page<Task> page = taskRepository.findByCreatorIdOrAssigneeId(userId, userId, pageable);
        List<TaskDTO> list = page.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public TaskDTO get(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new BusinessException("任务不存在"));
        return toDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO moveBoard(Long userId, Long id, String column) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new BusinessException("任务不存在"));
        task.setBoardColumn(column);
        if ("DONE".equals(column)) task.setStatus("COMPLETED");
        task = taskRepository.save(task);
        return toDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO assign(Long userId, Long id, Long assigneeId, String assigneeName) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new BusinessException("任务不存在"));
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assigneeName);
        task = taskRepository.save(task);
        return toDTO(task);
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCreatorId(task.getCreatorId());
        dto.setCreatorName(task.getCreatorName());
        dto.setAssigneeId(task.getAssigneeId());
        dto.setAssigneeName(task.getAssigneeName());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setBoardColumn(task.getBoardColumn());
        return dto;
    }
}
