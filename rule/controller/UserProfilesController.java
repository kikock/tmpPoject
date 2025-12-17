package org.jeecg.modules.rule.controller;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.excel.ExcelTools;
import org.jeecg.modules.rule.entity.domain.RuleDescriptionVo;
import org.jeecg.modules.rule.entity.domain.UserProfiles;
import org.jeecg.modules.rule.service.IUserProfilesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 用户画像信息接口
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@RestController
@RequestMapping("/lessonRule/userProfiles")
@Log4j2
public class UserProfilesController extends JeecgController<UserProfiles,IUserProfilesService> {
    @Resource
    private IUserProfilesService service;

    @Resource
    private ExcelTools excelTools;


    /**
     * 添加
     *
     * @param userProfiles
     * @return
     */
    @AutoLog(value = "用户画像-添加")
    @PostMapping(value = "/add")
    public Result<Boolean> add(@RequestBody UserProfiles userProfiles) {
        boolean res = service.save(userProfiles);
        return Result.OK(res);
    }

    /**
     * 编辑
     *
     * @param userProfiles
     * @return
     */
    @AutoLog(value = "用户画像-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<Boolean> edit(@RequestBody UserProfiles userProfiles) {
        boolean res = service.saveOrUpdate(userProfiles);
        return Result.OK(res);
    }




    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<UserProfiles> queryById(@RequestParam(name = "id", required = true) String id) {
        UserProfiles userProfiles = service.getById(id);
        if (userProfiles == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(userProfiles);
    }

//    /**
//     * 通过excel导入数据
//     *
//     * @param file
//     * @param taskName
//     * @return
//     */
//    @AutoLog(value = "导入人员评定规则", operateType = 5)
//    @PostMapping("/importExcel")
//    public ProgressStatus importExcel(@RequestParam("file") MultipartFile file, String taskName) {
//        if (ObjectUtil.isNull(file)) {
//            throw new JeecgBootException("没有收到上传文件");
//        }
//        ImportLessonRulesDataListener listener = new ImportLessonRulesDataListener();
//        //设置任务名称
//        //导入模板的表头行数
//        listener.setHeadRowCount(2);
//        //设置批量处理的行数
//        listener.setBatchSize(5000);
//        return excelTools.importExcel(file, taskName, listener);
//    }

    @AutoLog(value = "导出个人评定规则", operateType = 6)
    @GetMapping("/exportTemplateExcel")
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        service.getTemplate(response);
    }

    @GetMapping("/getRuleDescription")
    public Result<?> getRuleDescription(@RequestParam(name = "userProfileId") String userProfileId,
                                        @RequestParam(name = "ruleId", required = false) String ruleId) {
        if (StringUtils.isBlank(userProfileId)) {
            return Result.error("规则ID不能为空");
        }

        if (StringUtils.isNotBlank(ruleId)) {
            // 返回单个年龄段的规则描述
            RuleDescriptionVo description = service.getRuleDescription(userProfileId, ruleId);
            return Result.OK(description);
        } else {
            // 返回所有年龄段的规则描述
            List<RuleDescriptionVo> descriptions = service.getRuleDescriptions(userProfileId);
            return Result.OK(descriptions);
        }
    }
}
