package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
    
    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
    
    @Query("SELECT ar.announcementId FROM AnnouncementRead ar WHERE ar.userId = :userId")
    List<Long> findAnnouncementIdsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ar) FROM AnnouncementRead ar WHERE ar.announcementId = :announcementId")
    Long countByAnnouncementId(@Param("announcementId") Long announcementId);
}
