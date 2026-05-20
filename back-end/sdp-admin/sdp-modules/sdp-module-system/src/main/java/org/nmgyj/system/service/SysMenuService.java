package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.dto.MenuMetaVO;
import org.nmgyj.system.dto.MenuRouteVO;
import org.nmgyj.system.entity.SysMenu;
import org.nmgyj.system.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单查询、路由树构建及后台菜单维护。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final RbacPermissionService rbacPermissionService;

    /**
     * @param sysMenuMapper           菜单 Mapper
     * @param rbacPermissionService   RBAC 权限计算
     */
    public SysMenuService(SysMenuMapper sysMenuMapper, RbacPermissionService rbacPermissionService) {
        this.sysMenuMapper = sysMenuMapper;
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * 按用户权限过滤并组装前端路由森林。
     *
     * @param userId 用户主键
     * @return 路由根节点列表
     */
    public List<MenuRouteVO> buildUserMenuRoutes(Long userId) {
        Set<Long> allowed = rbacPermissionService.collectAllowedMenuIdsWithAncestors(userId);
        List<SysMenu> candidates = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .eq(SysMenu::getVisible, 1)
                .in(SysMenu::getMenuType, 0, 1)
                .orderByAsc(SysMenu::getSortOrder));
        List<SysMenu> filtered = candidates.stream().filter(m -> allowed.contains(m.getId())).toList();
        return buildRouteForest(filtered);
    }

    /**
     * 后台列表：全部菜单扁平排序。
     *
     * @return 菜单列表
     */
    public List<SysMenu> listAdminFlat() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
    }

    /**
     * 预览全部启用目录/菜单的路由树（不受用户过滤）。
     *
     * @return 路由根节点列表
     */
    public List<MenuRouteVO> buildAdminRoutePreview() {
        List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .in(SysMenu::getMenuType, 0, 1)
                .orderByAsc(SysMenu::getSortOrder));
        return buildRouteForest(menus);
    }

    private List<MenuRouteVO> buildRouteForest(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> byParent = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        byParent.values().forEach(list ->
                list.sort(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo))));
        return buildMenuLevel(0L, byParent);
    }

    private List<MenuRouteVO> buildMenuLevel(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<SysMenu> level = new ArrayList<>(byParent.getOrDefault(parentId, List.of()));
        List<MenuRouteVO> out = new ArrayList<>();
        for (SysMenu m : level) {
            MenuRouteVO vo = new MenuRouteVO();
            vo.setPath(m.getPath());
            vo.setComponent(StringUtils.hasText(m.getComponent()) ? m.getComponent() : null);
            vo.setMeta(new MenuMetaVO(m.getMenuName(), StringUtils.hasText(m.getIcon()) ? m.getIcon() : "Menu"));
            vo.setChildren(buildMenuLevel(m.getId(), byParent));
            out.add(vo);
        }
        return out;
    }

    /**
     * 按主键查询菜单。
     *
     * @param id 菜单主键
     * @return 实体或 null
     */
    public SysMenu getById(Long id) {
        return sysMenuMapper.selectById(id);
    }

    /**
     * 新增菜单并填充默认值。
     *
     * @param menu 菜单实体
     */
    public void save(SysMenu menu) {
        validateForPersist(menu, false);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        sysMenuMapper.insert(menu);
    }

    /**
     * 更新菜单。
     *
     * @param menu 含主键的实体
     */
    public void update(SysMenu menu) {
        validateForPersist(menu, true);
        sysMenuMapper.updateById(menu);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单主键
     * @throws IllegalArgumentException 存在子菜单时
     */
    public void delete(Long id) {
        long cnt = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (cnt > 0) {
            throw new IllegalArgumentException("存在子菜单，无法删除");
        }
        sysMenuMapper.deleteById(id);
    }

    /**
     * 构建后台菜单实体树（含 children）。
     *
     * @return 根节点列表
     */
    public List<SysMenu> buildAdminEntityTree() {
        List<SysMenu> all = listAdminFlat();
        Map<Long, List<SysMenu>> byParent = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        byParent.values().forEach(list ->
                list.sort(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo))));
        return buildMenuEntityLevel(0L, byParent);
    }

    private List<SysMenu> buildMenuEntityLevel(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<SysMenu> level = new ArrayList<>(byParent.getOrDefault(parentId, List.of()));
        for (SysMenu m : level) {
            m.setChildren(buildMenuEntityLevel(m.getId(), byParent));
        }
        return level;
    }

    private void validateForPersist(SysMenu menu, boolean requireId) {
        if (menu == null) {
            throw new IllegalArgumentException("菜单不能为空");
        }
        if (requireId && menu.getId() == null) {
            throw new IllegalArgumentException("菜单 ID 不能为空");
        }
        if (!StringUtils.hasText(menu.getMenuName())) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }
        Integer menuType = menu.getMenuType();
        if (menuType == null || (menuType != 0 && menuType != 1 && menuType != 2)) {
            throw new IllegalArgumentException("菜单类型不合法，仅支持 0/1/2");
        }
        String path = menu.getPath() == null ? "" : menu.getPath().trim();
        String component = menu.getComponent() == null ? "" : menu.getComponent().trim();

        if (menuType == 0 || menuType == 1) {
            if (!StringUtils.hasText(path) || !path.startsWith("/")) {
                throw new IllegalArgumentException("目录/菜单的路由路径必须以 / 开头");
            }
        }
        if (menuType == 1) {
            if (!StringUtils.hasText(component)) {
                throw new IllegalArgumentException("菜单类型为“菜单”时，component 不能为空");
            }
            if (!isValidComponentPattern(component)) {
                throw new IllegalArgumentException("component 格式不合法，请使用页面名（如 BussOrderInfo）或 @/views/... 路径");
            }
        }
        if (requireId && menu.getParentId() != null && menu.getId().equals(menu.getParentId())) {
            throw new IllegalArgumentException("菜单不能设置自己为父节点");
        }
    }

    private boolean isValidComponentPattern(String component) {
        String value = component.trim();
        return value.matches("^[A-Za-z][A-Za-z0-9_]*$")
                || value.matches("^@/views/[A-Za-z0-9/_-]+(?:\\.vue)?$")
                || value.matches("^[A-Za-z0-9/_-]+(?:\\.vue)?$");
    }
}
