package net.melion.rgbchat.api

import net.melion.rgbchat.chat.RGBUtils

object RGBApi {
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