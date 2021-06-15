package me.hubertus248.konvert.processor.model

import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType

data class Key(val source: String, val target: String) {
    companion object {

        fun create(source: KotlinProperty, target: KotlinProperty): Key = Key(
            (source.type.classifier as KmClassifier.Class).name,
            (target.type.classifier as KmClassifier.Class).name
        )

        fun create(source: KmType, target: KmType): Key = Key(
            (source.classifier as KmClassifier.Class).name,
            (target.classifier as KmClassifier.Class).name
        )
    }
}
