package net.melion.rgbchat.chat

import net.melion.rgbchat.chat.nms.NMSHook
import net.melion.rgbchat.chat.placeholders.Placeholders

enum class EnumChatFormat(val networkId: Int, private val character: Char) {
    BLACK(0, '0', 0x000000), DARK_BLUE(1, '1', 0x0000AA), DARK_GREEN(2, '2', 0x00AA00), DARK_AQUA(3, '3', 0x00AAAA), DARK_RED(4, '4', 0xAA0000), DARK_PURPLE(5, '5', 0xAA00AA), GOLD(6, '6', 0xFFAA00), GRAY(7, '7', 0xAAAAAA), DARK_GRAY(8, '8', 0x555555), BLUE(9, '9', 0x5555FF), GREEN(10, 'a', 0x55FF55), AQUA(11, 'b', 0x55FFFF), RED(12, 'c', 0xFF5555), LIGHT_PURPLE(13, 'd', 0xFF55FF), YELLOW(14, 'e', 0xFFFF55), WHITE(15, 'f', 0xFFFFFF), OBFUSCATED(16, 'k'), BOLD(17, 'l'), STRIKETHROUGH(18, 'm'), UNDERLINE(19, 'n'), ITALIC(20, 'o'), RESET(21, 'r');

    private var nmsEquivalent: Any? = null
    var hexColor = 0
    var red = 0
    var green = 0
    var blue = 0

    constructor(networkId: Int, character: Char, hexColor: Int) : this(networkId, character) {
        this.hexColor = hexColor
        red = hexColor shr 16 and 0xFF
        green = hexColor shr 8 and 0xFF
        blue = hexColor and 0xFF
    }

    fun toNMS(): Any? {
        return nmsEquivalent
    }

    val format: String
        get() = Placeholders.colorChar.toString() + "" + character

    companion object {
        fun getByChar(c: Char): EnumChatFormat? {
            for (format in values()) {
                if (format.character == c) return format
            }
            return null
        }

        fun lastColorsOf(string: String?): EnumChatFormat {
            if (string == null || string.isEmpty()) return RESET
            val last: String = Placeholders.getLastColors(string)
            if (last.isNotEmpty()) {
                val c = last.toCharArray()[1]
                for (e in values()) {
                    if (e.character == c) return e
                }
            }
            return RESET
        }

        fun fromRGBExact(red: Int, green: Int, blue: Int): EnumChatFormat? {
            for (format in values()) {
                if (format.red == red && format.green == green && format.blue == blue) return format
            }
            return null
        }
    }

    init {
        if (NMSHook.EnumChatFormat != null) nmsEquivalent = java.lang.Enum.valueOf(NMSHook.EnumChatFormat as Class<Enum<*>>, toString())
    }
}