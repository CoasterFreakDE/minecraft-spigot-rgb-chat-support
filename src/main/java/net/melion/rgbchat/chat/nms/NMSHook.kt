package net.melion.rgbchat.chat.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*


object NMSHook {
    private val SUPPORTED_VERSIONS = Arrays.asList(
            "v1_5_R1", "v1_5_R2", "v1_5_R3",
            "v1_6_R1", "v1_6_R2", "v1_6_R3",
            "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4",
            "v1_8_R1", "v1_8_R2", "v1_8_R3",
            "v1_9_R1", "v1_9_R2",
            "v1_10_R1",
            "v1_11_R1",
            "v1_12_R1",
            "v1_13_R1", "v1_13_R2",
            "v1_14_R1",
            "v1_15_R1",
            "v1_16_R1", "v1_16_R2"
    )
    var EnumChatFormat: Class<*>? = null
    private var ChatSerializer: Class<*>? = null
    private var PING: Field? = null
    private var PLAYER_CONNECTION: Field? = null
    private var CHANNEL: Field? = null
    private var sendPacket: Method? = null
    private var SERIALIZE: Method? = null
    private var DESERIALIZE: Method? = null
    @Throws(Exception::class)
    fun stringToComponent(json: String): Any? {
        return DESERIALIZE!!.invoke(null, json)
    }

    @Throws(Exception::class)
    fun componentToString(component: Any?): String? {
        return if (component == null) null else SERIALIZE!!.invoke(null, component) as String
    }

    fun getChannel(p: Player): Any? {
        return try {
            val connection = PLAYER_CONNECTION!![p.javaClass.getMethod("getHandle").invoke(p)]
            val networkManager = connection.javaClass.getField("networkManager")[connection]
            CHANNEL!![networkManager]
        } catch (e: Exception) {
            null
        }
    }

    @Throws(Exception::class)
    fun sendPacket(p: Player, nmsPacket: Any?) {
        sendPacket!!.invoke(PLAYER_CONNECTION!![p.javaClass.getMethod("getHandle").invoke(p)], nmsPacket)
    }

    @Throws(Exception::class)
    fun getPing(p: Player): Int {
        return PING!!.getInt(p.javaClass.getMethod("getHandle").invoke(p))
    }

    fun isVersionSupported(serverPackage: String): Boolean {
        return try {
            EnumChatFormat = PacketPlayOut.getNMSClass("EnumChatFormat")
            ChatSerializer = PacketPlayOut.getNMSClass("IChatBaseComponent\$ChatSerializer", "ChatSerializer")
            PING = PacketPlayOut.getField(PacketPlayOut.getNMSClass("EntityPlayer"), "ping")
            PLAYER_CONNECTION = PacketPlayOut.getField(PacketPlayOut.getNMSClass("EntityPlayer"), "playerConnection")
            sendPacket = PacketPlayOut.getNMSClass("PlayerConnection")?.getMethod("sendPacket", PacketPlayOut.getNMSClass("Packet"))
            try {
                //1.7+
                SERIALIZE = ChatSerializer!!.getMethod("a", PacketPlayOut.getNMSClass("IChatBaseComponent"))
                DESERIALIZE = ChatSerializer!!.getMethod("a", String::class.java)

                //1.8+
                CHANNEL = PacketPlayOut.getFields(PacketPlayOut.getNMSClass("NetworkManager"), Channel::class.java).get(0)
            } catch (t: Throwable) {
            }
            SUPPORTED_VERSIONS.contains(serverPackage)
        } catch (e: Throwable) {
            false
        }
    }
}