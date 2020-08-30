package net.melion.rgbchat.chat.placeholders

import java.text.DecimalFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object Placeholders {
    private val placeholderPattern: Pattern = Pattern.compile("%([^%]*)%")
    val decimal2 = DecimalFormat("#.##")
    const val colorChar = '\u00a7'

    //all placeholders used in all configuration files, including invalid ones
    var allUsedPlaceholderIdentifiers: MutableList<String> = ArrayList()

    //plugin internals + PAPI + API
    var registeredPlaceholders: MutableMap<String, Placeholder> = HashMap<String, Placeholder>()
    var usedPlaceholders: MutableSet<Placeholder?> = HashSet<Placeholder?>()
    private val allPlaceholders: Collection<Placeholder>
        get() = registeredPlaceholders.values

    fun getPlaceholder(identifier: String): Placeholder? {
        return registeredPlaceholders[identifier]
    }

    private fun detectAll(text: String?): List<String> {
        val placeholders: MutableList<String> = ArrayList()
        if (text == null) return placeholders
        val m: Matcher = placeholderPattern.matcher(text)
        while (m.find()) {
            placeholders.add(m.group())
        }
        return placeholders
    }

    fun getUsedPlaceholderIdentifiersRecursive(line: String?): Set<String> {
        val base: MutableSet<String> = HashSet(detectAll(line))
        for (placeholder in base.toTypedArray()) {
            val placeHolders: List<Placeholder> = detectPlaceholders(placeholder)
            for (placeH in placeHolders) {
                base.add(placeH.identifier)
            }
        }
        return base
    }

    //code taken from bukkit, so it can work on bungee too
    fun color(textToTranslate: String): String {
        if (!textToTranslate.contains("&")) return textToTranslate
        val b = textToTranslate.toCharArray()
        for (i in 0 until b.size - 1) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = colorChar
                b[i + 1] = Character.toLowerCase(b[i + 1])
            }
        }
        return String(b)
    }

    //code taken from bukkit, so it can work on bungee too
    fun getLastColors(input: String): String {
        var result = ""
        val length = input.length
        for (index in length - 1 downTo -1 + 1) {
            val section = input[index]
            if (section == colorChar && index < length - 1) {
                val c = input[index + 1]
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(c.toString() + "")) {
                    result = colorChar.toString() + "" + c + result
                    if ("0123456789AaBbCcDdEeFfRr".contains(c.toString() + "")) {
                        break
                    }
                }
            }
        }
        return result
    }

    private fun detectPlaceholders(rawValue: String?): List<Placeholder> {
        if (rawValue == null || !rawValue.contains("%")) return ArrayList<Placeholder>()
        val placeholdersTotal: MutableList<Placeholder> = ArrayList<Placeholder>()
        for (placeholder in allPlaceholders) {
            if (rawValue.contains(placeholder.identifier)) {
                placeholdersTotal.add(placeholder)
                for (child in placeholder.childs) {
                    for (p in detectPlaceholders(child)) {
                        placeholdersTotal.add(p)
                    }
                }
            }
        }
        return placeholdersTotal
    }

    fun findAllUsed(obj: Any?) {
        if (obj is Map<*, *>) {
            for (value in (obj).values) {
                findAllUsed(value)
            }
        }
        if (obj is List<*>) {
            for (line in obj) {
                findAllUsed(line)
            }
        }
        if (obj is String) {
            for (placeholder in detectAll(obj as String?)) {
                if (!allUsedPlaceholderIdentifiers.contains(placeholder)) allUsedPlaceholderIdentifiers.add(placeholder)
            }
        }
    }
}