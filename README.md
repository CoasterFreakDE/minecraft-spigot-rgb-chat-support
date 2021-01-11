# minecraft-spigot-rgb-chat-support
Use RGB Color Codes in Chat. Requires Minecraft Java 1.16+

Just a simple utility package to send colored rgb text into a minecraft chat.

Supported patterns:
-  `#RRGGBB`, `&#RRGGBB`, `{#RRGGBB}`,  `&x&r&r&g&g&b&b`

Gradients:
- `<colorCode>Text to apply gradient on</otherColorCode>`


Usage:
- `player.sendMessage(*TextComponent.fromLegacyText(RGBApi.toColoredMessage(rawChatMessageWithColorCodes)))`


![Chat Gradient](https://i.imgur.com/ASTODE7.png)

## Import in your Gradle/Maven Project

### Gradle:

#### Add Jitpack as Maven repo
```
repositories {
			...
			maven { url 'https://jitpack.io' }
		}
```

#### Add it to your dependencies
```
dependencies {
    compile 'com.github.F1b3rDEV:minecraft-spigot-rgb-chat-support:1.0.5'
}
```

### Maven:

#### Add Jitpack as Maven repo
```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

#### Add it to your dependencies
```
dependency>
	    <groupId>com.github.F1b3rDEV</groupId>
	    <artifactId>minecraft-spigot-rgb-chat-support</artifactId>
	    <version>1.0.5</version>
	</dependency>
```

