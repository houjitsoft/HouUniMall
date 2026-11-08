//package com.houjiang.commons.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
//import com.fasterxml.jackson.databind.ser.std.DateSerializer;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//
//@Configuration
//public class GlobalDateFormatConfig {
//    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 你想要的日期格式
//
//    @Bean
//    public ObjectMapper objectMapper(){
//        ObjectMapper objectMapper = new ObjectMapper();
//        JavaTimeModule timeModule = new JavaTimeModule();
//        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
//        timeModule.addSerializer(Date.class, new DateSerializer());
//        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
//        timeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer());
//        objectMapper.registerModule(timeModule);
//        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
//        return objectMapper;
//    }
//
//}
