package org.prismsus.tank.utils

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

inline fun <reified T : Any> Any.getMemberByName(propertyName: String): T {
    // https://stackoverflow.com/a/35525706/20080946
    val getterName = "get" + propertyName.replaceFirstChar { it.uppercase()  }
    return javaClass.getMethod(getterName).invoke(this) as T
}

fun Any.setPropertyByName(propertyName : String, value : Any){
    // this works even for private and val properties
    val declaredField = getPropertyByName(propertyName).javaField!!
    val origAccessibility = declaredField.isAccessible
    declaredField.isAccessible = true
    declaredField.set(this, value)
    declaredField.isAccessible = origAccessibility
}

inline fun <reified T : Any> T.getPropertyByName(propertyName : String) : KProperty1<out T, *> {
    return this::class.memberProperties.find { it.name == propertyName }!!
}

val <T : Any> KClass<T>.companionClass get() =
    // https://stackoverflow.com/questions/44481268/call-super-class-constructor-in-kotlin-super-is-not-an-expression
    if (isCompanion)
        this.java.declaringClass.kotlin
    else
        null

