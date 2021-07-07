import properties.PropertiesList
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.EmbedBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread
import kotlin.random.Random

@kotlin.ExperimentalUnsignedTypes
class OSet(
    private val bot : HongHong
)  {
    private val sectionName = "OSet"

    fun processCommand(commands: ArrayList<String>, event: MessageReceivedEvent) : Short{
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
                first().trim().toLowerCase() == "save" -> {
                    removeAt(0)
                    if(size >= 1)
                        1
                    else{
                        bot.properties.save()
                        0
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "superadmins" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "show" -> {
                            showSuperAdmins(event)
                        }
                        "change" -> {
                            removeAt(0)
                            if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID)) {
                                if (size != 2) {
                                    bot.sendMessage(
                                        event.channel,
                                        "Wrong syntax!\n" +
                                                "change superAdmins [add/remove] [user]"
                                    )
                                    0
                                } else {
                                    changeSuperAdmins(event, first(), last())
                                }
                            } else
                                1
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////

                first().trim().toLowerCase() == "description" -> {
                    removeAt(0)
                    when (first().toLowerCase()) {
                        "change" -> {
                            removeAt(0)
                            if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID)) {
                                when {
                                    size >= 3 -> {
                                        val status = first()
                                        removeAt(0)
                                        val activity = first()
                                        removeAt(0)
                                        var text = ""
                                        forEach {
                                            text += "$it "
                                        }
                                        text.removeSuffix(" ")

                                        changeDescription(event, status, activity, text)
                                    }
                                    size == 1 ->
                                        changeDescription(event, first(), "", "")
                                    else -> {
                                        bot.sendMessage(
                                            event.channel,
                                            "Wrong syntax!\n" +
                                                    "change description [online/dnd/idle/invisible] [listening/playing/streaming/watching/EMPTY] [text/EMPTY]"
                                        )
                                        0.toShort()
                                    }
                                }
                            } else 1
                        }
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "exec" -> {
                    removeAt(0)
                    if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID)) {
                        if (size > 1) {
                            bot.sendMessage(
                                event.channel,
                                "Wrong syntax!\n" +
                                        "exec [free/df]"
                            )
                            0.toShort()
                        } else {
                            exec(event, first())
                        }
                    } else 1
                }
                ////////////////////////////////////////////////////////////////////////////////
                first().trim().toLowerCase() == "errors" -> {
                    removeAt(0)
                    when(size){
                        0 -> printErrors(event,"","")
                        1 -> printErrors(event, first(),"")
                        2 -> printErrors(event,first(),last())
                        else -> 1
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////
                else -> 1
            }
        }
    }

    private fun help(event: MessageReceivedEvent) : Short {
        val prefix = bot.prefix(event.channel.guild)
        bot.sendEmbed(
            event.channel,
            EmbedBuilder().apply {
                withTitle("Oset Help:")
                withColor(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
                appendField(
                    "$prefix$sectionName SuperAdmins [show/change <[add/remove] [user]>]",
                    "Manage SuperAdmins",
                    false
                    )
                appendField(
                    "$prefix$sectionName Description change [online/dnd/idle/invisible] [listening/playing/streaming/watching/EMPTY] [text/EMPTY]",
                    "Change bot description",
                    false
                )
                appendField(
                    "$prefix$sectionName exec [free/df]",
                    "Show stuff...",
                    false
                )
                appendField(
                    "$prefix$sectionName errors [EMPTY/lines/l/clear/ <[x] [y]>]",
                    "Show last 30 lines from error log, Shows lines count of error log, shows errorlog between x and y lines",
                    false
                )
            }.build()
        )
        /*
        val help = "***%-55s*** %s\n"
            .format("$prefix$sectionName SuperAdmins [show/change <[add/remove] [user]>]", "Manage SuperAdmins") +
         "***%-55s*** %s\n"
            .format(
                "$prefix$sectionName Description change [online/dnd/idle/invisible] [listening/playing/streaming/watching/EMPTY] [text/EMPTY]",
                "Change bot description"
            ) +
                "***%-55s*** %s\n"
                    .format("$prefix$sectionName exec [free/df]", "Show stuff...") +
                "***%-55s*** %s\n"
                    .format("$prefix$sectionName errors", "Show error log")
        bot.sendMessage(event.channel, help)*/
        return 0
    }

    private fun printErrors(event: MessageReceivedEvent, a : String, b : String) : Short{
        println("$a $b")
        if(a.isEmpty() && b.isNotEmpty()) {
            bot.sendMessage(event.channel, "Invalid syntax!")
            return 0
        }
        if(a.isNotEmpty() && b.isEmpty()){
            if(a == "lines" || a == "l"){
                bot.sendMessage(event.channel,"Lines: ${InformationFormatter.readErrorLogLines()}")
            }
            else if(a == "clear"){
                InformationFormatter.clearErrorLog()
                bot.sendMessage(event.channel,"Error log cleared!")
            }
            else{
                bot.sendMessage(event.channel, "Invalid syntax!")
            }
            return 0
        }
        if(a.isNotEmpty() && b.isNotEmpty()){
            var la = try {
                a.toUInt()
            } catch (e : Exception){
                bot.sendMessage(event.channel, "x must be unsigned int!")
                return 0
            }
            var lb = try {
                b.toUInt()
            } catch (e : Exception){
                bot.sendMessage(event.channel, "y must be unsigned int!")
                return 0
            }

            if(la > lb){
                val x = la
                la = lb
                lb = x
            }
            val mess = InformationFormatter.readErrorLog(la,lb)
            try {
                bot.sendMessageNoBuffer(event.channel, "```\n$mess\n```")
            } catch (e: Exception) {
                if (e.localizedMessage.contains("Must be 2000 or fewer in length")) {
                    bot.sendMessage(event.channel, "Message is to long to display! (${mess.length})")
                } else {
                    InformationFormatter
                        .displayError(
                            null,
                            event.guild,
                            event.channel,
                            "Oset errors Error",
                            e.stackTrace
                        )
                }
            }
        }
        else {
            val mess = InformationFormatter.readErrorLog()
            try {
                bot.sendMessageNoBuffer(event.channel, "```\n${mess.replace("```","` `")}\n```")
            } catch (e: Exception) {
                if (e.localizedMessage.contains("Must be 2000 or fewer in length")) {
                    bot.sendMessage(event.channel, "Message is to long to display! (${mess.length})")
                } else {
                    InformationFormatter
                        .displayError(
                            null,
                            event.guild,
                            event.channel,
                            "Oset errors Error",
                            e.stackTrace
                        )
                }
            }
        }
        return 0
    }

    private fun changeDescription(event: MessageReceivedEvent, status: String, activity: String, text: String): Short {
        val statusType: StatusType?
        val activityType: ActivityType?

        when (status) {
            "online" -> {
                statusType = StatusType.ONLINE
            }
            "dnd" -> {
                statusType = StatusType.DND
            }
            "idle" -> {
                statusType = StatusType.IDLE
            }
            "invisible" -> {
                statusType = StatusType.INVISIBLE
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Wrong status!\n" +
                            "Statuses: [online], [dnd], [idle], [invisible]"
                )
                return 0
            }
        }

        when (activity) {
            "listening" -> {
                activityType = ActivityType.LISTENING
            }
            "playing" -> {
                activityType = ActivityType.PLAYING
            }
            "streaming" -> {
                activityType = ActivityType.STREAMING
            }
            "watching" -> {
                activityType = ActivityType.WATCHING
            }
            "" -> {
                activityType = null
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Wrong activity!\n" +
                            "Activities: [listening], [playing], [streaming], [watching]"
                )
                return 0
            }
        }

        if (activityType == null)
            bot.client.changePresence(statusType)
        else
            bot.client.changePresence(statusType, activityType, text)


        return 0
    }


    private fun exec(event: MessageReceivedEvent, command: String): Short {
        bot.sendMessage(event.channel, "Execution started!")
        when (command) {
            "free" -> {
                thread {
                    val buf = execCommand("free -h")
                    var message = ""
                    buf.forEachLine {
                        message += it
                    }
                    val table = arrayListOf<String>()
                    message.trim().split(" ").map {
                        var hasDigit = false
                        it.forEach { char ->
                            if (char.isDigit())
                                hasDigit = true
                        }
                        if (hasDigit)
                            it
                        else
                            ""
                    }.forEach {
                        if (it.isNotBlank())
                            table.add(it)
                    }

                    println(table)
                    bot.sendMessage(
                        event.channel,
                        "Execution output:\n```bash\nAvailable: ${table[5].takeWhile { it != 'i' }}/${table[0]}\n```"
                    )
                }
            }
            "df" -> {
                thread {
                    val buf = execCommand("df -h")
                    var message = ""
                    buf.forEachLine {
                        message += it
                    }
                    bot.sendMessage(event.channel, "Execution output:\n```bash\n$message\n```")
                }
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Wrong syntax!\n" +
                            "exec [free/df]"
                )
            }
        }
        return 0
    }

    private fun execCommand(command: String): BufferedReader {
        val run = Runtime.getRuntime()
        val pr = run.exec(command)
        pr.waitFor()
        return BufferedReader(InputStreamReader(pr.inputStream))
    }

    private fun changeSuperAdmins(event: MessageReceivedEvent, state: String, user: String): Short {
        if (user == bot.properties.owner()) {
            bot.sendMessage(event.channel, "Can't do this!")
            return 0
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

        when (state) {
            "add" -> {
                bot.properties.get(event.guild.longID, PropertiesList.Owners).add(userID)
            }
            "remove" -> {
                bot.properties.get(event.guild.longID, PropertiesList.Owners).remove(userID)
            }
            else -> {
                bot.sendMessage(
                    event.channel,
                    "Wrong syntax!\n" +
                            "change SuperAdmins [add/remove] [user]"
                )
            }
        }
        return 0
    }

    private fun showSuperAdmins(event: MessageReceivedEvent): Short {
        val cleanUp = arrayListOf<String>()
        var sa = "Super Admins:\n" +
                "```\n"
        bot.properties.get(event.guild.longID, PropertiesList.Owners).forEach { strID ->
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
                sa += "$name\n"
        }
        sa += "```"
        bot.sendMessage(event.channel, sa)
        cleanUp.forEach {
            bot.properties.get(event.guild.longID, PropertiesList.IgnoredUsers).remove(it)
        }
        return 0
    }
}