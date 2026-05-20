package org.nmgyj.system.service;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.constant.OnlineSessionConstants;
import org.nmgyj.system.dto.OnlineUserPageResult;
import org.nmgyj.system.dto.OnlineUserVO;
import org.nmgyj.system.entity.SysRole;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.entity.SysUserRole;
import org.nmgyj.system.mapper.SysRoleMapper;
import org.nmgyj.system.mapper.SysUserMapper;
import org.nmgyj.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 在线用户查询与强制下线。
 * <p>
 * 会话数据并非在本类中直连 RedisTemplate 写入；登录时由 {@link org.nmgyj.authentication.service.AuthService}
 * 调用 Sa-Token，经全局 {@link SaTokenDao}（启用 {@code sa-token-redis-jackson} 时为 Redis）持久化 Token 与 Token-Session。
 * 本服务通过注入的 {@link SaTokenDao} 与 {@link StpUtil} 读取、下线同一存储中的会话。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class OnlineUserAdminService {

    private static final DateTimeFormatter EXPIRE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SaTokenDao saTokenDao;

    /**
     * @param sysUserMapper       用户 Mapper
     * @param sysUserRoleMapper   用户角色 Mapper
     * @param sysRoleMapper       角色 Mapper
     * @param saTokenDao          Sa-Token 持久层（Redis 等），与登录会话共用
     */
    public OnlineUserAdminService(
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper,
            SysRoleMapper sysRoleMapper,
            SaTokenDao saTokenDao) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.saTokenDao = saTokenDao;
    }

    /**
     * 分页查询在线会话。
     *
     * @param applicationFilter 应用编码，空表示不限
     * @param userCodeFilter    用户名模糊匹配（忽略大小写），空表示不限
     * @param page              页码从 1 开始
     * @param size              每页条数，最大 100
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    public OnlineUserPageResult page(String applicationFilter, String userCodeFilter, long page, long size) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        long safePage = Math.max(page, 1);

        List<String> tokens = collectOnlineTokenValues();

        String appNeedle = StringUtils.hasText(applicationFilter) ? applicationFilter.trim() : "";
        String userNeedle = StringUtils.hasText(userCodeFilter) ? userCodeFilter.trim().toLowerCase(Locale.ROOT) : "";

        List<OnlineUserVO> candidates = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();

        for (String token : tokens) {
            Long parsedUid = parseLoginIdAsLong(StpUtil.getLoginIdByToken(token));
            if (parsedUid == null) {
                continue;
            }
            long uid = parsedUid;

            SaSession ts = StpUtil.getTokenSessionByToken(token);
            String app = OnlineSessionConstants.DEFAULT_APPLICATION;
            String authType = "password";
            if (ts != null) {
                Object av = ts.get(OnlineSessionConstants.APPLICATION);
                if (av != null && StringUtils.hasText(String.valueOf(av))) {
                    app = String.valueOf(av).trim();
                }
                Object at = ts.get(OnlineSessionConstants.AUTH_TYPE);
                if (at != null && StringUtils.hasText(String.valueOf(at))) {
                    authType = String.valueOf(at).trim();
                }
            }

            if (StringUtils.hasText(appNeedle) && !appNeedle.equals(app)) {
                continue;
            }

            long ttl = saTokenDao.getTimeout(StpUtil.getStpLogic().splicingKeyTokenValue(token));

            OnlineUserVO row = new OnlineUserVO();
            row.setTokenValue(token);
            row.setApplication(app);
            row.setAuthType(authType);
            if (ttl == SaTokenDao.NOT_VALUE_EXPIRE) {
                // 个别存储后端未返回 TTL 时会话仍可能有效，避免整表被过滤为空
                row.setExpireTime("—");
            } else {
                row.setExpireTime(formatExpire(ttl));
            }

            candidates.add(row);
            userIds.add(uid);
        }

        Map<Long, SysUser> userMap = loadUsers(userIds);
        Map<Long, String> roleNameByUser = primaryRoleNameByUserId(userIds);

        List<OnlineUserVO> filtered = new ArrayList<>();
        for (OnlineUserVO row : candidates) {
            Long parsedUid = parseLoginIdAsLong(StpUtil.getLoginIdByToken(row.getTokenValue()));
            if (parsedUid == null) {
                continue;
            }
            long uid = parsedUid;
            SysUser u = userMap.get(uid);
            String username = u != null && StringUtils.hasText(u.getUsername()) ? u.getUsername() : String.valueOf(uid);
            if (StringUtils.hasText(userNeedle) && !username.toLowerCase(Locale.ROOT).contains(userNeedle)) {
                continue;
            }
            row.setUsername(username);
            row.setDisplayName(u != null && StringUtils.hasText(u.getDisplayName()) ? u.getDisplayName() : username);
            row.setUserType(roleNameByUser.getOrDefault(uid, "-"));
            filtered.add(row);
        }

        filtered.sort(Comparator.comparing(OnlineUserVO::getExpireTime, Comparator.nullsLast(String::compareTo)));

        long total = filtered.size();
        int from = (int) ((safePage - 1) * safeSize);
        List<OnlineUserVO> pageRecords =
                from >= filtered.size() ? List.of() : filtered.subList(from, (int) Math.min(from + safeSize, filtered.size()));

        OnlineUserPageResult result = new OnlineUserPageResult();
        result.setRecords(pageRecords);
        result.setTotal(total);
        result.setCurrent(safePage);
        result.setSize(safeSize);
        return result;
    }

    /**
     * 注销指定令牌对应的会话。
     *
     * @param tokenValue 目标 token
     */
    public void kickByToken(String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            throw new IllegalArgumentException("token 不能为空");
        }
        String cur = StpUtil.getTokenValue();
        if (tokenValue.equals(cur)) {
            throw new IllegalArgumentException("不能下线当前登录会话");
        }
        if (StpUtil.getLoginIdByToken(tokenValue) == null) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        StpUtil.logoutByTokenValue(tokenValue);
    }

    /**
     * 枚举会话 token：合并 {@link SaTokenDao#searchData}（依赖 Redis KEYS，返回内容未必是纯 token）与
     * 按用户 {@link StpUtil#getTokenValueListByLoginId}（走账号 Session，不依赖全局扫描）。
     * 二者合并去重，避免「搜索有脏数据就不再枚举」导致列表恒为空。
     */
    private List<String> collectOnlineTokenValues() {
        LinkedHashSet<String> tokenSet = new LinkedHashSet<>();
        String tokenKeyPrefix = StpUtil.getStpLogic().splicingKeyTokenValue("");
        List<?> rawTokens = saTokenDao.searchData(tokenKeyPrefix, "", 0, -1, false);
        for (Object o : rawTokens) {
            if (o instanceof String s && StringUtils.hasText(s)) {
                tokenSet.add(s);
            }
        }
        // 与登录校验一致：status 为 null 或 1 视为可登录用户，否则枚举不到其会话
        List<SysUser> loginEligibleUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.isNull(SysUser::getStatus).or().eq(SysUser::getStatus, 1)));
        for (SysUser u : loginEligibleUsers) {
            if (u.getId() == null) {
                continue;
            }
            List<?> byLogin = StpUtil.getTokenValueListByLoginId(u.getId());
            for (Object o : byLogin) {
                if (o instanceof String s && StringUtils.hasText(s)) {
                    tokenSet.add(s);
                }
            }
        }
        return new ArrayList<>(tokenSet);
    }

    /**
     * Sa-Token 在 Redis-Jackson 等场景下可能将 loginId 反序列化为 {@link String}，不能只判断 {@link Number}。
     *
     * @param loginIdObj getLoginIdByToken 返回值
     * @return 可关联 {@link SysUser} 主键的数值 id；无法解析时返回 null
     */
    private static Long parseLoginIdAsLong(Object loginIdObj) {
        if (loginIdObj == null) {
            return null;
        }
        if (loginIdObj instanceof Number n) {
            return n.longValue();
        }
        if (loginIdObj instanceof String s && StringUtils.hasText(s)) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static String formatExpire(long ttlSeconds) {
        if (ttlSeconds == SaTokenDao.NEVER_EXPIRE) {
            return "永不过期";
        }
        return LocalDateTime.now().plusSeconds(ttlSeconds).format(EXPIRE_FMT);
    }

    private Map<Long, SysUser> loadUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUser> list = sysUserMapper.selectBatchIds(userIds);
        Map<Long, SysUser> map = new HashMap<>(list.size());
        for (SysUser u : list) {
            if (u.getId() != null) {
                map.put(u.getId(), u);
            }
        }
        return map;
    }

    private Map<Long, String> primaryRoleNameByUserId(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> links =
                sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        Map<Long, Long> userToFirstRole = new LinkedHashMap<>();
        for (SysUserRole link : links) {
            userToFirstRole.putIfAbsent(link.getUserId(), link.getRoleId());
        }
        if (userToFirstRole.isEmpty()) {
            Map<Long, String> empty = new HashMap<>();
            for (Long uid : userIds) {
                empty.put(uid, "-");
            }
            return empty;
        }
        Set<Long> roleIds = new HashSet<>(userToFirstRole.values());
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        Map<Long, String> roleIdToName = new HashMap<>();
        for (SysRole r : roles) {
            if (r.getId() != null) {
                roleIdToName.put(r.getId(), StringUtils.hasText(r.getRoleName()) ? r.getRoleName() : "-");
            }
        }
        Map<Long, String> out = new HashMap<>();
        for (Long uid : userIds) {
            Long rid = userToFirstRole.get(uid);
            out.put(uid, rid == null ? "-" : roleIdToName.getOrDefault(rid, "-"));
        }
        return out;
    }
}
