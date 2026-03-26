package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.Announcement;
import com.nirvanafire.ocadmin.enums.AnnouncementStatus;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    Page<Announcement> findByStatus(AnnouncementStatus status, Pageable pageable);
    
    Page<Announcement> findByAnnouncementType(AnnouncementType type, Pageable pageable);
    
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' AND (a.isTop = true AND a.topExpireTime > :now OR a.isTop = false) ORDER BY a.isTop DESC, a.publishedAt DESC")
    Page<Announcement> findPublishedOrderByTopAndTime(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' AND a.announcementType = :type ORDER BY a.isTop DESC, a.publishedAt DESC")
    Page<Announcement> findPublishedByType(@Param("type") AnnouncementType type, Pageable pageable);
    
    List<Announcement> findByIsTopTrueAndTopExpireTimeBefore(LocalDateTime time);
}
