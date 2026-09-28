package com.mtfm.deadman.system.domain.dict;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mtfm.deadman.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import com.mtfm.deadman.system.support.SystemErrorCodes;

/**
 * 字典层数：全局最多 3 级，组可以更浅。
 */
class DictDepthTest {

    @Test
    void rootFitsEveryGroupDepth() {
        assertThat(DictDepth.levelOf(null, 1)).isEqualTo(1);
        assertThat(DictDepth.levelOf(null, 3)).isEqualTo(1);
    }

    @Test
    void childCannotExceedGroupDepth() {
        assertThat(DictDepth.levelOf(1, 2)).isEqualTo(2);
        assertThatThrownBy(() -> DictDepth.levelOf(1, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(SystemErrorCodes.DICT_LEVEL_EXCEEDED);
        assertThatThrownBy(() -> DictDepth.levelOf(3, 3))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(SystemErrorCodes.DICT_LEVEL_EXCEEDED);
    }

    @Test
    void movingSubtreeMustStayInsideGroupDepth() {
        Map<Long, List<Long>> children = Map.of(1L, List.of(2L), 2L, List.of(3L));
        assertThat(DictDepth.subtreeSpan(1L, children)).isEqualTo(3);
        DictDepth.requireMoveFits(1, 3, 3);
        assertThatThrownBy(() -> DictDepth.requireMoveFits(2, 3, 3))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(SystemErrorCodes.DICT_LEVEL_EXCEEDED);
    }

    @Test
    void groupDepthMustBeOneToThree() {
        DictDepth.requireGroupMaxLevel(1);
        DictDepth.requireGroupMaxLevel(3);
        assertThatThrownBy(() -> DictDepth.requireGroupMaxLevel(0))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(SystemErrorCodes.DICT_LEVEL_INVALID);
        assertThatThrownBy(() -> DictDepth.requireGroupMaxLevel(4))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(SystemErrorCodes.DICT_LEVEL_INVALID);
    }
}
