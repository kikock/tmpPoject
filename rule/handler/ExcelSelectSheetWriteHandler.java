package org.jeecg.modules.rule.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.rule.annotation.ExcelSelect;
import org.jeecgframework.poi.excel.annotation.ExcelIgnore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description:
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public class ExcelSelectSheetWriteHandler implements SheetWriteHandler {

    private final Class<?> head;

    public ExcelSelectSheetWriteHandler(Class<?> head) {
        this.head = head;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();

        // 1. 获取所有需要导出的字段（排除了被 @ExcelIgnore 标记的）
        List<Field> exportableFields = new ArrayList<>();
        for (Field field : head.getDeclaredFields()) {
            if (field.getAnnotation(ExcelIgnore.class) == null) {
                exportableFields.add(field);
            }
        }

        // 2. 遍历字段，查找 @ExcelSelect 注解
        for (int i = 0; i < exportableFields.size(); i++) {
            Field field = exportableFields.get(i);
            ExcelSelect excelSelect = field.getAnnotation(ExcelSelect.class);
            if (excelSelect != null) {
                String[] source = {};
                // 优先从数据字典中获取下拉选项
                if (oConvertUtils.isNotEmpty(excelSelect.dictCode())) {
                    CommonAPI commonApi = SpringContextUtils.getBean(CommonAPI.class);
                    List<DictModel> dictModels = commonApi.queryDictItemsByCode(excelSelect.dictCode());
                    if (dictModels != null && !dictModels.isEmpty()) {
                        source = dictModels.stream().map(DictModel::getText).toArray(String[]::new);
                    }
                } else {
                    // 其次使用注解中固定的 source
                    source = excelSelect.source();
                }

                if (source.length > 0) {
                    // 3. 创建下拉列表约束
                    DataValidationConstraint constraint = helper.createExplicitListConstraint(source);

                    // 4. 设置作用范围
                    // firstRow: 起始行（1表示第二行，因为第一行是表头）
                    // lastRow: 结束行（设置为一个较大的值，以覆盖更多行）
                    // firstCol: 起始列
                    // lastCol: 结束列
                    // [优化] 将行数限制从1000提高到65535，以支持更多数据
                    CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, i, i);

                    // 5. 创建数据验证对象
                    DataValidation dataValidation = helper.createValidation(constraint, addressList);

                    // 6. 将验证规则添加到工作表中
                    sheet.addValidationData(dataValidation);
                }
            }
        }
    }
}