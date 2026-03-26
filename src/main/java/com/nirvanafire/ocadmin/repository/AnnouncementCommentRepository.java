package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AnnouncementComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementCommentRepository extends JpaRepository<AnnouncementComment, Long> {
    
    Page<AnnouncementComment> findByAnnouncementIdOrderByCreatedAtDesc(Long announcementId, Pageable pageable);
    
    Page<AnnouncementComment> findByAnnouncementIdAndParentIdOrderByCreatedAtAsc(Long announcementId, Long parentId, Pageable pageable);
    
    Long countByAnnouncementId(Long announcementId);
}
