package me.hubertus248.konvert.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import me.hubertus248.konvert.api.Transform
import java.io.File
import java.lang.IllegalStateException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.load.kotlin.TypeMappingConfigurationImpl

@AutoService(Processor::class)
class TransformAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        const val FROM_ANNOTATION_KEY = "from"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(Transform::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion =
        SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(Transform::class.java)

        if (elements.any { it.kind != ElementKind.CLASS }) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes should be annotated")
            return true
        }
        elements.forEach(this::processAnnotation)
        return false
    }

    private fun processAnnotation(element: Element) {
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: throw IllegalStateException("$KAPT_KOTLIN_GENERATED_OPTION_NAME is missing")

        (element as TypeElement).annotationMirrorByType(Transform::class.java)
            ?.annotationClassValuesByKey(FROM_ANNOTATION_KEY)
            ?.map { it.value as TypeMirror }
            ?.map { it.asTypeElement(processingEnv) }
            ?.forEach { CodeGenerator.generate(it, element, pack, kaptKotlinGeneratedDir) }
    }
}
