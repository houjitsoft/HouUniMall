package com.houjiang.commons.exception;

import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    @Test
    void databaseWriteErrorsDoNotExposeSqlOrMapperDetails() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "### Error updating database. SQL: INSERT INTO secret_table Mapper.insert"
        );

        ResponseResult<?> response = handler.runtimeException(exception);

        assertEquals("数据保存失败，请稍后重试", response.getMessage());
        assertFalse(response.getMessage().contains("SQL"));
        assertFalse(response.getMessage().contains("Mapper"));
    }
}
