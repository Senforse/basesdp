package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.entity.SysOrganization;
import org.nmgyj.system.mapper.SysOrganizationMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织架构树的查询与 CRUD。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class SysOrganizationService {

    private final SysOrganizationMapper sysOrganizationMapper;

    /**
     * @param sysOrganizationMapper 组织 Mapper
     */
    public SysOrganizationService(SysOrganizationMapper sysOrganizationMapper) {
        this.sysOrganizationMapper = sysOrganizationMapper;
    }

    /**
     * 查询组织树。
     *
     * @return 根组织列表（含 children）
     */
    public List<SysOrganization> tree() {
        List<SysOrganization> all = sysOrganizationMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                .orderByAsc(SysOrganization::getSortOrder));
        return buildTree(all);
    }

    private List<SysOrganization> buildTree(List<SysOrganization> flat) {
        Map<Long, List<SysOrganization>> byParent = flat.stream()
                .collect(Collectors.groupingBy(o -> o.getParentId() == null ? 0L : o.getParentId()));
        byParent.values().forEach(list ->
                list.sort(Comparator.comparing(SysOrganization::getSortOrder, Comparator.nullsLast(Integer::compareTo))));
        return buildLevel(0L, byParent);
    }

    private List<SysOrganization> buildLevel(Long parentId, Map<Long, List<SysOrganization>> byParent) {
        List<SysOrganization> level = new ArrayList<>(byParent.getOrDefault(parentId, List.of()));
        for (SysOrganization org : level) {
            org.setChildren(buildLevel(org.getId(), byParent));
        }
        return level;
    }

    /**
     * 按主键查询组织。
     *
     * @param id 组织主键
     * @return 实体或 null
     */
    public SysOrganization get(Long id) {
        return sysOrganizationMapper.selectById(id);
    }

    /**
     * 新增组织。
     *
     * @param org 组织实体
     * @throws IllegalArgumentException 编码或名称为空
     */
    public void save(SysOrganization org) {
        validate(org);
        if (org.getParentId() == null) {
            org.setParentId(0L);
        }
        if (org.getSortOrder() == null) {
            org.setSortOrder(0);
        }
        if (org.getStatus() == null) {
            org.setStatus(1);
        }
        sysOrganizationMapper.insert(org);
    }

    /**
     * 更新组织。
     *
     * @param org 含主键的实体
     * @throws IllegalArgumentException 编码或名称为空
     */
    public void update(SysOrganization org) {
        validate(org);
        sysOrganizationMapper.updateById(org);
    }

    /**
     * 删除组织。
     *
     * @param id 组织主键
     * @throws IllegalArgumentException 存在子组织时
     */
    public void delete(Long id) {
        long cnt = sysOrganizationMapper.selectCount(new LambdaQueryWrapper<SysOrganization>().eq(SysOrganization::getParentId, id));
        if (cnt > 0) {
            throw new IllegalArgumentException("存在子组织，无法删除");
        }
        sysOrganizationMapper.deleteById(id);
    }

    private void validate(SysOrganization org) {
        if (!StringUtils.hasText(org.getOrgCode()) || !StringUtils.hasText(org.getOrgName())) {
            throw new IllegalArgumentException("组织编码与名称不能为空");
        }
    }
}
