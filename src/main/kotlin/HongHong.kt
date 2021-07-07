import properties.Properties
import properties.PropertiesList
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL

@kotlin.ExperimentalUnsignedTypes
class HongHong(
    token : String
) {
    val properties : Properties
    val version = "2.0"
    val client = ClientBuilder().withToken(token).build()

    init {
        var loginTimer = 0
        client.apply {
            dispatcher.registerListener(CommandsProcessor(this@HongHong))
            login()
        }
        while(!client.isLoggedIn) {
            if(loginTimer > 10){
                InformationFormatter.displayError(
                    null,
                    null,
                    null,
                    "Login time exceeded! (tried to login for more than 10s)",
                    arrayOf()
                )
            }
            sleep(1000)
            loginTimer++
        }
        properties = Properties(client.applicationOwner.stringID)
    }


    fun prefix(guild: IGuild) = if (properties.get(guild.longID, PropertiesList.BotPrefix).isNotEmpty()) {
        properties.get(guild.longID, PropertiesList.BotPrefix).first()
    } else {
        InformationFormatter.displayWarning(
            null, guild, null, "Bot prefix is null!\n" +
                    "Using emergency prefix: ;?"
        )
        ";?"
    }

    fun startsWithMantion(message : String) : Boolean {
        return if(message.startsWith(client.ourUser.mention()))
            true
        else message.startsWith(client.ourUser.mention(false))
    }

    fun cutMention(message : String) : String {
        return when {
            message.contains(client.ourUser.mention()) -> message.replace(client.ourUser.mention(), "")
            message.contains(client.ourUser.mention(false)) -> message.replace(client.ourUser.mention(false),"")
            else -> message
        }
    }

    fun sendMessageNoBuffer(channel: IChannel, message: String): IMessage? {
        val mess: IMessage?
        try {
            mess = channel.sendMessage(message)
        } catch (e: Exception) {
            InformationFormatter.displayError(
                null,
                channel.guild,
                channel,
                "Cannot send message: $message",
                e.stackTrace
            )
            throw e
        }
        return mess
    }

    fun sendEmbed(channel: IChannel, embed : EmbedObject) : IMessage?{
        return try {
                 channel.sendMessage(embed)
            } catch (e: Exception) {
                InformationFormatter.displayError(
                    null,
                    channel.guild,
                    channel,
                    "Cannot send embed: $embed",
                    e.stackTrace
                )
                null
            }
    }

    fun sendMessage(channel: IChannel, message: String) {
        RequestBuffer.request {
            try {
                channel.sendMessage(message)
            } catch (e: Exception) {
                /*InformationFormatter.displayError(
                    null,
                    channel.guild,
                    channel,
                    "Cannot send message: $message",
                    e.stackTrace
                )*/
                throw e
            }
        }
    }

    fun sendLink(channel: IChannel, link: String) {
        RequestBuffer.request {
            try {
                val connection = URL(link).openConnection() as HttpURLConnection
                connection.responseCode
                sendMessage(channel, connection.url.toString())
                connection.disconnect()
            } catch (e: Exception) {
                InformationFormatter.displayError(
                    null,
                    channel.guild,
                    channel,
                    "Cannot send link: $link",
                    e.stackTrace
                )
                throw e
            }
        }
    }
}