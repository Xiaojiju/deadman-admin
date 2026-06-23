package com.mtfm.deadman.plugin.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.file.entity.FileMetadata;

/**
 * 文件元数据 Mapper。
 */
@Mapper
public interface FileMetadataMapper extends BaseMapper<FileMetadata> {
}
