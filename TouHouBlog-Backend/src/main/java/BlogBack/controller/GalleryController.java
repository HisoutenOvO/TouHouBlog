package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.dto.GalleryImageAddDTO;
import BlogBack.pojo.vo.GalleryImageVO;
import BlogBack.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
@Tag(name = "图集接口")
@Slf4j
public class GalleryController {

    private final GalleryService galleryService;

    /**
     * 获取图集图片列表
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取图集图片列表")
    public Result<List<GalleryImageVO>> list() {
        return Result.success(galleryService.listImages());
    }

    /**
     * 新增图集图片
     * @param dto
     * @return
     */
    @PostMapping
    @Operation(summary = "新增图集图片")
    public Result add(@RequestBody GalleryImageAddDTO dto) {
        galleryService.addImage(dto);
        return Result.success();
    }

    /**
     * 删除图集照片
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除图集图片")
    public Result delete(@PathVariable Long id) {
        galleryService.deleteImage(id);
        return Result.success();
    }
}