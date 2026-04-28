package com.pajamas.remark.controller;


import com.pajamas.remark.domain.dto.LikeRecordFormDTO;
import com.pajamas.remark.service.ILikedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-27
 */
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Api(tags = "点赞相关接口")
public class LikedRecordController {
    private ILikedRecordService likedRecordService;
    @PostMapping

    @ApiOperation("添加点赞记录")
    public  void addLikeRecord(@RequestBody @Valid LikeRecordFormDTO recordDTO){
        likedRecordService.addLikeRecord(recordDTO);
    }

    @GetMapping("list")
    @ApiOperation("查询指定业务id的点赞状态")
    public Set<Long> isBizLiked(@RequestParam("bizIds") List<Long> bizIds){
        return likedRecordService.isBizLiked(bizIds);
    }
}
