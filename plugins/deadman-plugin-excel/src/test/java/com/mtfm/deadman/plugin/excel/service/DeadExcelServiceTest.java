package com.mtfm.deadman.plugin.excel.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtfm.deadman.plugin.excel.annotation.DeadExcelColumn;
import com.mtfm.deadman.plugin.excel.column.DeadExcelColumns;
import com.mtfm.deadman.plugin.excel.export.DeadExcelExporter;
import com.mtfm.deadman.plugin.excel.model.DeadExcelReadOptions;
import com.mtfm.deadman.plugin.excel.model.DeadExcelWriteOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Excel 导入导出服务测试。
 */
class DeadExcelServiceTest {

    private DeadExcelService deadExcelService;

    @BeforeEach
    void setUp() {
        deadExcelService = new DefaultDeadExcelService();
    }

    @Test
    void shouldImportAndExportPojo() {
        List<UserRow> source =
                List.of(
                        new UserRow("U001", "张三", 1),
                        new UserRow("U002", "李四", 0));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        deadExcelService.export(output, UserRow.class, source);

        List<UserRow> imported =
                deadExcelService.importList(new ByteArrayInputStream(output.toByteArray()), UserRow.class);

        assertThat(imported).hasSize(2);
        assertThat(imported.get(0).getUserCode()).isEqualTo("U001");
        assertThat(imported.get(1).getNickname()).isEqualTo("李四");
    }

    @Test
    void shouldImportAndExportRecord() {
        List<UserRecord> source =
                List.of(
                        new UserRecord("R001", "王五", LocalDateTime.of(2026, 6, 7, 10, 0)),
                        new UserRecord("R002", "赵六", LocalDateTime.of(2026, 6, 8, 11, 30)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        deadExcelService.export(output, UserRecord.class, source);

        List<UserRecord> imported =
                deadExcelService.importList(new ByteArrayInputStream(output.toByteArray()), UserRecord.class);

        assertThat(imported).hasSize(2);
        assertThat(imported.get(0).userCode()).isEqualTo("R001");
        assertThat(imported.get(1).createTime()).isEqualTo(LocalDateTime.of(2026, 6, 8, 11, 30));
    }

    @Test
    void shouldExportWithCustomColumns() {
        List<UserRow> source = List.of(new UserRow("U100", "测试用户", 1));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DeadExcelExporter.of(deadExcelService, UserRow.class)
                .data(source)
                .sheetName("用户导出")
                .columns(
                        columns ->
                                columns
                                        .only("userCode", "nickname")
                                        .header("userCode", "用户编码")
                                        .header("nickname", "昵称")
                                        .column("statusLabel", "状态", row -> "启用"))
                .write(output);

        List<UserRow> imported =
                deadExcelService.importList(
                        new ByteArrayInputStream(output.toByteArray()),
                        UserRow.class,
                        DeadExcelReadOptions.builder().matchByHeader(true).build());

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getUserCode()).isEqualTo("U100");
        assertThat(imported.get(0).getNickname()).isEqualTo("测试用户");
    }

    @Test
    void shouldExportWithWriteOptionsColumns() {
        List<UserRow> source = List.of(new UserRow("U200", "选项导出", 0));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        deadExcelService.export(
                output,
                UserRow.class,
                source,
                DeadExcelWriteOptions.builder()
                        .sheetName("自定义")
                        .columns(
                                DeadExcelColumns.from(UserRow.class)
                                        .exclude("status")
                                        .header("nickname", "用户昵称")
                                        .build())
                        .build());

        List<UserRow> imported =
                deadExcelService.importList(new ByteArrayInputStream(output.toByteArray()), UserRow.class);

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getUserCode()).isEqualTo("U200");
        assertThat(imported.get(0).getNickname()).isEqualTo("选项导出");
    }

    /**
     * 测试用 POJO 行。
     */
    @Data
    public static class UserRow {

        /** 用户编码 */
        @DeadExcelColumn("用户编码")
        private String userCode;

        /** 昵称 */
        @DeadExcelColumn("昵称")
        private String nickname;

        /** 状态 */
        @DeadExcelColumn(value = "状态", ignore = true)
        private Integer status;

        UserRow() {}

        UserRow(String userCode, String nickname, Integer status) {
            this.userCode = userCode;
            this.nickname = nickname;
            this.status = status;
        }
    }

    /**
     * 测试用 Record 行。
     *
     * @param userCode   用户编码
     * @param nickname   昵称
     * @param createTime 创建时间
     */
    record UserRecord(
            @DeadExcelColumn("用户编码") String userCode,
            @DeadExcelColumn("昵称") String nickname,
            @DeadExcelColumn(value = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime) {}
}
