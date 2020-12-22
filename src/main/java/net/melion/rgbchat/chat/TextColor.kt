package net.melion.rgbchat.chat

class TextColor(hexCode: String) {
    var red: Int
        private set
    var green: Int
        private set
    var blue: Int
        private set

    init {
        val hexColor = hexCode.toInt(16)
        red = hexColor shr 16 and 0xFF
        green = hexColor shr 8 and 0xFF
        blue = hexColor and 0xFF
    }
}
