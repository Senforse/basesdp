package org.nmgyj.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.order.entity.BussOrderInfo;
import org.nmgyj.order.mapper.BussOrderInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BussOrderInfo 的 CRUD 服务。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
@Service
public class BussOrderInfoService {

    private final BussOrderInfoMapper bussOrderInfoMapper;

    public BussOrderInfoService(BussOrderInfoMapper bussOrderInfoMapper) {
        this.bussOrderInfoMapper = bussOrderInfoMapper;
    }

    public List<BussOrderInfo> listAll() {
        return bussOrderInfoMapper.selectList(new LambdaQueryWrapper<BussOrderInfo>().orderByDesc(BussOrderInfo::getId));
    }

    public BussOrderInfo getById(Long id) {
        return bussOrderInfoMapper.selectById(id);
    }

    public void save(BussOrderInfo body) {
        bussOrderInfoMapper.insert(body);
    }

    public void update(BussOrderInfo body) {
        if (body.getId() == null) {
            throw new IllegalArgumentException("主键不能为空");
        }
        bussOrderInfoMapper.updateById(body);
    }

    public void delete(Long id) {
        bussOrderInfoMapper.deleteById(id);
    }
}
