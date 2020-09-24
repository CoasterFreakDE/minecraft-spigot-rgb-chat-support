package net.melion.rgbchat.api

import net.melion.rgbchat.chat.IChatBaseComponent
import net.melion.rgbchat.chat.RGBUtils
import net.melion.rgbchat.chat.nms.ProtocolVersion
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer
import org.bukkit.entity.Player

object RGBApi {

    /**
     * Sends a colored chat message to a player.
     * Supported patterns:
     * #RRGGBB, &#RRGGBB, {#RRGGBB},  &x&r&r&g&g&b&b
     * Gradients:
     * <colorCode>Text to apply gradient on</otherColorCode>
     * @param player Player to send this message to
     * @param rawChatMessage Message with colorCodes
     */
    fun sendChatMessage(player: Player, rawChatMessage: String) {
        val jsonMessage = IChatBaseComponent.fromColoredText(rawChatMessage).toString(ProtocolVersion.v1_16_2)!!
        (player as CraftPlayer).handle.sendMessage(arrayOf(net.minecraft.server.v1_16_R2.IChatBaseComponent.ChatSerializer.a(jsonMessage)))
    }


    /**
     * To colored message
     *
     * @param rawChatMessage
     * @return
     */
    fun toColoredMessage(rawChatMessage: String): String {
        return RGBUtils.toChatColorString(rawChatMessage)
    }

}