package net.melion.rgbchat.chat

import net.melion.rgbchat.chat.RGBUtils.applyFormats
import net.melion.rgbchat.chat.RGBUtils.toHexString
import net.melion.rgbchat.chat.nms.ProtocolVersion
import net.melion.rgbchat.chat.placeholders.Placeholders
import org.bukkit.Bukkit
import java.util.*
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


class IChatBaseComponent {
    companion object {
        const val EMPTY_COMPONENT = "{\"translate\":\"\"}"
        private var NBTTagCompound: Class<*>? = null
        private var CraftItemStack_asNMSCopy: Method? = null
        private var ItemStack_save: Method? = null
        fun fromString(json: String?): IChatBaseComponent? {
            return try {
                if (json == null) return null
                val jsonObject: JSONObject = JSONParser().parse(json) as JSONObject
                val component = IChatBaseComponent()
                component.setText(jsonObject["text"] as String)
                component.setBold(jsonObject["bold"] as Boolean)
                component.setItalic(jsonObject["italic"] as Boolean)
                component.setUnderlined(jsonObject["underlined"] as Boolean)
                component.setStrikethrough(jsonObject["strikethrough"] as Boolean)
                component.setObfuscated(jsonObject["obfuscated"] as Boolean)
                component.setColor(TextColor.fromString(jsonObject["color"] as String))
                if (jsonObject.containsKey("clickEvent")) {
                    val clickEvent: JSONObject = jsonObject["clickEvent"] as JSONObject
                    val action = clickEvent["action"] as String
                    val value = clickEvent["value"] as Any
                    component.onClick(ClickAction.valueOf(action.toUpperCase()), value)
                }
                if (jsonObject.containsKey("hoverEvent")) {
                    val hoverEvent: JSONObject = jsonObject["hoverEvent"] as JSONObject
                    val action = hoverEvent["action"] as String
                    val value = hoverEvent["value"] as String
                    component.onHover(HoverAction.valueOf(action.toUpperCase()), value)
                }
                if (jsonObject.containsKey("extra")) {
                    for (extra in jsonObject["extra"] as JSONArray) {
                        component.addExtra(fromString(extra.toString()))
                    }
                }
                component
            } catch (e: ParseException) {
                fromColoredText(json)
            } catch (e: ClassCastException) {
                fromColoredText(json)
            }
        }

        fun fromColoredText(message: String?): IChatBaseComponent {
            var message = message ?: return IChatBaseComponent()
            message = applyFormats(message)
            val components: MutableList<IChatBaseComponent?> = ArrayList()
            var builder = StringBuilder()
            var component = IChatBaseComponent()
            var i = 0
            while (i < message.length) {
                var c = message[i]
                if (c == Placeholders.colorChar || c == '&') {
                    i++
                    if (i >= message.length) {
                        break
                    }
                    c = message[i]
                    if (c in 'A'..'Z') {
                        c = (c + ' '.toInt())
                    }
                    var format: EnumChatFormat? = EnumChatFormat.getByChar(c)
                    if (builder.isNotEmpty()) {
                        component.setText(builder.toString())
                        components.add(component)
                        component = IChatBaseComponent()
                        builder = StringBuilder()
                    }
                    when (format) {
                        EnumChatFormat.BOLD -> component.setBold(true)
                        EnumChatFormat.ITALIC -> component.setItalic(true)
                        EnumChatFormat.UNDERLINE -> component.setUnderlined(true)
                        EnumChatFormat.STRIKETHROUGH -> component.setStrikethrough(true)
                        EnumChatFormat.OBFUSCATED -> component.setObfuscated(true)
                        EnumChatFormat.RESET -> {
                            format = EnumChatFormat.WHITE
                            component = IChatBaseComponent()
                            component.setColor(TextColor(format))
                        }
                        else -> {
                            component = IChatBaseComponent()
                            component.setColor(format?.let { TextColor(it) })
                        }
                    }
                } else if (c == '#') {
                    try {
                        val hex = message.substring(i + 1, i + 7)
                        val color = TextColor(hex) //the validation check is in constructor
                        if (builder.isNotEmpty()) {
                            component.setText(builder.toString())
                            components.add(component)
                            component = IChatBaseComponent()
                            builder = StringBuilder()
                        }
                        component = IChatBaseComponent()
                        component.setColor(color)
                        i += 6
                    } catch (e: Exception) {
                        //invalid hex code
                        builder.append(c)
                    }
                } else {
                    builder.append(c)
                }
                i++
            }
            component.setText(builder.toString())
            components.add(component)
            return IChatBaseComponent("").setExtra(components)
        }

        fun optimizedComponent(text: String?): IChatBaseComponent {
            return if (text != null && (text.contains("#") || text.contains("&x") || text.contains(Placeholders.colorChar.toString() + "x"))) fromColoredText(text) else IChatBaseComponent(text)
        }

        init {
            try {
                val pack = Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).toTypedArray()[3]
                NBTTagCompound = Class.forName("net.minecraft.server.$pack.NBTTagCompound")
                CraftItemStack_asNMSCopy = Class.forName("org.bukkit.craftbukkit.$pack.inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack::class.java)
                ItemStack_save = Class.forName("net.minecraft.server.$pack.ItemStack").getMethod("save", NBTTagCompound)
            } catch (t: Throwable) {
            }
        }
    }

    var text: String? = null
        private set
    var color: TextColor? = null
        private set
    private var bold: Boolean? = null
    private var italic: Boolean? = null
    private var underlined: Boolean? = null
    private var strikethrough: Boolean? = null
    private var obfuscated: Boolean? = null
    var clickAction: ClickAction? = null
        private set
    var clickValue: Any? = null
        private set
    var hoverAction: HoverAction? = null
        private set
    var hoverValue: String? = null
        private set
    private var extra: MutableList<IChatBaseComponent?>? = null
    private val jsonObject: JSONObject = JSONObject()

    constructor() {}
    constructor(text: String?) {
        setText(text)
    }

    fun getExtra(): List<IChatBaseComponent?>? {
        return extra
    }

    fun setExtra(components: MutableList<IChatBaseComponent?>?): IChatBaseComponent {
        extra = components
        jsonObject.put("extra", extra)
        return this
    }

    fun addExtra(child: IChatBaseComponent?): IChatBaseComponent {
        if (extra == null) {
            extra = ArrayList()
            jsonObject.put("extra", extra)
        }
        extra!!.add(child)
        return this
    }

    fun isBold(): Boolean {
        return if (bold == null) false else bold!!
    }

    fun isItalic(): Boolean {
        return if (italic == null) false else italic!!
    }

    fun isUnderlined(): Boolean {
        return if (underlined == null) false else underlined!!
    }

    fun isStrikethrough(): Boolean {
        return if (strikethrough == null) false else strikethrough!!
    }

    fun isObfuscated(): Boolean {
        return if (obfuscated == null) false else obfuscated!!
    }

    fun setText(text: String?): IChatBaseComponent {
        this.text = text
        if (text != null) {
            jsonObject.put("text", text)
        } else {
            jsonObject.remove("text")
        }
        return this
    }

    fun setColor(color: TextColor?): IChatBaseComponent {
        this.color = color
        return this
    }

    fun setBold(bold: Boolean?): IChatBaseComponent {
        this.bold = bold
        if (bold != null) {
            jsonObject.put("bold", bold)
        } else {
            jsonObject.remove("bold")
        }
        return this
    }

    fun setItalic(italic: Boolean?): IChatBaseComponent {
        this.italic = italic
        if (italic != null) {
            jsonObject.put("italic", italic)
        } else {
            jsonObject.remove("italic")
        }
        return this
    }

    fun setUnderlined(underlined: Boolean?): IChatBaseComponent {
        this.underlined = underlined
        if (underlined != null) {
            jsonObject.put("underlined", underlined)
        } else {
            jsonObject.remove("underlined")
        }
        return this
    }

    fun setStrikethrough(strikethrough: Boolean?): IChatBaseComponent {
        this.strikethrough = strikethrough
        if (strikethrough != null) {
            jsonObject.put("strikethrough", strikethrough)
        } else {
            jsonObject.remove("strikethrough")
        }
        return this
    }

    fun setObfuscated(obfuscated: Boolean?): IChatBaseComponent {
        this.obfuscated = obfuscated
        if (obfuscated != null) {
            jsonObject.put("obfuscated", obfuscated)
        } else {
            jsonObject.remove("obfuscated")
        }
        return this
    }

    fun onClickOpenUrl(url: String): IChatBaseComponent {
        return onClick(ClickAction.OPEN_URL, url)
    }

    fun onClickRunCommand(command: String): IChatBaseComponent {
        return onClick(ClickAction.RUN_COMMAND, command)
    }

    fun onClickSuggestCommand(command: String): IChatBaseComponent {
        return onClick(ClickAction.SUGGEST_COMMAND, command)
    }

    fun onClickChangePage(newpage: Int): IChatBaseComponent {
        return onClick(ClickAction.CHANGE_PAGE, newpage)
    }

    private fun onClick(action: ClickAction, value: Any): IChatBaseComponent {
        clickAction = action
        clickValue = value
        val click = JSONObject()
        click.put("action", action.toString().toLowerCase())
        click.put("value", value)
        jsonObject.put("clickEvent", click)
        return this
    }

    fun onHoverShowText(text: String): IChatBaseComponent {
        return onHover(HoverAction.SHOW_TEXT, text)
    }

    fun onHoverShowItem(item: ItemStack): IChatBaseComponent {
        return onHover(HoverAction.SHOW_ITEM, serialize(item))
    }

    private fun serialize(item: ItemStack): String {
        return try {
            ItemStack_save?.invoke(CraftItemStack_asNMSCopy?.invoke(null, item), NBTTagCompound!!.getConstructor().newInstance()).toString()
        } catch (t: Throwable) {
            t.printStackTrace()
            "null"
        }
    }

    fun onHoverShowEntity(id: UUID, customname: String?, type: String?): IChatBaseComponent {
        val json = JSONObject()
        json.put("id", id.toString())
        if (type != null) json.put("type", type)
        if (customname != null) json.put("name", customname)
        return onHover(HoverAction.SHOW_ENTITY, json.toString())
    }

    private fun onHover(action: HoverAction, value: String): IChatBaseComponent {
        hoverAction = action
        hoverValue = value
        val hover = JSONObject()
        hover.put("action", action.toString().toLowerCase())
        hover.put("value", value)
        jsonObject.put("hoverEvent", hover)
        return this
    }

    fun toString(clientVersion: ProtocolVersion): String? {
        if (extra == null) {
            if (text == null) return null
            if (text!!.isEmpty()) return EMPTY_COMPONENT
        }
        //the core component, fixing all colors
        if (color != null) {
            jsonObject["color"] = color!!.toString(clientVersion)
        }
        if (extra != null) {
            for (extra in extra!!) {
                if (extra!!.color != null) {
                    extra.jsonObject["color"] = extra.color!!.toString(clientVersion)
                }
            }
        }
        return toString()
    }

    override fun toString(): String {
        return if (ProtocolVersion.SERVER_VERSION.minorVersion >= 7) {
            //1.7+
            jsonObject.toString()
        } else {
            val text = toColoredText()
            if (ProtocolVersion.SERVER_VERSION.minorVersion == 6) {
                //1.6.x
                val jsonObject = JSONObject()
                jsonObject["text"] = text
                jsonObject.toString()
            } else {
                //1.5.x
                text
            }
        }
    }

    fun toColoredText(): String {
        val builder = StringBuilder()
        if (color != null) builder.append(color!!.legacy?.format)
        if (isBold()) builder.append(EnumChatFormat.BOLD?.format)
        if (isItalic()) builder.append(EnumChatFormat.ITALIC?.format)
        if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE?.format)
        if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH?.format)
        if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED?.format)
        if (text != null) builder.append(text)
        if (extra != null) {
            for (component in extra!!) {
                builder.append(component!!.toColoredText())
            }
        }
        return builder.toString()
    }

    fun toRawText(): String {
        val builder = StringBuilder()
        if (text != null) builder.append(text)
        if (extra != null) {
            for (extra in extra!!) {
                if (extra!!.text != null) builder.append(extra.text)
            }
        }
        return builder.toString()
    }

    enum class ClickAction {
        OPEN_URL, OPEN_FILE,  //Cannot be sent by server
        RUN_COMMAND, TWITCH_USER_INFO,  //Removed in 1.9
        CHANGE_PAGE, SUGGEST_COMMAND, COPY_TO_CLIPBOARD //since 1.15
    }

    enum class HoverAction {
        SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY, SHOW_ACHIEVEMENT //Removed in 1.12
    }

    class TextColor {
        var red: Int
            private set
        var green: Int
            private set
        var blue: Int
            private set
        var legacy: EnumChatFormat? = null

        constructor(legacy: EnumChatFormat) {
            red = legacy.red
            green = legacy.green
            blue = legacy.blue
            this.legacy = legacy
        }

        constructor(hexCode: String) {
            val hexColor = hexCode.toInt(16)
            red = hexColor shr 16 and 0xFF
            green = hexColor shr 8 and 0xFF
            blue = hexColor and 0xFF
            var minDist = 9999.0
            var dist: Double
            for (color in EnumChatFormat.values()) {
                val r = Math.pow((color.red - red).toDouble(), 2.0).toInt()
                val g = Math.pow((color.green - green).toDouble(), 2.0).toInt()
                val b = Math.pow((color.blue - blue).toDouble(), 2.0).toInt()
                dist = Math.sqrt(r + g + b.toDouble())
                if (dist < minDist) {
                    minDist = dist
                    legacy = color
                }
            }
        }

        fun toString(clientVersion: ProtocolVersion): String {
            return if (clientVersion.minorVersion >= 16) {
                val legacyEquivalent: EnumChatFormat? = EnumChatFormat.fromRGBExact(red, green, blue)
                //not sending old colors as RGB to 1.16 clients if not needed, also viaversion blocks that as well
                legacyEquivalent?.toString()?.toLowerCase() ?: "#" + toHexString(red, green, blue)
            } else {
                legacy.toString().toLowerCase()
            }
        }

        companion object {
            fun fromString(string: String?): TextColor? {
                if (string == null) return null
                return if (string.startsWith("#")) {
                    TextColor(string.substring(1))
                } else {
                    TextColor(EnumChatFormat.valueOf(string.toUpperCase()))
                }
            }
        }
    }
}