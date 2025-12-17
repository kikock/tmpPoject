package org.jeecg.modules.rule.controller;


import com.alibaba.excel.EasyExcel;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.excel.ExcelTools;
import org.jeecg.modules.excel.progress.ProgressService;
import org.jeecg.modules.rule.entity.PersonnelDept;
import org.jeecg.modules.rule.entity.excel.PersonnelDeptExcelData;
import org.jeecg.modules.rule.handler.ExcelSelectSheetWriteHandler;
import org.jeecg.modules.rule.service.IPersonnelDeptService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 单位规则接口
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@RestController
@RequestMapping("/personnelDept")
@Log4j2
public class PersonnelDeptController extends JeecgController<PersonnelDept, IPersonnelDeptService> {

    @Resource
    private IPersonnelDeptService service;
    @Resource
    private ExcelTools excelTools;

    @Resource
    private ProgressService progressService;

    /**
     * 添加单位评定规则
     *
     */
    @AutoLog(value = "添加单位评定规则", operateType = 2)
    @PostMapping(value = "/add")
    public Result<Boolean> add(@RequestBody @Valid PersonnelDept param) {
        boolean res = service.save(param);
        return Result.OK(res);
    }

    /**
     * 编辑单位评定规则
     *
     */
    @AutoLog(value = "编辑单位评定规则", operateType = 3)
    @PutMapping(value = "/edit")
    public Result<Boolean> edit(@RequestBody @Valid PersonnelDept param) {
        boolean res = service.saveOrUpdate(param);
        return Result.OK(res);
    }



    /**
     * 导出单位评定规则模板
     *
     */
    @GetMapping("/exportTemplateExcel")
    @AutoLog(value = "导出单位评定规则模板", operateType = 6)
    public void exportTemplateExcel(HttpServletResponse response) {
        try {
            String fileName = "单位总评规则导入模板";
            // 1. 设置下载响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");

            // 2. 创建一个包含默认说明的数据行
            List<PersonnelDeptExcelData> dataList = new ArrayList<>();
            PersonnelDeptExcelData exampleRow = new PersonnelDeptExcelData();
            exampleRow.setDescription("注意评等形式为二级制,单位评定规则评定等级只有及格会导入");
            dataList.add(exampleRow);
            // 3. 使用 EasyExcel 写入模板，并注册自定义的下拉框处理器
            EasyExcel.write(response.getOutputStream(), PersonnelDeptExcelData.class)
                    .registerWriteHandler(new ExcelSelectSheetWriteHandler(PersonnelDeptExcelData.class))
                    .sheet("单位总评规则").doWrite(dataList);
        } catch (IOException e) {
            throw new JeecgBootException("导出Excel模板失败: " + e.getMessage());
        }
    }


    /**
     * 保存或更新规则
     *
     */
    @AutoLog(value = "保存单位评定规则", operateType = 2)
    @PostMapping(value = "/saveDeptRuleDetail")
    @AutoDict
    public Result<Boolean> saveDeptRuleDetail(@RequestBody @Valid PersonnelDept param) {
        boolean b = service.saveOrUpdate(param);
        return Result.OK("保存单位总评规则成功", b);
    }
}
