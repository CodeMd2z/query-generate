package com.corleone.processor;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.corleone.annotations.EnableQueryGenerate;
import com.corleone.annotations.EntityFieldConfig;
import com.corleone.model.EntityConfig;
import com.corleone.model.GenerateEntityConfigInterface;
import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.corleone.annotations.EnableQueryGenerate")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityGenerateProcessor extends CommonGenerator {

    private static final String PREFIX = "E";

    private static final String FILE_NAME = "GeneratedEntityConfig";
    private static final String COMPONENT_PREFIX = "GEC_";
    private static final String COMMON = "common";
    private static final String ID = "id";
    private static final Integer STEP = 10;
    private static final Map<String, String> COMMON_COLUMNS = new HashMap<>();

    static {
        COMMON_COLUMNS.put("id", "id");
        COMMON_COLUMNS.put("createdBy", "created_by");
        COMMON_COLUMNS.put("createdTime", "created_time");
        COMMON_COLUMNS.put("updatedBy", "updated_by");
        COMMON_COLUMNS.put("updatedTime", "updated_time");
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement typeElement : annotations) {
            if (typeElement.getQualifiedName().contentEquals(EnableQueryGenerate.class.getName())) {
                Map<String, EntityConfig> configMap = new HashMap<>();
                String packageName = null;
                for (Element elEntity : roundEnv.getElementsAnnotatedWith(typeElement)) {
                    EntityConfig entityConfig = new EntityConfig();
                    // 获取注解
                    EnableQueryGenerate annotation = elEntity.getAnnotation(EnableQueryGenerate.class);
                    if (Objects.isNull(annotation) || StrUtil.isBlank(annotation.table())) {
                        continue;
                    }
                    entityConfig.setTable(annotation.table().toLowerCase());
                    String className = elEntity.getSimpleName().toString();
                    if (elEntity instanceof TypeElement) {
                        String fullName = ((TypeElement) elEntity).getQualifiedName().toString();
                        String currentPackage = removeLastDot(fullName);
                        packageName = generatePackageName(currentPackage, packageName);
                        if (packageName != null) {
                            readAnnotation(elEntity, entityConfig);
                            entityConfig.setClazzName(fullName);
                            entityConfig.setNumFields(getNumFields(elEntity));
                            configMap.put(className, entityConfig);
                        }
                        createEntityFNFile(elEntity, currentPackage, className);
                    }
                }
                if (StrUtil.isNotBlank(packageName)) {
                    generateFile(packageName, configMap);
                }

            }
        }
        return true;
    }

    private void createEntityFNFile(Element elEntity, String currentPackage, String className) {
        String fileName = PREFIX + className;
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(fileName)
                .addModifiers(Modifier.PUBLIC);
        List<? extends Element> elFields = elEntity.getEnclosedElements();
        elFields.forEach(element -> {
            if (element.getKind().isField()) {
                String fn = element.getSimpleName().toString();
                builder.addField(FieldSpec.builder(String.class, fn)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", fn)
                        .build());
            }
        });
        createFile(fileName, removeLastDot(currentPackage), builder.build());
    }

    private static String removeLastDot(String source) {
        return source.replaceFirst("\\.[^.]*$", StrUtil.EMPTY);
    }

    private static String generatePackageName(String currentPackage, String packageName) {
        String tempPackage = currentPackage;
        // if (tempPackage.equals(currentPackage)) {
        //     return null;
        // }
        if (Objects.isNull(packageName)) {
            packageName = tempPackage;
        } else if (!tempPackage.startsWith(packageName)) {
            int i = 0;
            for (; i < tempPackage.length() && i < packageName.length(); i++) {
                if (tempPackage.charAt(i) != packageName.charAt(i)) {
                    break;
                }
            }
            packageName = tempPackage.substring(0, i);
            packageName = packageName.substring(0, packageName.lastIndexOf(StrUtil.DOT));
        }
        return packageName;
    }

    private static void readAnnotation(Element elEntity, EntityConfig entityConfig) {
        Map<String, String> columnMap = new HashMap<>();
        List<? extends Element> elFields = elEntity.getEnclosedElements();
        elFields.forEach(element -> {
            if (element.getKind().isField()) {
                EntityFieldConfig fc = element.getAnnotation(EntityFieldConfig.class);
                if (Objects.nonNull(fc)) {
                    String column = fc.column();
                    String fieldName = element.getSimpleName().toString();
                    if (StrUtil.isEmpty(column)) {
                        column = StrUtil.toUnderlineCase(fieldName);
                    }
                    columnMap.put(fieldName, column.toLowerCase());
                }
            }
        });
        columnMap.putAll(COMMON_COLUMNS);
        entityConfig.setColumns(columnMap);
    }

    private static Set<String> getNumFields(Element elEntity) {
        Set<String> numFields = Sets.newHashSet();
        List<? extends Element> elFields = elEntity.getEnclosedElements();
        elFields.forEach(element -> {
            if (element.getKind().isField()) {
                try {
                    String typeName = element.asType().toString();
                    Class<?> aClass = Class.forName(typeName);
                    if (Number.class.isAssignableFrom(aClass)) {
                        numFields.add(element.getSimpleName().toString());
                    }
                } catch (ClassNotFoundException ignore) {
                }
            }
        });
        numFields.add(ID);
        return numFields;
    }

    private void generateFile(String packageName, Map<String, EntityConfig> configMap) {
        String fileName = generateFileName(packageName);
        String uuid = COMPONENT_PREFIX + UUID.fastUUID();
        TypeSpec.Builder builder = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(GenerateEntityConfigInterface.class)
                .addAnnotation(Slf4j.class)
                .addAnnotation(AnnotationSpec.builder(Component.class)
                        .addMember("value", "$S", uuid).build());
        initFields(builder);

        // method - getConfig
        builder.addMethod(MethodSpec
                .methodBuilder("getConfig")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(EntityConfig.class)
                .addParameter(String.class, "name")
                .addCode(CodeBlock.builder()
                        .addStatement("return MAP.get(name)").build())
                .build());
        // method - keySet
        builder.addMethod(MethodSpec
                .methodBuilder("keySet")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class)))
                .addCode(CodeBlock.builder()
                        .addStatement("return MAP.keySet()").build())
                .build());

        // insert to map
        insertToStaticMap(configMap, builder, fileName + StrUtil.DASHED + uuid);
        // create file
        createFile(fileName, packageName, builder.build());
    }

    private static String generateFileName(String packageName) {
        String fileName = FILE_NAME;
        int index = packageName.lastIndexOf(StrUtil.DOT);
        if (index > 0) {
            String sub = packageName.substring(index + 1);
            if (Objects.equals(sub, COMMON)) {
                sub = packageName.substring(0, index);
                int i = sub.lastIndexOf(StrUtil.DOT);
                if (i > 0) {
                    sub = sub.substring(i + 1);
                }
            }
            fileName += StrUtil.upperFirst(sub);
        }
        return fileName;
    }

    private static void initFields(TypeSpec.Builder builder) {
        builder.addField(FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                ClassName.get(String.class),
                                ClassName.get(EntityConfig.class)), "MAP")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", HashMap.class)
                        .build())
                .addField(FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                ClassName.get(String.class),
                                ClassName.get(String.class)), "columnMap")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .build())
                .addField(FieldSpec.builder(EntityConfig.class, "config")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .build())
                .addField(FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get(Set.class),
                                ClassName.get(String.class)), "numFields")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .build());
    }

    private static void packMethod(List<Map.Entry<String, EntityConfig>> tempList, TypeSpec.Builder builder, int steps) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        tempList.forEach(o -> {
            EntityConfig config = o.getValue();
            // codeBuilder.add("$L//adding $L$L", StrUtil.LF, config.getClazzName(), StrUtil.LF);
            codeBuilder.addStatement("columnMap = new $T<>()", HashMap.class);
            config.getColumns().forEach((k, v) -> codeBuilder.addStatement("columnMap.put($S, $S)", k, v));
            codeBuilder.addStatement("numFields = new $T<>()", HashSet.class);
            config.getNumFields().forEach(n -> codeBuilder.addStatement("numFields.add($S)", n));
            codeBuilder.addStatement("config = new $T()", EntityConfig.class);
            codeBuilder.addStatement("config.setColumns(columnMap)");
            codeBuilder.addStatement("config.setNumFields(numFields)");
            codeBuilder.addStatement("config.setClazzName($S)", config.getClazzName());
            codeBuilder.addStatement("config.setTable($S)", config.getTable());
            codeBuilder.addStatement("MAP.put($S, config)", o.getKey());
        });
        builder.addMethod(MethodSpec
                .methodBuilder("add" + steps)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeBuilder.build())
                .build());
    }

    private static void insertToStaticMap(Map<String, EntityConfig> configMap, TypeSpec.Builder builder, String fileName) {
        int count = 0;
        int steps = 1;
        List<Map.Entry<String, EntityConfig>> tempList = new ArrayList<>(STEP);
        for (Map.Entry<String, EntityConfig> entry : configMap.entrySet()) {
            if (++count == STEP) {
                packMethod(tempList, builder, steps);
                count = 0;
                steps++;
                tempList = new ArrayList<>(STEP);
            }
            tempList.add(entry);
        }
        if (!tempList.isEmpty()) {
            packMethod(tempList, builder, steps);
        }
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        for (int i = 1; i <= steps; i++) {
            codeBuilder.addStatement("add" + i + "()");
        }
        codeBuilder.addStatement("log.info($S, $S)",
                "----QG Loaded: {}", fileName);
        builder.addStaticBlock(codeBuilder.build());
    }

}
