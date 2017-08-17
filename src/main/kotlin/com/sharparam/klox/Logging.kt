package com.sharparam.klox

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

fun logger(name: String): Logger = LoggerFactory.getLogger(name)

fun <T: Any> T.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass)) }

private fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

private fun <T: Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> = unwrapCompanionClass(ofClass.java).kotlin
