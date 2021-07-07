package music
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.VoiceChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IVoiceChannel
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import HongHong
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.util.EmbedBuilder
import kotlin.random.Random

@kotlin.ExperimentalUnsignedTypes
internal class MusicInfoUpdater(
    private val event : MessageReceivedEvent,
    private val guildMusicManager: GuildMusicManager,
    private val voiceChannel : IVoiceChannel,
    private val bot : HongHong
) {
    private val trackScheduler = guildMusicManager.scheduler
    private var working = true
    private var message : IMessage? = null

    init {
        val builder = EmbedBuilder().apply {
            withAuthorName("${bot.client.applicationName} Player")
            withColor(0, 0, 255)
            withDesc("Stopped!")
        }
        messageUpdate(builder.build())
        start()
    }

    private fun botNick() : String {
        var botNickname = ""
        event.guild.users.forEach {
            if(it.isBot && it.stringID == bot.client.applicationClientID){
                botNickname = it.name
            }
        }
        return botNickname
    }

    private fun update(){
        try {
            val newTitle = guildMusicManager.player.playingTrack.info.title
            val currentTime = guildMusicManager.player.playingTrack.position
            val maxTime = guildMusicManager.player.playingTrack.info.length
            val next10 = trackScheduler.getNext10()
            val author = guildMusicManager.player.playingTrack.info.author
            val percentage = (currentTime * 100 / maxTime)
            val progression = (percentage * 20) / 100
            var progress = ""

            for (i in 0 until 21) {
                progress += when {
                    i == 10 -> when {
                        percentage < 10 -> " 0$percentage% "
                        else -> " %$percentage "
                    }
                    i < progression -> '█'
                    else -> '░'
                }
            }

            val builder = EmbedBuilder().apply {
                withAuthorName("${botNick()} Player")
                withAuthorUrl(guildMusicManager.player.playingTrack.info.uri)
                //withThumbnail("https://img.youtube.com/vi/${guildMusicManager.player.playingTrack.info.identifier}/default.jpg")

                withColor(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))//0, 0, 255)
                withDesc("Author: $author")
                if (newTitle.length > 48)
                    withTitle("${if (guildMusicManager.player.isPaused) "Paused" else "Playing"}: ${newTitle.take(45)}...")
                else
                    withTitle("${if (guildMusicManager.player.isPaused) "Paused" else "Playing"}: $newTitle")
                appendField(
                    "(${trackScheduler.secToTime(currentTime)}) [$progress] (${trackScheduler.secToTime(maxTime)})",
                    "Volume: ${guildMusicManager.player.volume}%",
                    false
                )
                if (trackScheduler.queueCount() > 0) {
                    appendField("Queue:", "${trackScheduler.queueCount()} ${if(trackScheduler.queueCount() < 2) "Track" else "Tracks"}:", false)
                    for (i in 0 until next10.size)
                        appendField(
                            "${i + 1}. ${next10[i].info.title}",
                            trackScheduler.secToTime(next10[i].info.length),
                            false
                        )
                    withFooterText("Playing time:  ${trackScheduler.getFullLength()}")
                }
            }
            messageUpdate(builder.build())
        } catch (e : Exception) {
            InformationFormatter.displayError(null,event.guild,event.channel,"Information message error!", e.stackTrace)
        }
        //bot.sendEmbed(event.channel, builder.build())
    }

    private fun start() {
        thread {
            sleep(1000)
            var stopped = false
            while(working){
                if(!guildMusicManager.player.isPaused && guildMusicManager.player.playingTrack != null){
                    stopped = false
                    try{
                        update()
                    } catch (e : Exception){
                        e.printStackTrace()
                    }
                }
                else if(guildMusicManager.player.playingTrack == null && !stopped) {
                    val builder = EmbedBuilder().apply {
                        withAuthorName("${bot.client.applicationName} Player")
                        withColor(0, 0, 255)
                        withDesc("Stopped!")

                    }
                    messageUpdate(builder.build())
                    stopped = true
                }
                if(!voiceChannel.isConnected)
                    stop()
                sleep(1000)
            }
        }
    }

    private fun messageUpdate(embed : EmbedObject) {
        if(message != null) {
            if (message!!.channel.messageHistory.latestMessage != message){
                sleep(2000)
                message!!.delete()
                message = bot.sendEmbed(event.channel, embed)
            } else {
                message!!.edit(embed)
            }
        } else {
            message = bot.sendEmbed(event.channel, embed)
        }
    }

    fun stop() {
        working = false
        message?.delete()
    }
}