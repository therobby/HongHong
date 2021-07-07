import music.MusicCommands
import properties.PropertiesList
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser


@kotlin.ExperimentalUnsignedTypes
class CommandsProcessor(
    private val bot: HongHong
) {

    private val superOption = HashMap<IUser, ArrayList<String>>()
    private val extra = Extra(bot)
    private val settings = Settings(bot)
    private val oset = OSet(bot)
    private val musicCommands = MusicCommands(bot)

    @EventSubscriber
    fun onMessageReceived(event: MessageReceivedEvent) {
        //println(event.message)
        if (!bot.properties.get(event.guild.ownerLongID, PropertiesList.Owners).contains(event.guild.owner.stringID)) {
            bot.properties.get(event.guild.ownerLongID, PropertiesList.Owners).add(event.guild.owner.stringID)
            if (!bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(event.guild.owner.stringID))
                bot.properties.get(event.guild.longID, PropertiesList.Admins).add(event.guild.owner.stringID)
        }

        if (event.author.isBot)
            return

        var message = event.message.content

        if ((!message.startsWith(bot.prefix(event.guild)) && !bot.startsWithMantion(message)) && superOption[event.author].isNullOrEmpty())
            return

        if ((message.startsWith(bot.prefix(event.guild)) || bot.startsWithMantion(message)) && event.author.isBot) {
            bot.sendMessage(event.channel, "I'm not taking commands from a bot! >:(")
            return
        } else if (bot.properties.get(
                event.guild.longID,
                PropertiesList.IgnoredUsers
            ).contains(event.author.stringID) &&
            !bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(event.guild.owner.stringID)
        )
            return

        message = if (message.startsWith(bot.prefix(event.guild)))
            message.removePrefix(bot.prefix(event.guild))
        else
            bot.cutMention(message)

        InformationFormatter.displayCommandInfo(event.author, event.guild, event.channel, message)

        if (!superOption[event.author].isNullOrEmpty()) {
            superOption[event.author]!!.add(message)
            processCommand(superOption[event.author]!!, event)
            superOption[event.author]!!.clear()
        } else {
            val command = ArrayList<String>()
            message.split(" ").forEach {
                command.add(it)
            }
            //println("superOpt 1: ${superOption[event.author]}")
            processCommand(command, event)
            //println("superOpt 2: ${superOption[event.author]}")
        }
    }

    private fun processCommand(commands: ArrayList<String>, event: MessageReceivedEvent) {
        //println(commands)
        /*if(commands.first() == "test"){
            test(event)
            return
        }*/

        commands.removeIf(String::isEmpty)

        if (commands.size == 1 && commands.first().trim().toLowerCase() == "help")
            return help(event)
        val mc = musicCommands.processCommand(commands, event)
        if (mc == 0.toShort() || mc == 2.toShort()) {
            if(mc == 2.toShort()){
                  superOption[event.author] = arrayListOf("")
            }
            return
        }
        if (commands.first().trim().toLowerCase() == "extra" && extra.processCommand(
                ArrayList(commands.drop(1)),
                event
            ) == 0.toShort()
        )
            return
        if (bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(event.author.stringID))
            if (commands.first().trim().toLowerCase() == "settings" && settings.processCommand(
                    ArrayList(commands.drop(1)),
                    event
                ) == 0.toShort()
            )
                return
        if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID))
            if (commands.first().trim().toLowerCase() == "oset" && oset.processCommand(
                    ArrayList(commands.drop(1)),
                    event
                ) == 0.toShort()
            )
                return
        bot.sendMessage(event.channel, "Invalid Command!\nType `${bot.prefix(event.guild)}help` to get commands list!")
    }

    /*private fun test(event : MessageReceivedEvent){
        val builder = EmbedBuilder().apply {
            withAuthorName("${bot.client.applicationName} Player")

            withColor(0, 0, 255)
            withDesc("Author: Otoczak Wielki")
            withTitle("${if (false) "Paused" else "Playing"}: Ddew")
            appendField("(2:23) |***████████████░░░ 45% ░░░░░░░░░░░░░░░***| (4:32)", "\u200b", false)
            appendField("Queue:", "12 Titles:", false)
            appendField("1. Dupa", "1:34", false)
            appendField("2. dds ", "3:54", false)
            appendField("3. erw ", "45:45", false)
            appendField("4. trb", "1:34", false)
            appendField("5. vbcx ", "3:54", false)
            appendField("6. dsfr ", "45:45", false)
            appendField("7. mnuy", "1:34", false)
            appendField("8. xcvd ", "3:54", false)
            appendField("9. rqr4 ", "45:45", false)
            appendField("10. 65yhg ", "45:45", false)
            withFooterText("Playing time:  1:24:45")

        }
        bot.sendEmbed(event.channel, builder.build())
    }*/

    private fun help(event: MessageReceivedEvent) {
        bot.sendEmbed(
            event.channel,
            musicCommands.help(event).apply {
                appendField(
                    "${bot.prefix(event.guild)}extra",
                    "Extra functions",
                    false
                )
                if (bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(event.author.stringID)) {
                    appendField(
                        "${bot.prefix(event.guild)}settings",
                        "Bot settings",
                        false
                    )
                }
                if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID)) {
                    appendField(
                        "${bot.prefix(event.guild)}oset",
                        "Bot global settings",
                        false
                    )
                }
                withThumbnail("https://www.publicdomainpictures.net/pictures/40000/velka/question-mark.jpg")
                withFooterText("Version: ${bot.version}")
            }.build()
        )
        /*
        val mess = musicCommands.help(event) +
                "%-30s %s\n"
                    .format("${bot.prefix(event.guild)}extra", "Extra functions") +
                (if (bot.properties.get(event.guild.longID, PropertiesList.Admins).contains(event.author.stringID))
                    "%-30s %s\n"
                        .format("${bot.prefix(event.guild)}settings", "Bot settings")
                else
                    "") +
                (if (bot.properties.get(event.guild.longID, PropertiesList.Owners).contains(event.author.stringID))
                    "%-30s %s\n"
                        .format("${bot.prefix(event.guild)}oset", "Bot global settings")
                else
                    "")

        bot.sendMessage(event.channel, "```css\n$mess```")*/
    }
}