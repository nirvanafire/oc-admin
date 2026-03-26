package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.DocumentFolderDTO;
import com.nirvanafire.ocadmin.entity.DocumentFolder;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DocumentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentFolderRepository folderRepository;
    @Mock
    private DocumentFileRepository fileRepository;
    @Mock
    private DocumentVersionRepository versionRepository;
    @Mock
    private DocumentShareRepository shareRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private DocumentFolder testFolder;

    @BeforeEach
    void setUp() {
        testFolder = DocumentFolder.builder()
                .id(1L)
                .parentId(0L)
                .name("测试文件夹")
                .ownerId(1L)
                .ownerName("管理员")
                .build();
    }

    @Test
    void createFolder_Success() {
        when(folderRepository.existsByParentIdAndName(0L, "新文件夹")).thenReturn(false);
        when(folderRepository.save(any(DocumentFolder.class)))
                .thenAnswer(inv -> {
                    DocumentFolder f = inv.getArgument(0);
                    f.setId(1L);
                    return f;
                });

        DocumentFolderDTO result = documentService.createFolder(1L, "admin", 0L, "新文件夹");

        assertNotNull(result);
        assertEquals("新文件夹", result.getName());
    }

    @Test
    void createFolder_AlreadyExists() {
        when(folderRepository.existsByParentIdAndName(0L, "已存在")).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            documentService.createFolder(1L, "admin", 0L, "已存在");
        });
    }

    @Test
    void renameFolder_Success() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(folderRepository.save(any(DocumentFolder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DocumentFolderDTO result = documentService.renameFolder(1L, 1L, "新名称");

        assertNotNull(result);
        assertEquals("新名称", result.getName());
    }

    @Test
    void renameFolder_NotOwner() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));

        assertThrows(BusinessException.class, () -> {
            documentService.renameFolder(2L, 1L, "新名称");
        });
    }

    @Test
    void deleteFolder_Success() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(folderRepository.findByParentId(1L)).thenReturn(List.of());
        when(fileRepository.findByFolderIdAndIsLatestTrue(1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> {
            documentService.deleteFolder(1L, 1L);
        });

        verify(folderRepository).delete(testFolder);
    }

    @Test
    void deleteFolder_HasChildren() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(folderRepository.findByParentId(1L)).thenReturn(List.of(testFolder));

        assertThrows(BusinessException.class, () -> {
            documentService.deleteFolder(1L, 1L);
        });
    }

    @Test
    void moveFolder_Success() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(folderRepository.existsById(2L)).thenReturn(true);
        when(folderRepository.save(any(DocumentFolder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DocumentFolderDTO result = documentService.moveFolder(1L, 1L, 2L);

        assertNotNull(result);
        assertEquals(2L, result.getParentId());
    }
}
