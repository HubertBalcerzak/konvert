package me.hubertus248.konvert.processor

import com.google.auto.service.AutoService
import me.hubertus248.konvert.api.Konvert
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

@AutoService(Processor::class)
class TransformAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        const val FROM_ANNOTATION_KEY = "from"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(Konvert::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion =
        SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?: throw IllegalStateException("$KAPT_KOTLIN_GENERATED_OPTION_NAME is missing")

        val elements = roundEnv.getElementsAnnotatedWith(Konvert::class.java)

        if (elements.any { it.kind != ElementKind.CLASS }) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes should be annotated")
            return true
        }
        elements.forEach(this::processAnnotation)

        KonverterBuilder.generate(kaptKotlinGeneratedDir)
        return false
    }

    private fun processAnnotation(element: Element) {
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        (element as TypeElement).annotationMirrorByType(Konvert::class.java)
            ?.annotationClassValuesByKey(FROM_ANNOTATION_KEY)
            ?.map { it.value as TypeMirror }
            ?.map { it.asTypeElement(processingEnv) }
            ?.forEach { KonverterBuilder.registerKonverter(it, element, pack) }
    }
}
