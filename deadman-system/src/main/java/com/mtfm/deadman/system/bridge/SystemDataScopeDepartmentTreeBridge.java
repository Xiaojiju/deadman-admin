package com.mtfm.deadman.system.bridge;

import com.mtfm.deadman.common.spi.DataScopeDepartmentTreeBridge;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.mapper.SysDepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限插件访问部门树的 system 侧实现。
 */
@Component
@RequiredArgsConstructor
public class SystemDataScopeDepartmentTreeBridge implements DataScopeDepartmentTreeBridge {

    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public Set<Long> resolveSelfAndDescendantIds(Long departmentId) {
        if (departmentId == null) {
            return Set.of();
        }
        List<SysDepartment> departments = sysDepartmentMapper.selectList(null);
        if (departments.isEmpty()) {
            return Set.of(departmentId);
        }
        Map<Long, List<SysDepartment>> childrenMap = departments.stream()
                .filter(dept -> dept.getParentId() != null)
                .collect(Collectors.groupingBy(SysDepartment::getParentId));
        Set<Long> result = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(departmentId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!result.add(current)) {
                continue;
            }
            List<SysDepartment> children = childrenMap.get(current);
            if (children == null) {
                continue;
            }
            for (SysDepartment child : children) {
                queue.add(child.getId());
            }
        }
        return result;
    }
}
