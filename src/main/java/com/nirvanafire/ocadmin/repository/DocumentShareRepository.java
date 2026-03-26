package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {
    
    List<DocumentShare> findByFileId(Long fileId);
    
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.fileId = :fileId AND (ds.shareType = 'PUBLIC' OR ds.targetId = :userId OR ds.creatorId = :userId)")
    List<DocumentShare> findAccessible(@Param("fileId") Long fileId, @Param("userId") Long userId);
    
    void deleteByFileId(Long fileId);
}
