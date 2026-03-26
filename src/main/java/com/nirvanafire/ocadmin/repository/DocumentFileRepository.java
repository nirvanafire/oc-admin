package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.DocumentFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
    
    List<DocumentFile> findByFolderIdAndIsLatestTrue(Long folderId);
    
    Page<DocumentFile> findByFolderId(Long folderId, Pageable pageable);
    
    Page<DocumentFile> findByOwnerId(Long ownerId, Pageable pageable);
    
    @Query("SELECT df FROM DocumentFile df WHERE df.isLatest = true AND (df.fileName LIKE %:keyword% OR df.tags LIKE %:keyword%)")
    Page<DocumentFile> search(@Param("keyword") String keyword, Pageable pageable);
    
    Optional<DocumentFile> findByFolderIdAndFileNameAndIsLatestTrue(Long folderId, String fileName);
    
    @Query("SELECT df FROM DocumentFile df WHERE df.folderId IN :folderIds AND df.isLatest = true")
    List<DocumentFile> findByFolderIds(@Param("folderIds") List<Long> folderIds);
}
