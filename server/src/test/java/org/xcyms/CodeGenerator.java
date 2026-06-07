package org.xcyms;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @author liu-xu
 * @date 2026年01月11日 11:53
 */
public class CodeGenerator {

    private static final String URL = "jdbc:mysql://localhost:3306/universal_starter?serverTimezone=GMT%2B8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String PACKAGE_NAME = "org.xcyms";
    private static final String OutPutDir = "D://lewis/code";

    public static void main(String[] args) {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> {
                    builder.author("liu-xu") // 设置作者
                            .outputDir(OutPutDir + "/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_NAME) // 设置父包名
                            .moduleName("") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, OutPutDir + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> builder.addInclude("sys_file") // 设置需要生成的表名
                        .addTablePrefix("sys_", "biz_") // 设置过滤表前缀
                        .entityBuilder()
                        .enableFileOverride()
                        .enableLombok() // 启用 Lombok
                        .enableTableFieldAnnotation() // 启用字段注解
                        .logicDeleteColumnName("deleted") // 逻辑删除字段名
                        .addTableFills(new com.baomidou.mybatisplus.generator.fill.Column("create_time", com.baomidou.mybatisplus.annotation.FieldFill.INSERT))
                        .addTableFills(new com.baomidou.mybatisplus.generator.fill.Column("update_time", com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE))
                        .controllerBuilder()
                        .enableRestStyle() // 开启 restview 控制器
                        .enableFileOverride()
                        .mapperBuilder()
                        .mapperAnnotation(org.apache.ibatis.annotations.Mapper.class)
                        .enableFileOverride())
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板
                .execute();
    }
}
