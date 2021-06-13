package me.hubertus248.konvert.processor.tree

import me.hubertus248.konvert.processor.model.KonverterDefinition
import me.hubertus248.konvert.processor.model.KotlinProperty

data class Edge(val property: KotlinProperty, val node: TreeNode)

class TreeNode(val value: KonverterDefinition) {

    private val children = mutableSetOf<Edge>()

    val pure by lazy { purify() }

    fun addChild(property: KotlinProperty, node: TreeNode) {
        children.add(Edge(property, node))
    }

    private fun purify(): Boolean = children.all { it.node.pure }

    operator fun component1() = value
    operator fun component2() = children
}
