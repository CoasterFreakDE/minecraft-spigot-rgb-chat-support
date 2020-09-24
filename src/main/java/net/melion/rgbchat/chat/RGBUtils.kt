package net.melion.rgbchat.chat

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern


object RGBUtils {
    private val hex = Pattern.compile("#[0-9a-fA-F]{6}")
    private val fix2 = Pattern.compile("\\{#[0-9a-fA-F]{6}\\}")
    private val fix3 = Pattern.compile("\\&x[\\&0-9a-fA-F]{12}")
    private val gradient1 = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>")
    private val gradient2 = Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}")
    fun toHexString(red: Int, green: Int, blue: Int): String {
        var s = Integer.toHexString((red shl 16) + (green shl 8) + blue)
        while (s.length < 6) s = "0$s"
        return s
    }

    fun applyFormats(textInput: String): String {
        var text = textInput
        text = fixFormat1(text)
        text = fixFormat2(text)
        text = fixFormat3(text)
        text = setGradient1(text)
        text = setGradient2(text)
        return text
    }

    fun toChatColorString(textInput: String): String {
        var text = applyFormats(textInput)
        val m = hex.matcher(text)
        while (m.find()) {
            val hexcode = m.group()
            text = text.replace(hexcode, ChatColor.of(hexcode).toString())
        }
        return text
    }

    //&#RRGGBB
    private fun fixFormat1(text: String): String {
        return text.replace("&#", "#")
    }

    //{#RRGGBB}
    private fun fixFormat2(text: String): String {
        var text = text
        val m = fix2.matcher(text)
        while (m.find()) {
            val hexcode = m.group()
            val fixed = hexcode.substring(2, 8)
            text = text.replace(hexcode, "#$fixed")
        }
        return text
    }

    //&x&R&R&G&G&B&B
    private fun fixFormat3(text: String): String {
        var text = text
        text = text.replace('\u00a7', '&')
        val m = fix3.matcher(text)
        while (m.find()) {
            val hexcode = m.group()
            val fixed = String(charArrayOf(hexcode[3], hexcode[5], hexcode[7], hexcode[9], hexcode[11], hexcode[13]))
            text = text.replace(hexcode, "#$fixed")
        }
        return text
    }

    //<#RRGGBB>Text</#RRGGBB>
    private fun setGradient1(text: String): String {
        var text = text
        val m = gradient1.matcher(text)
        while (m.find()) {
            val format = m.group()
            val start = IChatBaseComponent.TextColor(format.substring(2, 8))
            val message = format.substring(9, format.length - 10)
            val end = IChatBaseComponent.TextColor(format.substring(format.length - 7, format.length - 1))
            val applied = asGradient(start, message, end)
            text = text.replace(format, applied)
        }
        return text
    }

    //{#RRGGBB>}text{#RRGGBB<}
    private fun setGradient2(text: String): String {
        var text = text
        val m = gradient2.matcher(text)
        while (m.find()) {
            val format = m.group()
            val start = IChatBaseComponent.TextColor(format.substring(2, 8))
            val message = format.substring(10, format.length - 10)
            val end = IChatBaseComponent.TextColor(format.substring(format.length - 8, format.length - 2))
            val applied = asGradient(start, message, end)
            text = text.replace(format, applied)
        }
        return text
    }

    private fun asGradient(start: IChatBaseComponent.TextColor, text: String, end: IChatBaseComponent.TextColor): String {
        val sb = StringBuilder()
        val length = text.length
        for (i in 0 until length) {
            val red = (start.red + (end.red - start.red).toFloat() / (length - 1) * i).toInt()
            val green = (start.green + (end.green - start.green).toFloat() / (length - 1) * i).toInt()
            val blue = (start.blue + (end.blue - start.blue).toFloat() / (length - 1) * i).toInt()
            sb.append("#" + toHexString(red, green, blue) + text[i])
        }
        return sb.toString()
    }
}