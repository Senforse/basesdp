package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.entity.SysDictItem;
import org.nmgyj.system.entity.SysDictType;
import org.nmgyj.system.mapper.SysDictItemMapper;
import org.nmgyj.system.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 数据字典服务，提供字典类型和字典项的CRUD操作。
 *
 * @author nmgyj
 * @since 2026-05-22
 */
@Service
public class SysDictService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictItemMapper sysDictItemMapper;

    public SysDictService(SysDictTypeMapper sysDictTypeMapper, SysDictItemMapper sysDictItemMapper) {
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictItemMapper = sysDictItemMapper;
    }

    // ==================== 字典类型操作 ====================

    /**
     * 查询所有字典类型。
     *
     * @return 字典类型列表
     */
    public List<SysDictType> listDictTypes() {
        return sysDictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .orderByAsc(SysDictType::getSortOrder));
    }

    /**
     * 查询启用的字典类型。
     *
     * @return 启用的字典类型列表
     */
    public List<SysDictType> listEnabledDictTypes() {
        return sysDictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, 1)
                .orderByAsc(SysDictType::getSortOrder));
    }

    /**
     * 根据ID查询字典类型。
     *
     * @param id 字典类型ID
     * @return 字典类型实体或null
     */
    public SysDictType getDictTypeById(Long id) {
        return sysDictTypeMapper.selectById(id);
    }

    /**
     * 根据编码查询字典类型。
     *
     * @param dictCode 字典编码
     * @return 字典类型实体或null
     */
    public SysDictType getDictTypeByCode(String dictCode) {
        return sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictCode, dictCode));
    }

    /**
     * 新增字典类型。
     *
     * @param dictType 字典类型实体
     */
    public void saveDictType(SysDictType dictType) {
        validateDictType(dictType, false);
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        if (dictType.getSortOrder() == null) {
            dictType.setSortOrder(0);
        }
        sysDictTypeMapper.insert(dictType);
    }

    /**
     * 更新字典类型。
     *
     * @param dictType 字典类型实体（含ID）
     */
    public void updateDictType(SysDictType dictType) {
        validateDictType(dictType, true);
        sysDictTypeMapper.updateById(dictType);
    }

    /**
     * 删除字典类型（存在字典项时不允许删除）。
     *
     * @param id 字典类型ID
     */
    public void deleteDictType(Long id) {
        long count = sysDictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictTypeId, id));
        if (count > 0) {
            throw new IllegalArgumentException("该字典类型下存在字典项，无法删除");
        }
        sysDictTypeMapper.deleteById(id);
    }

    // ==================== 字典项操作 ====================

    /**
     * 根据字典类型ID查询字典项列表。
     *
     * @param dictTypeId 字典类型ID
     * @return 字典项列表
     */
    public List<SysDictItem> listDictItemsByTypeId(Long dictTypeId) {
        return sysDictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictTypeId, dictTypeId)
                .orderByAsc(SysDictItem::getSortOrder));
    }

    /**
     * 根据字典类型ID查询启用的字典项列表。
     *
     * @param dictTypeId 字典类型ID
     * @return 启用的字典项列表
     */
    public List<SysDictItem> listEnabledDictItemsByTypeId(Long dictTypeId) {
        return sysDictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictTypeId, dictTypeId)
                .eq(SysDictItem::getStatus, 1)
                .orderByAsc(SysDictItem::getSortOrder));
    }

    /**
     * 根据字典编码查询启用的字典项列表。
     *
     * @param dictCode 字典编码
     * @return 启用的字典项列表
     */
    public List<SysDictItem> listEnabledDictItemsByCode(String dictCode) {
        SysDictType dictType = getDictTypeByCode(dictCode);
        if (dictType == null || dictType.getStatus() != 1) {
            return List.of();
        }
        return listEnabledDictItemsByTypeId(dictType.getId());
    }

    /**
     * 根据ID查询字典项。
     *
     * @param id 字典项ID
     * @return 字典项实体或null
     */
    public SysDictItem getDictItemById(Long id) {
        return sysDictItemMapper.selectById(id);
    }

    /**
     * 新增字典项。
     *
     * @param dictItem 字典项实体
     */
    public void saveDictItem(SysDictItem dictItem) {
        validateDictItem(dictItem, false);
        if (dictItem.getStatus() == null) {
            dictItem.setStatus(1);
        }
        if (dictItem.getSortOrder() == null) {
            dictItem.setSortOrder(0);
        }
        sysDictItemMapper.insert(dictItem);
    }

    /**
     * 更新字典项。
     *
     * @param dictItem 字典项实体（含ID）
     */
    public void updateDictItem(SysDictItem dictItem) {
        validateDictItem(dictItem, true);
        sysDictItemMapper.updateById(dictItem);
    }

    /**
     * 删除字典项。
     *
     * @param id 字典项ID
     */
    public void deleteDictItem(Long id) {
        sysDictItemMapper.deleteById(id);
    }

    // ==================== 验证方法 ====================

    private void validateDictType(SysDictType dictType, boolean requireId) {
        if (dictType == null) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        if (requireId && dictType.getId() == null) {
            throw new IllegalArgumentException("字典类型ID不能为空");
        }
        if (!StringUtils.hasText(dictType.getDictCode())) {
            throw new IllegalArgumentException("字典编码不能为空");
        }
        if (!StringUtils.hasText(dictType.getDictName())) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
        if (!dictType.getDictCode().matches("^[a-z][a-z0-9_]*$")) {
            throw new IllegalArgumentException("字典编码只能包含小写字母、数字和下划线，且以字母开头");
        }
        if (requireId) {
            SysDictType existing = getDictTypeByCode(dictType.getDictCode());
            if (existing != null && !existing.getId().equals(dictType.getId())) {
                throw new IllegalArgumentException("字典编码已存在");
            }
        } else {
            if (getDictTypeByCode(dictType.getDictCode()) != null) {
                throw new IllegalArgumentException("字典编码已存在");
            }
        }
    }

    private void validateDictItem(SysDictItem dictItem, boolean requireId) {
        if (dictItem == null) {
            throw new IllegalArgumentException("字典项不能为空");
        }
        if (requireId && dictItem.getId() == null) {
            throw new IllegalArgumentException("字典项ID不能为空");
        }
        if (dictItem.getDictTypeId() == null) {
            throw new IllegalArgumentException("所属字典类型ID不能为空");
        }
        if (!StringUtils.hasText(dictItem.getItemValue())) {
            throw new IllegalArgumentException("字典项值不能为空");
        }
        if (!StringUtils.hasText(dictItem.getItemLabel())) {
            throw new IllegalArgumentException("字典项标签不能为空");
        }
        SysDictType dictType = getDictTypeById(dictItem.getDictTypeId());
        if (dictType == null) {
            throw new IllegalArgumentException("所属字典类型不存在");
        }
        if (requireId) {
            SysDictItem existing = sysDictItemMapper.selectOne(new LambdaQueryWrapper<SysDictItem>()
                    .eq(SysDictItem::getDictTypeId, dictItem.getDictTypeId())
                    .eq(SysDictItem::getItemValue, dictItem.getItemValue()));
            if (existing != null && !existing.getId().equals(dictItem.getId())) {
                throw new IllegalArgumentException("同一字典类型下已存在相同的值");
            }
        } else {
            if (sysDictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                    .eq(SysDictItem::getDictTypeId, dictItem.getDictTypeId())
                    .eq(SysDictItem::getItemValue, dictItem.getItemValue())) > 0) {
                throw new IllegalArgumentException("同一字典类型下已存在相同的值");
            }
        }
    }
}