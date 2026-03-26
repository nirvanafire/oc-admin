package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.DocumentFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentFolderRepository extends JpaRepository<DocumentFolder, Long> {
    
    List<DocumentFolder> findByParentIdOrderByNameAsc(Long parentId);
    
    List<DocumentFolder> findByOwnerId(Long ownerId);
    
    @Query("SELECT df FROM DocumentFolder df WHERE df.isPublic = true OR df.ownerId = :userId")
    List<DocumentFolder> findAllAccessible(@Param("userId") Long userId);
    
    boolean existsByParentIdAndName(Long parentId, String name);
}
