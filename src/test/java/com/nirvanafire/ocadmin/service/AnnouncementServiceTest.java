package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.AnnouncementCommentDTO;
import com.nirvanafire.ocadmin.dto.AnnouncementDTO;
import com.nirvanafire.ocadmin.entity.Announcement;
import com.nirvanafire.ocadmin.entity.AnnouncementComment;
import com.nirvanafire.ocadmin.enums.AnnouncementStatus;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.impl.AnnouncementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AnnouncementService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private AnnouncementReadRepository announcementReadRepository;
    @Mock
    private AnnouncementCommentRepository announcementCommentRepository;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    private Announcement testAnnouncement;

    @BeforeEach
    void setUp() {
        testAnnouncement = Announcement.builder()
                .id(1L)
                .title("测试公告")
                .content("测试内容")
                .announcementType(AnnouncementType.NOTICE)
                .status(AnnouncementStatus.DRAFT)
                .publisherId(1L)
                .publisherName("管理员")
                .isTop(false)
                .allowComment(true)
                .viewCount(0)
                .build();
    }

    @Test
    void create_Success() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("新公告");
        dto.setContent("内容");
        dto.setAnnouncementType(AnnouncementType.NEWS);

        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(invocation -> {
                    Announcement a = invocation.getArgument(0);
                    a.setId(1L);
                    return a;
                });

        AnnouncementDTO result = announcementService.create(1L, "admin", dto);

        assertNotNull(result);
        assertEquals("新公告", result.getTitle());
        assertEquals(AnnouncementStatus.DRAFT, result.getStatus());
        verify(announcementRepository).save(any(Announcement.class));
    }

    @Test
    void publish_Success() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        AnnouncementDTO result = announcementService.publish(1L, 1L);

        assertNotNull(result);
        assertEquals(AnnouncementStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublishedAt());
    }

    @Test
    void publish_NotOwner() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));

        assertThrows(BusinessException.class, () -> {
            announcementService.publish(2L, 1L);
        });
    }

    @Test
    void archive_Success() {
        testAnnouncement.setStatus(AnnouncementStatus.PUBLISHED);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        AnnouncementDTO result = announcementService.archive(1L, 1L);

        assertNotNull(result);
        assertEquals(AnnouncementStatus.ARCHIVED, result.getStatus());
    }

    @Test
    void setTop_Success() {
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        AnnouncementDTO result = announcementService.setTop(1L, 1L, true, null);

        assertNotNull(result);
        assertTrue(result.getIsTop());
    }

    @Test
    void getById_Success() {
        testAnnouncement.setStatus(AnnouncementStatus.PUBLISHED);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(announcementReadRepository.findByAnnouncementIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        AnnouncementDTO result = announcementService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.getViewCount());
    }

    @Test
    void markAsRead_Success() {
        when(announcementReadRepository.findByAnnouncementIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(announcementReadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> {
            announcementService.markAsRead(1L, 1L);
        });

        verify(announcementReadRepository).save(any());
    }

    @Test
    void markAsRead_AlreadyRead() {
        when(announcementReadRepository.findByAnnouncementIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(new AnnouncementRead()));

        assertDoesNotThrow(() -> {
            announcementService.markAsRead(1L, 1L);
        });

        verify(announcementReadRepository, never()).save(any());
    }

    @Test
    void addComment_Success() {
        testAnnouncement.setAllowComment(true);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));
        when(announcementCommentRepository.save(any(AnnouncementComment.class)))
                .thenAnswer(invocation -> {
                    AnnouncementComment c = invocation.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        AnnouncementCommentDTO dto = new AnnouncementCommentDTO();
        dto.setAnnouncementId(1L);
        dto.setContent("测试评论");

        AnnouncementCommentDTO result = announcementService.addComment(1L, "用户", dto);

        assertNotNull(result);
        assertEquals("测试评论", result.getContent());
    }

    @Test
    void addComment_NotAllowed() {
        testAnnouncement.setAllowComment(false);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(testAnnouncement));

        AnnouncementCommentDTO dto = new AnnouncementCommentDTO();
        dto.setAnnouncementId(1L);
        dto.setContent("测试评论");

        assertThrows(BusinessException.class, () -> {
            announcementService.addComment(1L, "用户", dto);
        });
    }

    @Test
    void deleteComment_Success() {
        AnnouncementComment comment = AnnouncementComment.builder()
                .id(1L)
                .userId(1L)
                .content("测试")
                .build();

        when(announcementCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> {
            announcementService.deleteComment(1L, 1L);
        });

        verify(announcementCommentRepository).delete(comment);
    }

    @Test
    void deleteComment_NotOwner() {
        AnnouncementComment comment = AnnouncementComment.builder()
                .id(1L)
                .userId(1L)
                .content("测试")
                .build();

        when(announcementCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(BusinessException.class, () -> {
            announcementService.deleteComment(2L, 1L);
        });
    }
}
