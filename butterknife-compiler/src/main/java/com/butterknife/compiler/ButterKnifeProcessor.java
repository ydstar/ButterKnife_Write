package com.butterknife.compiler;

import com.butterknife.annotations.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Author: 信仰年轻
 * Date: 2021-06-18 14:48
 * Email: hydznsqk@163.com
 * Des:在编译时用来生成BindView标记的注解的代码
 */
//@AutoService 这个注解能帮助我们自动注册，对应com.google.auto.service:auto-service:1.0-rc2如果不使用这个注解的话，需要手动去注册，
// 步骤是，在butterKnife-compilermodule的main目录下创建resources/META-INF/services文件夹，
// 再创建javax.annotation.processing.Processor文件，文件中写BindViewProcessor的全类名
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "=========init============");
    }

    /**
     * 1.指定处理的版本
     * 支持的jdk版本,一般返回支持的最新的
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

     //2. 给到需要处理的注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    /**
     * 能处理的注解类型，里面接受的是一个注解，说明可以同时处理多个，butterknife就是支持多个，
     * 同时处理BindView和OnClick注解，如果不使用注解的话，
     * 你也可以重写getSupportedAnnotationTypes方法,getSupportedAnnotationTypes返回的是一个set<String>集合，里面添加需要处理的集合信息
     *
     * @return
     */
    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        // 需要解析的自定义注解 BindView  OnClick
        annotations.add(BindView.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 调试打印
        messager.printMessage(Diagnostic.Kind.NOTE, "=======process============");

        // process 方法代表的是，有注解就都会进来 ，但是这里面是一团乱麻
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        /*for (Element element : elements) {
            Element enclosingElement = element.getEnclosingElement();
            System.out.println("------------------------>"+element.getSimpleName().toString()+" "+enclosingElement.getSimpleName().toString());
        }*/
        // 解析 属性 activity -> List<Element>
        //       1个Activity对应一个List的Element,该Element的名字在本例中就是textView1 textView2
        Map<Element, List<Element>> elementsMap = new LinkedHashMap<>();
        for (Element element : elements) {
            Element enclosingElement = element.getEnclosingElement();

            List<Element> viewBindElements = elementsMap.get(enclosingElement);
            if (viewBindElements == null) {
                viewBindElements = new ArrayList<>();
                elementsMap.put(enclosingElement, viewBindElements);
            }

            viewBindElements.add(element);
        }

        // 生成代码
        for (Map.Entry<Element, List<Element>> entry : elementsMap.entrySet()) {
            Element enclosingElement = entry.getKey();
            List<Element> viewBindElements = entry.getValue();

            // public final class xxxActivity_ViewBinding implements Unbinder
            String activityClassNameStr = enclosingElement.getSimpleName().toString();
            ClassName activityClassName = ClassName.bestGuess(activityClassNameStr);
            ClassName unbinderClassName = ClassName.get("com.butterknife","Unbinder");
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(activityClassNameStr+"_ViewBinding")
                    .addModifiers(Modifier.FINAL,Modifier.PUBLIC).addSuperinterface(unbinderClassName)
                    .addField(activityClassName,"target",Modifier.PRIVATE);


            // 实现 unbind 方法
            // android.support.annotation.CallSuper
            ClassName callSuperClassName = ClassName.get("android.support.annotation","CallSuper");
            MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                    .addAnnotation(callSuperClassName);

            unbindMethodBuilder.addStatement("$T target = this.target",activityClassName);
            unbindMethodBuilder.addStatement("if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");");

            // 构造函数
            MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                    .addParameter(activityClassName,"target");
            // this.target = target;
            constructorMethodBuilder.addStatement("this.target = target");
            // findViewById 属性
            for (Element viewBindElement : viewBindElements) {
                // target.textView1 = Utils.findRequiredViewAsType(source, R.id.tv1, "field 'textView1'", TextView.class);
                // target.textView1 = Utils.findViewById(source, R.id.tv1);
                String filedName = viewBindElement.getSimpleName().toString();
                ClassName utilsClassName = ClassName.get("com.butterknife","Utils");
                int resId = viewBindElement.getAnnotation(BindView.class).value();
                constructorMethodBuilder.addStatement("target.$L = $T.findViewById(target, $L)",filedName,utilsClassName,resId);
                // target.textView1 = null;
                unbindMethodBuilder.addStatement("target.$L = null",filedName);
            }

            classBuilder.addMethod(unbindMethodBuilder.build());
            classBuilder.addMethod(constructorMethodBuilder.build());

            // 生成类，看下效果
            try {
                String packageName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
                JavaFile.builder(packageName,classBuilder.build())
                        .addFileComment("butterknife 自动生成")
                        .build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("翻车了！");
            }
        }
        return false;
    }
}