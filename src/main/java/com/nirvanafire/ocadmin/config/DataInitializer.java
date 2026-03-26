package com.nirvanafire.ocadmin.config;

import com.nirvanafire.ocadmin.entity.SysConfig;
import com.nirvanafire.ocadmin.entity.SysMenu;
import com.nirvanafire.ocadmin.entity.SysPermission;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.MenuRepository;
import com.nirvanafire.ocadmin.repository.PermissionRepository;
import com.nirvanafire.ocadmin.repository.SysConfigRepository;
import com.nirvanafire.ocadmin.repository.RoleRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final SysConfigRepository configRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("检查并初始化默认数据...");

        // 初始化权限
        initPermissions();

        // 初始化角色
        initRoles();

        // 初始化菜单
        initMenus();

        // 初始化管理员用户
        initAdminUser();

        // 初始化系统配置
        initConfigs();

        log.info("默认数据初始化完成");
    }

    private void initPermissions() {
        if (permissionRepository.count() > 0) {
            log.info("权限数据已存在，跳过初始化");
            return;
        }

        log.info("初始化权限数据...");

        Set<SysPermission> permissions = new HashSet<>();

        // 用户权限
        permissions.add(createPermission("user:create", "创建用户", "button"));
        permissions.add(createPermission("user:update", "修改用户", "button"));
        permissions.add(createPermission("user:delete", "删除用户", "button"));
        permissions.add(createPermission("user:view", "查看用户", "button"));
        permissions.add(createPermission("user:list", "用户列表", "button"));

        // 角色权限
        permissions.add(createPermission("role:create", "创建角色", "button"));
        permissions.add(createPermission("role:update", "修改角色", "button"));
        permissions.add(createPermission("role:delete", "删除角色", "button"));
        permissions.add(createPermission("role:view", "查看角色", "button"));
        permissions.add(createPermission("role:list", "角色列表", "button"));

        // 菜单权限
        permissions.add(createPermission("menu:create", "创建菜单", "button"));
        permissions.add(createPermission("menu:update", "修改菜单", "button"));
        permissions.add(createPermission("menu:delete", "删除菜单", "button"));
        permissions.add(createPermission("menu:view", "查看菜单", "button"));
        permissions.add(createPermission("menu:list", "菜单列表", "button"));

        // 工作流权限
        permissions.add(createPermission("workflow:list", "流程列表", "button"));
        permissions.add(createPermission("workflow:deploy", "部署流程", "button"));
        permissions.add(createPermission("workflow:delete", "删除流程", "button"));
        permissions.add(createPermission("workflow:request", "提交申请", "button"));
        permissions.add(createPermission("workflow:approve", "审核任务", "button"));

        // 系统配置权限
        permissions.add(createPermission("config:list", "配置列表", "button"));
        permissions.add(createPermission("config:update", "修改配置", "button"));

        permissionRepository.saveAll(permissions);
    }

    private SysPermission createPermission(String code, String name, String type) {
        return SysPermission.builder()
                .code(code)
                .name(name)
                .permissionType(type)
                .build();
    }

    private void initRoles() {
        if (roleRepository.count() > 0) {
            log.info("角色数据已存在，跳过初始化");
            return;
        }

        log.info("初始化角色数据...");

        Set<SysPermission> allPermissions = new HashSet<>(permissionRepository.findAll());

        // 超级管理员角色
        SysRole adminRole = SysRole.builder()
                .code("admin")
                .name("超级管理员")
                .description("拥有所有权限")
                .roleSort(1)
                .enabled(true)
                .permissions(allPermissions)
                .build();

        // 普通用户角色
        SysRole userRole = SysRole.builder()
                .code("user")
                .name("普通用户")
                .description("普通用户权限")
                .roleSort(2)
                .enabled(true)
                .build();

        roleRepository.save(adminRole);
        roleRepository.save(userRole);
    }

    private void initMenus() {
        if (menuRepository.count() > 0) {
            log.info("菜单数据已存在，跳过初始化");
            return;
        }

        log.info("初始化菜单数据...");

        // 系统管理目录
        SysMenu systemDir = SysMenu.builder()
                .name("系统管理")
                .path("/system")
                .component("Layout")
                .menuType("directory")
                .icon("Setting")
                .parentId(0L)
                .menuSort(1)
                .visible("1")
                .build();
        systemDir = menuRepository.save(systemDir);

        // 用户管理
        SysMenu userMenu = SysMenu.builder()
                .name("用户管理")
                .path("/system/users")
                .component("/system/users/index")
                .menuType("menu")
                .icon("User")
                .parentId(systemDir.getId())
                .menuSort(1)
                .visible("1")
                .build();
        menuRepository.save(userMenu);

        // 角色管理
        SysMenu roleMenu = SysMenu.builder()
                .name("角色管理")
                .path("/system/roles")
                .component("/system/roles/index")
                .menuType("menu")
                .icon("UserFilled")
                .parentId(systemDir.getId())
                .menuSort(2)
                .visible("1")
                .build();
        menuRepository.save(roleMenu);

        // 菜单管理
        SysMenu menuManage = SysMenu.builder()
                .name("菜单管理")
                .path("/system/menus")
                .component("/system/menus/index")
                .menuType("menu")
                .icon("Menu")
                .parentId(systemDir.getId())
                .menuSort(3)
                .visible("1")
                .build();
        menuRepository.save(menuManage);

        // 系统配置
        SysMenu configMenu = SysMenu.builder()
                .name("系统配置")
                .path("/system/configs")
                .component("/system/configs/index")
                .menuType("menu")
                .icon("Tools")
                .parentId(systemDir.getId())
                .menuSort(4)
                .visible("1")
                .build();
        menuRepository.save(configMenu);

        // 工作流管理目录
        SysMenu workflowDir = SysMenu.builder()
                .name("工作流管理")
                .path("/workflow")
                .component("Layout")
                .menuType("directory")
                .icon("DocumentCopy")
                .parentId(0L)
                .menuSort(2)
                .visible("1")
                .build();
        workflowDir = menuRepository.save(workflowDir);

        // 流程管理
        SysMenu processMenu = SysMenu.builder()
                .name("流程管理")
                .path("/workflow/processes")
                .component("/workflow/processes/index")
                .menuType("menu")
                .icon("Document")
                .parentId(workflowDir.getId())
                .menuSort(1)
                .visible("1")
                .build();
        menuRepository.save(processMenu);

        // 流程设计
        SysMenu designMenu = SysMenu.builder()
                .name("流程设计")
                .path("/workflow/processes/design")
                .component("/workflow/processes/design")
                .menuType("menu")
                .icon("Edit")
                .parentId(workflowDir.getId())
                .menuSort(2)
                .visible("1")
                .build();
        menuRepository.save(designMenu);

        // 我的申请
        SysMenu requestMenu = SysMenu.builder()
                .name("我的申请")
                .path("/workflow/requests")
                .component("/workflow/requests/index")
                .menuType("menu")
                .icon("List")
                .parentId(workflowDir.getId())
                .menuSort(3)
                .visible("1")
                .build();
        menuRepository.save(requestMenu);

        // 待审核任务
        SysMenu taskMenu = SysMenu.builder()
                .name("待审核任务")
                .path("/workflow/tasks")
                .component("/workflow/tasks/index")
                .menuType("menu")
                .icon("Check")
                .parentId(workflowDir.getId())
                .menuSort(4)
                .visible("1")
                .build();
        menuRepository.save(taskMenu);
    }

    private void initAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("管理员用户已存在，跳过初始化");
            return;
        }

        log.info("初始化管理员用户...");

        SysRole adminRole = roleRepository.findByCode("admin")
                .orElseThrow(() -> new RuntimeException("管理员角色不存在，请先初始化角色数据"));

        Set<SysRole> roles = new HashSet<>();
        roles.add(adminRole);

        SysUser adminUser = SysUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("系统管理员")
                .email("admin@example.com")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();

        userRepository.save(adminUser);
    }

    private void initConfigs() {
        if (configRepository.count() > 0) {
            log.info("系统配置数据已存在，跳过初始化");
            return;
        }

        log.info("初始化系统配置...");

        // 存储配置
        configRepository.save(SysConfig.builder().configKey("storage.type").configValue("rustfs").description("存储类型（rustfs/oss）").build());
        configRepository.save(SysConfig.builder().configKey("storage.rustfs.endpoint").configValue("http://192.168.1.100:9000").description("RustFS S3兼容接口地址").build());
        configRepository.save(SysConfig.builder().configKey("storage.rustfs.bucket").configValue("oc-admin").description("RustFS Bucket名称").build());
        configRepository.save(SysConfig.builder().configKey("storage.rustfs.access-key").configValue("rustfsadmin").description("RustFS Access Key").build());
        configRepository.save(SysConfig.builder().configKey("storage.rustfs.secret-key").configValue("rustfssecret").description("RustFS Secret Key").build());
        configRepository.save(SysConfig.builder().configKey("storage.oss.endpoint").configValue("").description("阿里云OSS Endpoint").build());
        configRepository.save(SysConfig.builder().configKey("storage.oss.bucket").configValue("").description("阿里云OSS Bucket").build());
        configRepository.save(SysConfig.builder().configKey("storage.oss.access-key").configValue("").description("阿里云OSS AccessKey").build());
        configRepository.save(SysConfig.builder().configKey("storage.oss.secret-key").configValue("").description("阿里云OSS SecretKey").build());

        // 水印配置
        configRepository.save(SysConfig.builder().configKey("watermark.enabled").configValue("true").description("是否启用水印").build());
        configRepository.save(SysConfig.builder().configKey("watermark.text").configValue("oc-admin").description("水印文字").build());
    }
}
