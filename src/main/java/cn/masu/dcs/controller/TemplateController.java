package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.TemplateCreateDTO;
import cn.masu.dcs.dto.TemplateUpdateDTO;
import cn.masu.dcs.service.TemplateService;
import cn.masu.dcs.vo.TemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 模板管理控制器
 * <p>
 * 同时支持 /api/template 和 /api/templates 路径，保证向后兼容
 * </p>
 * @author zyq
 */
@RestController
@RequestMapping({"/api/template", "/api/templates"})  // ✅ 同时支持单数和复数
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 创建模板
     */
    @PostMapping
    public R<Long> createTemplate(@Validated @RequestBody TemplateCreateDTO dto) {
        Long id = templateService.createTemplate(dto);
        return R.ok("创建成功", id);
    }

    /**
     * 更新模板
     */
    @PutMapping
    public R<Boolean> updateTemplate(@Validated @RequestBody TemplateUpdateDTO dto) {
        Boolean result = templateService.updateTemplate(dto);
        return R.ok("更新成功", result);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteTemplate(@PathVariable Long id) {
        Boolean result = templateService.deleteTemplate(id);
        return R.ok("删除成功", result);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public R<TemplateVO> getTemplateDetail(@PathVariable Long id) {
        TemplateVO vo = templateService.getTemplateDetail(id);
        return R.ok(vo);
    }

    /**
     * 根据模板编码获取模板
     */
    @GetMapping("/code/{code}")
    public R<TemplateVO> getTemplateByCode(@PathVariable String code) {
        TemplateVO vo = templateService.getTemplateByCode(code);
        return R.ok(vo);
    }

    /**
     * 分页查询模板列表
     */
    @GetMapping("/page")
    public R<PageResult<TemplateVO>> getTemplatePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<TemplateVO> pageResult = templateService.getTemplatePage(current, size, keyword, status);
        return R.ok(pageResult);
    }

    /**
     * 启用/禁用模板
     */
    @PutMapping("/{id}/status")
    public R<Boolean> toggleTemplateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Boolean result = templateService.toggleTemplateStatus(id, status);
        return R.ok("状态更新成功", result);
    }
}
