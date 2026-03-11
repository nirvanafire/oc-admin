package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<SysMenu, Long> {
    List<SysMenu> findByParentIdOrderByMenuSort(Long parentId);
    List<SysMenu> findByVisibleOrderByMenuSort(String visible);
}
