# minecraft-spigot-rgb-chat-support
Use RGB Color Codes in Chat. Requires Minecraft Java 1.16.2

Just a simple utility package to send colored rgb text into a minecraft chat.

Supported patterns:
-  `#RRGGBB`, `&#RRGGBB`, `{#RRGGBB}`,  `&x&r&r&g&g&b&b`

Gradients:
- `<colorCode>Text to apply gradient on</otherColorCode>`


Usage:
- `RGBApi.sendChatMessage(spigotPlayer, rawChatMessageWithColorCodes)`


![Chat Gradient](https://i.imgur.com/ASTODE7.png)