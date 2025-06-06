package com.corleone.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

public abstract class CommonGenerator extends AbstractProcessor {

    protected static final String INDENT = "    ";// 缩进四个空格

    protected void createFile(String fileName, String packageName, TypeSpec typeSpec) {
        try {
            // 创建Java文件
            JavaFileObject f = processingEnv.getFiler().createSourceFile(fileName);
            // 在控制台输出文件路径
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + f.toUri());
            try (Writer w = f.openWriter()) {
                JavaFile.builder(packageName, typeSpec)
                        .indent(INDENT).build()
                        .writeTo(w);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    e.toString());
        }
    }
}
