package org.jeecg.modules.rule.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @project_name: eosen-military
 * @description: 通用的 List<T> 类型处理器，用于将 List 序列化为 JSON 字符串存入数据库，
 * 并将 JSON 字符串反序列化回 List<T>。
 * @Author: kikock
 * @Date: 2025-10-08
 * @Version: V1.0
 **/
/**
 * @project_name: 后端平台项目
 * @description: 通用的 List<T> 类型处理器，用于将 List 序列化为 JSON 字符串存入数据库，
 *  * 并将 JSON 字符串反序列化回 List<T>。
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@MappedTypes(List.class)
public class JsonListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 运行时类型引用，用于解决 JSON 反序列化时的类型擦除问题
    private final TypeReference<List<T>> typeReference;

    /**
     * 构造函数：在 MyBatis 实例化 TypeHandler 时，动态获取 List<T> 中 T 的实际类型。
     */
    public JsonListTypeHandler() {
        // 1. 获取当前类的泛型父类（即 BaseTypeHandler<List<T>>）
        Type superclass = getClass().getGenericSuperclass();

        // 2. 确保它是一个泛型类型，并获取其类型参数
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superclass;

            // 3. 提取 List<T>
            Type listType = parameterizedType.getActualTypeArguments()[0];

            // 4. 创建 TypeReference，以便 ObjectMapper 知道如何反序列化 List<T>
            this.typeReference = new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return listType;
                }
            };
        } else {
            // 如果类不是泛型（不应该发生），抛出异常
            throw new IllegalArgumentException("JsonListTypeHandler 必须使用泛型参数来指定 List 元素的类型。");
        }
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 将 List<T> 序列化为 JSON 字符串
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("序列化 List<T> 到 JSON 失败。", e);
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    /**
     * 用于解析来自数据库的 JSON 字符串的辅助方法。
     */
    private List<T> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 使用存储在 typeReference 中的运行时类型信息进行反序列化
            return objectMapper.readValue(json, this.typeReference);
        } catch (Exception e) {
            throw new SQLException("反序列化 JSON 到 List<T> 失败。", e);
        }
    }
}
