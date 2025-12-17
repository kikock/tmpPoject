package org.jeecg.modules.rule.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @project_name: 后端平台项目
 * @description: 通用的JSON类型处理器，用于Java对象和JSON字符串的转换
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Class<T> type;

    // MyBatis 实例化时需要一个无参构造函数
    public JsonTypeHandler() {
        // 在这里，类型稍后会通过 MappedTypes 注解设置
    }

    // 显式设置类型的构造函数，如果你需要手动指定类型的话
    public JsonTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error serializing object to JSON string", e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonString = rs.getString(columnName);
        return convertJson(jsonString);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return convertJson(jsonString);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return convertJson(jsonString);
    }

    private T convertJson(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            // 检查类型是否已被设置
            if (this.type == null) {
                throw new SQLException("JsonTypeHandler was not properly initialized with a Class type.");
            }
            return OBJECT_MAPPER.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error deserializing JSON string to object", e);
        }
    }
}