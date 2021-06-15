package me.hubertus248.konvert.processor.tree

import me.hubertus248.konvert.processor.BuiltinConverterMap
import me.hubertus248.konvert.processor.canonicalName
import me.hubertus248.konvert.processor.canonicalNameMapped
import me.hubertus248.konvert.processor.contains
import me.hubertus248.konvert.processor.model.*
import javax.annotation.processing.ProcessingEnvironment


class KonverterTree private constructor(
    private val nodes: List<TreeNode>,
) {

    fun getKonverters(): List<Konverter> = nodes
        .filter { (konverterDefinition, _) -> konverterDefinition is KonverterDefinition.Generated }
        .map { (konverterDefinition, children) ->
            konverterDefinition as KonverterDefinition.Generated
            val convertibleProperties = children
                .filter { it.node.pure }
                .map { edge ->
                    when (edge.node.value) {
                        is KonverterDefinition.Generated -> SimpleConvertibleProperty(edge.property, edge.node.value)
                        is KonverterDefinition.Predefined -> ConvertibleIterableProperty(edge.property, edge.node.value)
                    }
                }
                .toSet()

            return@map Konverter(
                konverterDefinition.key,
                konverterDefinition.source,
                konverterDefinition.target,
                konverterDefinition.pack,
                konverterDefinition.filename,
                konverterDefinition.sourceProperties,
                konverterDefinition.commonProperties,
                konverterDefinition.missingProperties.filter { missingProperty ->
                    !convertibleProperties.contains { it.property == missingProperty }
                }.toSet(),
                convertibleProperties,
            )
        }


    companion object {
        fun create(konverters: Map<Key, KonverterDefinition.Generated>, processingEnv: ProcessingEnvironment) =
            KonverterTree(prepareNodes(konverters, processingEnv))

        private fun prepareNodes(
            konverters: Map<Key, KonverterDefinition.Generated>,
            processingEnv: ProcessingEnvironment
        ): List<TreeNode> {
            val nodes = konverters.map { (key, konverter) ->
                key to TreeNode(konverter)
            }.toMap()

            nodes.values
                .map { it.value }
                .filterIsInstance<KonverterDefinition.Generated>()
                .forEach { konverter ->
                    val sourceProperties = konverter.sourceProperties.associateBy { it.name }
                    findNestedProperties(konverter, sourceProperties, nodes)
                    findIterableNestedProperties(konverter, sourceProperties, nodes, processingEnv)
                }
            return nodes.values.toList()
        }

        private fun findNestedProperties(
            konverter: KonverterDefinition.Generated,
            sourceProperties: Map<String, KotlinProperty>,
            nodes: Map<Key, TreeNode>
        ) {
            konverter.missingProperties
                .map { it to sourceProperties[it.name] }
                .filter { (_, source) -> source != null }
                .filter { (target, source) -> nodes.containsKey(Key.create(source!!, target)) }
                .forEach { (target, source) ->
                    nodes[konverter.key]?.addChild(target, nodes[Key.create(source!!, target)]!!)
                }
        }

        private fun findIterableNestedProperties(
            konverter: KonverterDefinition.Generated,
            sourceProperties: Map<String, KotlinProperty>,
            nodes: Map<Key, TreeNode>,
            processingEnv: ProcessingEnvironment
        ) {
            val iterable = processingEnv.typeUtils.erasure(
                processingEnv.elementUtils.getTypeElement(Iterable::class.java.canonicalName).asType()
            )
            konverter.missingProperties
                .map { it to sourceProperties[it.name] }
                //TODO refactor, move to validator
                .filter { (_, source) -> source != null }
                .filter { (_, source) ->
                    processingEnv.elementUtils.getTypeElement(source!!.type.canonicalNameMapped())?.asType()
                        ?.let { processingEnv.typeUtils.erasure(it) }
                        ?.let { processingEnv.typeUtils.isAssignable(it, iterable) } ?: false
                }
                .filter { (target, source) -> target.type.arguments.size == 1 && source!!.type.arguments.size == 1 }
                .forEach { (target, source) ->
                    val sourceElementType = source!!.type.arguments.first().type ?: return@forEach
                    val targetElementType = target.type.arguments.first().type ?: return@forEach
                    val elementKonverter =
                        nodes[Key.create(
                            sourceElementType,
                            targetElementType
                        )]?.value ?: return@forEach

                    BuiltinConverterMap.CONVERTERS[target.type.canonicalName()]
                        ?.let { konvertFunction ->
                            nodes[konverter.key]?.addChild(
                                target,
                                TreeNode(KonverterDefinition.Predefined(konvertFunction, elementKonverter))
                            )
                        }
                }
        }
    }

}
