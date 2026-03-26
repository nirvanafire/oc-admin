package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    
    List<DocumentVersion> findByFileIdOrderByVersionDesc(Long fileId);
    
    Optional<DocumentVersion> findByFileIdAndVersion(Long fileId, Integer version);
    
    Optional<DocumentVersion> findTopByFileIdOrderByVersionDesc(Long fileId);
}
