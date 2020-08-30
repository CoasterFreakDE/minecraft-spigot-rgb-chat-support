package net.melion.rgbchat.chat.placeholders

import net.melion.rgbchat.chat.placeholders.Placeholders.getPlaceholder
import java.util.*


abstract class Placeholder(var identifier: String, var cooldown: Int) {
    private var replacements: Map<String, Any> = HashMap()
    private val outputPlaceholders: MutableList<String> = ArrayList()
    val childs: Array<String?>
        get() = arrayOfNulls(0)

    fun set(s: String): String {
        return try {
            var value = getLastValue()
            if (value == null) value = ""
            var newValue = setPlaceholders(findReplacement(value))
            if (newValue.contains("%value%")) {
                newValue = newValue.replace("%value%", value)
            }
            s.replace(identifier, newValue)
        } catch (t: Throwable) {
            return t.message!!
        }
    }

    private fun findReplacement(originalOutput: String): String {
        if (replacements.isEmpty()) return originalOutput
        if (replacements.containsKey(originalOutput)) {
            return replacements[originalOutput].toString()
        }
        for ((key, value) in replacements) {
            if (key.contains("-")) {
                try {
                    val low = key.split("-".toRegex()).toTypedArray()[0].toFloat()
                    val high = key.split("-".toRegex()).toTypedArray()[1].toFloat()
                    val actualValue = originalOutput.toFloat()
                    if (actualValue in low..high) return value.toString()
                } catch (e: NumberFormatException) {
                    //nope
                }
            }
        }
        return if (replacements.containsKey("else")) replacements["else"].toString() else originalOutput
    }

    private fun setPlaceholders(textInput: String): String {
        var text = textInput
        for (s in outputPlaceholders) {
            if (s == "%value%") continue
            val pl = getPlaceholder(s)
            if (pl != null && text.contains(pl.identifier)) text = pl.set(text)
        }
        return text
    }

    abstract fun getLastValue(): String?
}