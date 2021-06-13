package me.hubertus248.konvert.processor.tree

import me.hubertus248.konvert.processor.contains
import me.hubertus248.konvert.processor.model.Key
import me.hubertus248.konvert.processor.model.ConvertibleProperty
import me.hubertus248.konvert.processor.model.Konverter
import me.hubertus248.konvert.processor.model.KonverterDefinition


class KonverterTree private constructor(private val nodes: List<TreeNode>) {

    fun getKonverters(): List<Konverter> = nodes.map { (konverterDefinition, children) ->
        val convertibleProperties = children
            .filter { it.node.pure }
            .map { ConvertibleProperty(it.property, it.node.value) }
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
            convertibleProperties
        )
    }


    companion object {
        fun create(konverters: Map<Key, KonverterDefinition>) = KonverterTree(prepareNodes(konverters))

        private fun prepareNodes(konverters: Map<Key, KonverterDefinition>): List<TreeNode> {
            val nodes = konverters.map { (key, konverter) ->
                key to TreeNode(konverter)
            }.toMap()

            nodes.values.map {
                it.value
            }.forEach { konverter ->
                val sourceProperties = konverter.sourceProperties.associateBy { it.name }

                konverter.missingProperties
                    .map { it to sourceProperties[it.name] }
                    .filter { (_, source) -> source != null }
                    .filter { (target, source) -> nodes.containsKey(Key.create(source!!, target)) }
                    .forEach { (target, source) ->
                        nodes[konverter.key]?.addChild(target, nodes[Key.create(source!!, target)]!!)
                    }
            }
            return nodes.values.toList()
        }
    }

}
