package com.mtfm.deadman.plugin.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.file.entity.FileMetadata;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件元数据 Mapper。
 */
@Mapper
public interface FileMetadataMapper extends BaseMapper<FileMetadata> {}
