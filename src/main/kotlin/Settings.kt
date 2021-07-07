import properties.PropertiesList
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import kotlin.random.Random

@kotlin.ExperimentalUnsignedTypes
class Settings(
    private val bot: HongHong
) {
    private val sectionName = "Settings"
    fun processCommand(commands: ArrayList<String>, event: MessageReceivedEvent): Short {
        if (event.guild == null) {
            bot.sendMessage(
                event.channel,
                "You can use that commands only on servers!"
            )
            return 0
        }

        with(commands) {
            return when {
                isEmpty() -> {
                    bot.sendMessage(event.channel, "Incomplete command")
                    0
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "help" -> help(event)
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "volume" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "show" -> {
                            showVolume(event)
                        }
                        "change" -> {
                            removeAt(0)
                            changeVolume(event, first())
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "findmusiccount" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "show" -> {
                            showFindMusicCount(event)
                        }
                        "change" -> {
                            removeAt(0)
                            changeFindMusicCount(event, first())
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "admins" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "show" -> {
                            showAdmins(event)
                        }
                        "change" -> {
                            removeAt(0)
                            changeAdmins(event, first(), last())
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "ignoredusers" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "show" -> {
                            showIgnoredUsers(event)
                        }
                        "change" -> {
                            removeAt(0)
                            changeIgnoredUsers(event, first(), last())
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "botprefix" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "change" -> {
                            removeAt(0)
                            changeBotPrefix(event, first())
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                else -> 1
            }
        }
    }

    private fun help(event: MessageReceivedEvent): Short {
        val prefix = bot.prefix(event.channel.guild)

        bot.sendEmbed(
            event.channel,
            EmbedBuilder().apply {
                withTitle("Settings Help:")
                withColor(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
                appendField(
                    "$prefix$sectionName Volume [show/change <[0-100]>]",
                    "Change default Volume",
                    false
                )
                /*appendField(
                    "$prefix$sectionName QueueShowListCount [show/change <[ >0 ]>]",
                    "Change displayed queue list count",
                    false
                )*/
                appendField(
                    "$prefix$sectionName Save",
                    "Saves Properties",
                    false
                )
                appendField(
                    "$prefix$sectionName FindMusicCount [show/change <[1-26]>]",
                    "Change default FindMusicCount",
                    false
                )
                appendField(
                    "$prefix$sectionName Admins [show/change <[add/remove] [user]>]",
                    "Manage Admins",
                    false
                )
                appendField(
                    "$prefix$sectionName IgnoredUsers [show/change <[add/remove] [user]>]",
                    "Manage IgnoredUsers",
                    false
                )
                appendField(
                    "$prefix$sectionName BotPrefix change [new prefix]",
                    "Change bot prefix on this server",
                    false
                )
            }.build()
        )
        /*
        val help = "***%-55s*** %s\n"
            .format("$prefix$sectionName Volume [show/change <[0-100]>]", "Change default Volume") +
                "***%-55s*** %s\n"
                    .format(
                        "$prefix$sectionName QueueShowListCount [show/change <[ >0 ]>]",
                        "Change displayed queue list count"
                    ) +
                "***%-55s*** %s\n"
                    .format("$prefix$sectionName Save", "Saves Properties") +
                "***%-55s*** %s\n"
                    .format(
                        "$prefix$sectionName FindMusicCount [show/change <[1-26]>]",
                        "Change default FindMusicCount"
                    ) +
                "***%-55s*** %s\n"
                    .format("$prefix$sectionName Admins [show/change <[add/remove] [user]>]", "Manage Admins") +
                "***%-55s*** %s\n"
                    .format(
                        "$prefix$sectionName IgnoredUsers [show/change <[add/remove] [user]>]",
                        "Manage IgnoredUsers"
                    ) +
                "***%-55s*** %s\n"
                    .format("$prefix$sectionName BotPrefix change [new prefix]", "Change bot prefix on this server")
        bot.sendMessage(event.channel, "```\n$help\n```")*/
        return 0
    }

    private fun changeVolume(event: MessageReceivedEvent, volume: String): Short {
        try {
            var percentage = volume.trim().takeWhile { it.isDigit() }
            if (percentage.length == volume.trim().length) {
                if (percentage.toInt() > 100)
                    percentage = "100"
                else if (percentage.toInt() < 0)
                    percentage = "0"

                bot.properties.set(event.guild.longID, PropertiesList.Volume, arrayListOf(percentage))
                bot.sendMessage(event.channel, "Changed default volume to $percentage%")
            } else
                bot.sendMessage(
                    event.channel,
                    "${volume.trim()} is not a number.\n" +
                            "Enter number between 0-100 to change default music volume!"
                )
        } catch (e: Exception) {
            //e.printStackTrace()
            bot.sendMessage(
                event.channel,
                "${volume.trim()} is not a number.\n" +
                        "Enter number between 0-100 to change default music volume!"
            )
        }
        return 0
    }

    private fun changeFindMusicCount(event: MessageReceivedEvent, count: String): Short {
        try {
            var musicCount = count.trim().takeWhile { it.isDigit() }
            if (musicCount.length == count.trim().length) {
                if (musicCount.toInt() > 26)
                    musicCount = "26"
                else if (musicCount.toInt() < 1)
                    musicCount = "1"

                bot.properties.set(event.guild.longID, PropertiesList.FoundMusicCount, arrayListOf(musicCount))
                bot.sendMessage(event.channel, "Changed default music find count to $musicCount")
            } else
                bot.sendMessage(
                    event.channel,
                    "${count.trim()} is not a number.\n" +
                            "Enter number between 1-26 to change default music find count!"
                )
        } catch (e: Exception) {
            //e.printStackTrace()
            bot.sendMessage(
                event.channel,
                "${count.trim()} is not a number.\n" +
                        "Enter number between 1-26 to change default music find count!"
            )
        }
        return 0
    }

    private fun changeAdmins(event: MessageReceivedEvent, mode: String, user: String): Short {
        when (mode) {
            "add" -> {
                if (user.length > 5) {
                    var digitFlag = true
                    user.takeLast(4).forEach {
                        if (!it.isDigit())
                            digitFlag = false
                    }
                    if (user.takeLast(5).first() == '#' && digitFlag) {
                        var userID = ""
                        event.guild.users.forEach { serverUser ->
                            if (serverUser.name == user.takeWhile { it != '#' } &&
                                serverUser.discriminator == user.takeLast(4))
                                userID = serverUser.stringID
                        }

                        if (userID.isBlank()) {
                            bot.sendMessage(event.channel, "Couldn't find that user!")
                            return 0
                        }

                        if (bot.properties.get(event.guild.longID, PropertiesList.Admins).isEmpty()) {
                            bot.properties.set(event.guild.longID, PropertiesList.Admins, arrayListOf())
                        }
                        if (!bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(user)) {
                            bot.properties.get(event.guild.longID, PropertiesList.Admins).add(userID)
                        } else {
                            bot.sendMessage(event.channel, "This user is already admin!")
                        }
                    } else
                        bot.sendMessage(
                            event.channel,
                            "User name is wrong!\n" +
                                    "User name example: USERNAME#1234"
                        )
                } else
                    bot.sendMessage(event.channel, "User name is too short!")
            }
            "remove" -> {
                if (user.length > 5) {
                    var digitFlag = true
                    user.takeLast(4).forEach {
                        if (!it.isDigit())
                            digitFlag = false
                    }
                    if (user.takeLast(5).first() == '#' && digitFlag) {
                        if (bot.properties.get(event.guild.longID, PropertiesList.Admins).isEmpty()) {
                            bot.sendMessage(event.channel, "There are no admins!")
                        } else {
                            var userID = ""
                            event.guild.users.forEach { serverUser ->
                                if (serverUser.name == user.takeWhile { it != '#' } &&
                                    serverUser.discriminator == user.takeLast(4))
                                    userID = serverUser.stringID
                            }

                            if (userID.isBlank()) {
                                bot.sendMessage(event.channel, "Couldn't find that user!")
                                return 0
                            }

                            if (bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(userID)) {
                                if (user == bot.properties.owner() || bot.properties.get(
                                        event.guild.longID,
                                        PropertiesList.Owners
                                    ).contains(userID)
                                ) {
                                    bot.sendMessage(event.channel, "Can't do this")
                                } else {
                                    bot.properties.get(event.guild.longID, PropertiesList.Admins).remove(userID)
                                }
                            } else {
                                bot.sendMessage(event.channel, "Can't do this, this user wasn't in admin pool")
                            }
                        }
                    } else
                        bot.sendMessage(
                            event.channel,
                            "User name is wrong!\n" +
                                    "User name example: USERNAME#1234"
                        )
                } else
                    bot.sendMessage(event.channel, "User name is too short!")
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Cannot recognize selected mode!\n" +
                            "Supported modes: [add], [remove]"
                )
            }
        }

        return 0
    }

    private fun changeIgnoredUsers(event: MessageReceivedEvent, mode: String, user: String): Short {
        when (mode) {
            "add" -> {
                if (user.length > 5) {
                    var digitFlag = true
                    user.takeLast(4).forEach {
                        if (!it.isDigit())
                            digitFlag = false
                    }
                    if (user.takeLast(5).first() == '#' && digitFlag) {
                        if (bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).isEmpty()) {
                            bot.properties.set(event.guild.longID, PropertiesList.IgnoredUsers, arrayListOf())
                        }
                        var userID = ""
                        event.guild.users.forEach { serverUser ->
                            if (serverUser.name == user.takeWhile { it != '#' } &&
                                serverUser.discriminator == user.takeLast(4))
                                userID = serverUser.stringID
                        }

                        if (userID.isBlank()) {
                            bot.sendMessage(event.channel, "Couldn't find that user!")
                            return 0
                        }

                        if (!bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).contains(userID)) {
                            if (user == bot.properties.owner()) {
                                bot.sendMessage(event.channel, "Can't do this")
                            } else {
                                bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).add(userID)
                            }
                        } else {
                            bot.sendMessage(event.channel, "This user is already ignored!")
                        }
                    } else
                        bot.sendMessage(
                            event.channel,
                            "User name is wrong!\n" +
                                    "User name example: USERNAME#1234"
                        )
                } else
                    bot.sendMessage(event.channel, "User name is too short!")
            }
            "remove" -> {
                if (user.length > 5) {
                    var digitFlag = true
                    user.takeLast(4).forEach {
                        if (!it.isDigit())
                            digitFlag = false
                    }
                    if (user.takeLast(5).first() == '#' && digitFlag) {
                        if (bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).isEmpty()) {
                            bot.sendMessage(event.channel, "There are no ignored users!")
                        } else {
                            var userID = ""
                            event.guild.users.forEach { serverUser ->
                                if (serverUser.name == user.takeWhile { it != '#' } &&
                                    serverUser.discriminator == user.takeLast(4))
                                    userID = serverUser.stringID
                            }

                            if (userID.isBlank()) {
                                bot.sendMessage(event.channel, "Couldn't find that user!")
                                return 0
                            }

                            if (bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).contains(userID)) {
                                bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).remove(userID)
                            } else {
                                bot.sendMessage(event.channel, "Can't do this, this user wasn't in IgnoredUsers pool")
                            }
                        }
                    } else
                        bot.sendMessage(
                            event.channel,
                            "User name is wrong!\n" +
                                    "User name example: USERNAME#1234"
                        )
                } else
                    bot.sendMessage(event.channel, "User name is too short!")
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Cannot recognize selected mode!\n" +
                            "Supported modes: [add], [remove]"
                )
            }
        }

        return 0
    }

    private fun changeBotPrefix(event: MessageReceivedEvent, prefix: String): Short {
        when {
            prefix.length > 3 ->
                bot.sendMessage(event.channel, "Prefix length should be max 3!")
            prefix.length < 0 ->
                bot.sendMessage(event.channel, "Well, don't do that. You shouldn't be able to set empty bot prefix!")
            else -> {
                bot.properties.set(event.guild.longID, PropertiesList.BotPrefix, arrayListOf(prefix))
            }
        }

        return 0
    }

    private fun showVolume(event: MessageReceivedEvent): Short {
        bot.sendMessage(
            event.channel,
            "Default Volume: ${bot.properties.get(event.guild.longID, PropertiesList.Volume).first()}"
        )
        return 0
    }

    private fun showFindMusicCount(event: MessageReceivedEvent): Short {
        bot.sendMessage(
            event.channel,
            "Default Find Music Count: ${bot.properties.get(
                event.guild.longID,
                PropertiesList.FoundMusicCount
            ).first()}"
        )
        return 0
    }

    private fun showAdmins(event: MessageReceivedEvent): Short {
        val cleanUp = arrayListOf<String>()
        var admins = "Admins:\n" +
                "```\n"
        bot.properties.get(event.guild.longID, PropertiesList.Admins).forEach admins@{ strID ->
            var name = ""
            event.guild.users.forEach guild@{
                if (it.stringID == strID) {
                    name = it.name + "#" + it.discriminator
                    return@guild
                }
            }
            if (name.isBlank())
                cleanUp.add(strID)
            else
                admins += "$name\n"
        }
        admins += "```"
        bot.sendMessage(event.channel, admins)
        cleanUp.forEach {
            bot.properties.get(event.guild.longID, PropertiesList.Admins).remove(it)
        }
        return 0
    }

    private fun showIgnoredUsers(event: MessageReceivedEvent): Short {
        val cleanUp = arrayListOf<String>()
        var ignored = "Ignored users:\n" +
                "```\n"
        bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).forEach { strID ->
            var name = ""
            event.guild.users.forEach guild@{
                if (it.stringID == strID) {
                    name = it.name + "#" + it.discriminator
                    return@guild
                }
            }
            if (name.isBlank())
                cleanUp.add(strID)
            else
                ignored += "$name\n"
        }
        ignored += "```"
        bot.sendMessage(event.channel, ignored)
        cleanUp.forEach {
            bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).remove(it)
        }
        return 0
    }
}