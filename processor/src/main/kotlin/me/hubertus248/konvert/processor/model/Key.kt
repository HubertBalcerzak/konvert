package me.hubertus248.konvert.processor.model

import kotlinx.metadata.KmClassifier

data class Key(val source: String, val target: String) {
    companion object {
        fun create(source: KotlinProperty, target: KotlinProperty): Key = Key(
            (source.type.classifier as KmClassifier.Class).name,
            (target.type.classifier as KmClassifier.Class).name
        )

    }
}
