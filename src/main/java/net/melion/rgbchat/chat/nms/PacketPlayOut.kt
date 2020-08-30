package net.melion.rgbchat.chat.nms

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;


abstract class PacketPlayOut {
    @Throws(Exception::class)
    abstract fun toNMS(clientVersion: ProtocolVersion?): Any?

    companion object {
        fun getFields(clazz: Class<*>?): Map<String, Field> {
            val fields: MutableMap<String, Field> = HashMap<String, Field>()
            if (clazz == null) return fields
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                fields[field.name] = field
            }
            return fields
        }

        fun getStaticFields(clazz: Class<*>?): Map<String, Any> {
            val fields: MutableMap<String, Any> = HashMap()
            if (clazz == null) return fields
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                if (Modifier.isStatic(field.modifiers)) {
                    try {
                        fields[field.name] = field[null]
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return fields
        }

        fun getFields(clazz: Class<*>?, type: Class<*>): List<Field> {
            val list: MutableList<Field> = ArrayList<Field>()
            if (clazz == null) return list
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                if (field.type === type) list.add(field)
            }
            return list
        }

        fun getObjectAt(list: List<Field?>, index: Int): Field? {
            return try {
                list[index]
            } catch (e: Exception) {
                null
            }
        }

        fun getField(fields: Map<String, Field?>, field: String): Field? {
            val f: Field? = fields[field]
            if (f == null) {
                //modded server
                for ((key, value) in fields) {
                    if (key.contains("_")) {
                        val localfield = key.split("_".toRegex()).toTypedArray()[2]
                        if (localfield == field) return value
                    }
                }
            } else {
                return f
            }
            return null
        }

        fun getField(clazz: Class<*>?, name: String?): Field? {
            return if (clazz == null) null else try {
                val f: Field = clazz.getDeclaredField(name)
                f.setAccessible(true)
                f
            } catch (e: NoSuchFieldException) {
                null
            }
        }

        fun getNMSClass(vararg potentialNames: String): Class<*>? {
            for (className in potentialNames) {
                try {
                    return Class.forName("net.minecraft.server.v1_16_2.$className")
                } catch (e: Throwable) {
                }
            }
            return null
        }

        fun getClass(vararg potentialNames: String?): Class<*>? {
            for (className in potentialNames) {
                try {
                    return Class.forName(className)
                } catch (e: Throwable) {
                }
            }
            return null
        }

        fun getConstructor(clazz: Class<*>?, vararg parameterCount: Int): Constructor<*>? {
            if (clazz == null) return null
            for (count in parameterCount) {
                for (c in clazz.constructors) {
                    if (c.parameterCount === count) return c
                }
            }
            return null
        }

        fun getConstructor(clazz: Class<*>?, vararg parameterTypes: Class<*>?): Constructor<*>? {
            return if (clazz == null) null else try {
                clazz.getConstructor(*parameterTypes)
            } catch (e: NoSuchMethodException) {
                null
            }
        }

        fun getMethod(clazz: Class<*>?, methodName: String?, parameterCount: Int): Method? {
            if (clazz == null) return null
            for (m in clazz.methods) {
                if (m.name == methodName && m.parameterCount == parameterCount) return m
            }
            return null
        }
    }
}