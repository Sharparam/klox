package com.sharparam.klox

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

fun <T: Any> T.logger() = lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass)) }

fun logger(name: String) = lazy { LoggerFactory.getLogger(name) }

private fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

private fun <T: Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> = unwrapCompanionClass(ofClass.java).kotlin
