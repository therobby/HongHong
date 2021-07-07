package music

import HongHong
import InformationFormatter
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import properties.PropertiesList
import sx.blah.discord.handle.audio.IAudioManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.random.Random

@kotlin.ExperimentalUnsignedTypes
class MusicCommands(
    private val bot: HongHong
) {

    private val sectionName = "Music"
    private val playerManager: AudioPlayerManager
    private val musicManagers: MutableMap<Long, GuildMusicManager>
    private var findOption = HashMap<IUser, ArrayList<FindContainer>>()
    private val liveView = HashMap<Long, MusicInfoUpdater>()

    init {
        this.musicManagers = HashMap()

        this.playerManager = DefaultAudioPlayerManager()
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun processCommand(commands: ArrayList<String>, event: MessageReceivedEvent): Short {
        if (findOption[event.author] == null || if (findOption[event.author] != null) findOption[event.author]!!.isEmpty() else true) {
            with(commands) {
                return when {
                    size == 1 -> {
                        when (first().trim().toLowerCase()) {
                            /*"help" -> {
                                help(event)
                            }*/
                            "volume" -> {
                                bot.sendMessage(
                                    event.channel,
                                    "${getGuildAudioPlayer(event.channel.guild).player.volume}%"
                                )
                                0
                            }
                            "skip" -> {
                                skipTrack(event.channel)
                            }
                            "leave" -> {
                                leave(event)
                            }
                            "pause" -> {
                                pause(event.channel)
                            }
                            "resume" -> {
                                resume(event.channel)
                            }
                            "stop" -> {
                                stop(event.channel)
                            }
                            /*"queue" -> {
                                queue(event.channel)
                            }
                            "queuecount" -> {
                                queueCount(event.channel)
                            }*/
                            "join" -> {
                                join(event)
                            }
                            "play" -> {
                                play(event.channel)
                            }
                            /*
                            "playing" -> {
                                playing(event.channel)
                            }*/
                            "clear" -> {
                                clearQueue(event.channel)
                            }
                            "playkonon" -> {
                                loadAndPlay(
                                    "https://www.youtube.com/playlist?list=PLROnc04njwrCT-7u1qbfemRhP8WNsJDPq",
                                    event
                                )
                            }
                            else -> 1
                        }
                    }
                    first().trim().toLowerCase() == "titleof" -> {
                        removeAt(0)
                        titleOf(first(), event)
                    }
                    first().trim().toLowerCase() == "skipto" -> {
                        removeAt(0)
                        skipTrack(event.channel, first())
                    }
                    first().trim().toLowerCase() == "playkonon" -> {
                        removeAt(0)
                        if (size == 1 && first() == "shuffle")
                            loadAndPlay(
                                "https://www.youtube.com/playlist?list=PLROnc04njwrCT-7u1qbfemRhP8WNsJDPq",
                                event,
                                true
                            )
                        else {
                            bot.sendMessage(
                                event.channel, "Wrong syntax!\n" +
                                        "playkonon [shuffle/EMPTY]"
                            )
                        }
                        0
                    }
                    first().trim().toLowerCase() == "volume" -> {
                        removeAt(0)
                        volume(event.channel, first())
                    }
                    first().trim().toLowerCase() == "play" -> {
                        removeAt(0)
                        when (size) {
                            1 -> {
                                if (extractYTId(first()) != null) {
                                    loadAndPlay(first(), event)
                                } else {
                                    var title = ""
                                    val link = first()
                                    if (link.contains("&feature")) {
                                        link.replaceAfter("&feature", "")
                                        link.replace("&feature", "")
                                    }
                                    link.forEach {
                                        title += "$it "
                                    }
                                    title.removeSuffix(" ")
                                    if (findAndPlay(title, event) == null) {
                                        2.toShort()
                                    } else
                                        0
                                }
                            }
                            2 -> {
                                if (last() == "shuffle")
                                    if (extractYTId(first()) != null) {
                                        loadAndPlay(first(), event, true)
                                    } else {
                                        var title = ""
                                        val link = first()
                                        if (link.contains("&feature")) {
                                            link.replaceAfter("&feature", "")
                                            link.replace("&feature", "")
                                        }
                                        link.forEach {
                                            title += "$it "
                                        }
                                        title.removeSuffix(" ")
                                        if (findAndPlay(title, event) == null) {
                                            2.toShort()
                                        } else
                                            0
                                    }
                                else {
                                    bot.sendMessage(
                                        event.channel, "Wrong syntax!\n" +
                                                "play [url] [shuffle/EMPTY]"
                                    )
                                    0
                                }
                            }
                            else -> {
                                bot.sendMessage(
                                    event.channel, "Wrong syntax!\n" +
                                            "play [url] [shuffle/EMPTY]"
                                )
                                0
                            }
                        }
                    }
                    first().trim().toLowerCase() == "seek" -> {
                        removeAt(0)
                        seek(event, first())
                    }
                    else -> 1
                }
            }
        } else {
            val digit = commands.last().takeWhile { it.isDigit() }
            val number = if (digit.isNotBlank()) digit.toInt() else 0
            if (number > 0 && number <= findOption[event.author]!!.size) {
                loadAndPlay("https://www.youtube.com${findOption[event.author]!![number - 1].link}", event)
            } else if (number == 0) {
                bot.sendMessage(event.channel, "Aborted!")
            } else {
                bot.sendMessage(event.channel, "Bad song number")
            }
            findOption[event.author]?.clear()
            return 0
        }
    }

    fun help(event: MessageReceivedEvent): EmbedBuilder {
        val prefix = bot.prefix(event.guild)
        return EmbedBuilder().apply {
            withColor(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
            withTitle("Help:")
            appendField(
                "${prefix}play [url] [shuffle/EMPTY]",
                "Plays or adds to queue provided url",
                false)
            appendField(
                "${prefix}play [title]",
                "Searches for title in youtube and adds it to queue or plays it",
                false
            )
            appendField(
                "${prefix}playkonon [shuffle/EMPTY]",
                "Plays Kononowicz playlist",
                false)
            appendField(
                "${prefix}skip",
                "Skips current song",
                false)
            appendField(
                "${prefix}skipTo [nr] [shuffle/EMPTY]",
                "Skips current song to selected song",
                false)
            appendField(
                "${prefix}titleOf [nr]",
                "Shows title of track that has [nr] number in queue",
                false)
            appendField(
                "${prefix}volume [0-100/EMPTY]",
                "Changes volume/Shows volume",
                false)
            appendField(
                "${prefix}stop",
                "Stops playing",
                false)
            appendField(
                "${prefix}pause",
                "Pauses song",
                false)
            appendField(
                "${prefix}resume",
                "Resumes song",
                false)
            appendField(
                "${prefix}clear",
                "Clears queue",
                false)
            appendField(
                "${prefix}join",
                "Joins to voice channel",
                false)
            appendField(
                "${prefix}leave",
                "Leaves voice channel",
                false)
            appendField(
                "${prefix}seek [MIN:SEC]",
                "Seeks to specified position",
                false)
        }
    }

    @Synchronized
    private fun getGuildAudioPlayer(guild: IGuild): GuildMusicManager {
        val guildId = guild.longID
        var musicManager: GuildMusicManager? = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(bot, playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.audioProvider = musicManager.audioProvider

        return musicManager
    }

    private fun extractYTId(ytUrl: String): String? {
        var vId: String? = null
        val pattern = Pattern.compile(
            "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(ytUrl)
        if (matcher.matches()) {
            vId = matcher.group(1)
        }
        return vId
    }

    private fun seek(event: MessageReceivedEvent, seek: String): Short {
        val track = getGuildAudioPlayer(event.channel.guild).player.playingTrack
        if (track.isSeekable) {
            when {
                seek.contains(":") && !seek.contains('-') -> {
                    val splited = seek.split(":")
                    var flag = false
                    splited.forEach {
                        if (!isInt(it))
                            flag = true
                    }
                    return if (flag || splited.size > 2) {
                        bot.sendMessage(
                            event.channel,
                            "I don't understand what you want me to do"
                        )
                        0
                    } else {
                        val pos =
                            TimeUnit.MINUTES.toMillis(splited[0].toLong()) + TimeUnit.SECONDS.toMillis(splited[1].toLong())
                        track.position = pos
                        0
                    }
                }
                else -> {
                    bot.sendMessage(
                        event.channel,
                        "I don't understand what you want me to do"
                    )
                }
            }
        } else {
            bot.sendMessage(
                event.channel,
                "This track doesn't support seeking :/"
            )
            return 0
        }
        return 0
    }

    private fun isInt(str: String): Boolean {
        return try {
            str.toInt()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun playing(channel: IChannel): Short {
        val track = getGuildAudioPlayer(channel.guild).player.playingTrack
        val trackScheduler = getGuildAudioPlayer(channel.guild).scheduler
        return if (track != null) {
            val playing = "Currently playing:\n```fix\n${track.info.title}\n" +
                    "Time: %s/%s Author: %10s\n```".format(
                        trackScheduler.secToTime(track.position),
                        trackScheduler.secToTime(track.duration),
                        track.info.author
                    )
            bot.sendMessage(channel, playing)
            0
        } else {
            bot.sendMessage(channel, "Nothing is playing")
            0
        }
    }

    private fun leave(event: MessageReceivedEvent): Short {
        val channel = event.client.ourUser.getVoiceStateForGuild(event.guild).channel
        return if (channel != null) {
            getGuildAudioPlayer(channel.guild).player.isPaused = false
            liveView[event.guild.longID]?.stop()
            channel.leave()
            clearQueue(event.channel)   // bo ni umią clearować queue
            getGuildAudioPlayer(event.guild).scheduler.nextTrack(null)
            0
        } else {
            bot.sendMessage(event.channel, "Already left!")
            0
        }

    }

    private fun join(event: MessageReceivedEvent): Short {
        connectToFirstVoiceChannel(event.channel.guild.audioManager, event)
        return 0
    }

    private fun pause(channel: IChannel): Short {
        if (!getGuildAudioPlayer(channel.guild).player.isPaused)
            getGuildAudioPlayer(channel.guild).player.isPaused = true
        return 0
    }

    private fun resume(channel: IChannel): Short {
        if (getGuildAudioPlayer(channel.guild).player.isPaused)
            getGuildAudioPlayer(channel.guild).player.isPaused = false
        return 0
    }

    private fun stop(channel: IChannel): Short {
        val player = getGuildAudioPlayer(channel.guild).player
        player.stopTrack()
        return 0
    }

    /*
    private fun queue(channel: IChannel): Short {
        val scheduler = getGuildAudioPlayer(channel.guild).scheduler
        if (!silent)
            scheduler.showQueue(channel)
        return scheduler.queueCount().toString()
    }*/

    private fun clearQueue(channel: IChannel): Short {
        val musicPlayer = getGuildAudioPlayer(channel.guild)
        musicPlayer.scheduler.clearQueue(channel)
        return 0
    }

    private fun queueCount(channel: IChannel): Short {
        val scheduler = getGuildAudioPlayer(channel.guild).scheduler
        bot.sendMessage(channel, scheduler.queueCount().toString())
        return 0
    }

    private fun play(channel: IChannel): Short {
        val musicPlayer = getGuildAudioPlayer(channel.guild)

        with(musicPlayer) {
            return when {
                player.playingTrack != null -> {
                    musicPlayer.player.playTrack(musicPlayer.player.playingTrack)
                    0
                }
                player.isPaused -> {
                    pause(channel)
                }
                scheduler.isQueueEmpty() -> {
                    bot.sendMessage(channel, "There is nothing to play!")
                    0
                }
                else -> {
                    musicPlayer.scheduler.nextTrack(null)
                    0
                }
            }
        }
    }

    private fun volume(channel: IChannel, command: String): Short {
        try {
            var percentage = command.trim().takeWhile { it.isDigit() }
            return if (percentage.length == command.trim().length) {
                if (percentage.toInt() > 100)
                    percentage = "100"
                else if (percentage.toInt() < 0)
                    percentage = "0"

                getGuildAudioPlayer(channel.guild).player.volume = percentage.toInt()
                bot.sendMessage(channel, "Volume set to $percentage%")
                0
            } else {
                bot.sendMessage(
                    channel,
                    "${command.trim()} is not a number.\nEnter number between 0-100 to change music volume!"
                )
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    private fun findAndPlay(trackName: String, event: MessageReceivedEvent): Boolean? {
        val name = trackName.replace(" ", "+")
        val entity = HttpClients
            .createDefault()
            .execute(HttpPost("https://www.youtube.com/results?search_query=$name"))
            .entity

        return if (entity != null) {
            val result = String(entity.content.readBytes())

            //println(result)
            findOption[event.author] = ArrayList()
            //println()

            val matcher = Pattern.compile("<h3 class=\"yt-lockup-title \"><a href=(.){20,9000}</h3>").matcher(result)
            while (matcher.find()) {
                if (findOption[event.author]!!.size >=
                    if (bot.properties.get(event.guild.longID, PropertiesList.FoundMusicCount).isNotEmpty())
                        bot.properties.get(event.guild.longID, PropertiesList.FoundMusicCount).first().toInt()
                    else
                        3
                )
                    break

                //println("#############################")
                //println(matcher.group())

                var entrance = matcher.group().removePrefix("<h3 class=\"yt-lockup-title \"><a href=\"")
                    .removeSuffix(".</span></h3>")
                val time = entrance.takeLastWhile { it != ' ' }
                entrance = entrance.replaceAfter("\" rel=", "").replace("\" rel=", "")
                val link = entrance.takeWhile { it != '\"' }
                val title = entrance
                    .replaceBefore("  title=\"", "")
                    .replace("  title=\"", "")
                    .replaceAfter(" aria-describedby=\"description-id-", "")
                    .replace(" aria-describedby=\"description-id-", "")
                    .replace("&amp;", "&")
                println("Link: $link Title: $title Time: $time")
                if (!time.contains("</span></h3>") && link.drop(1).take(4) != "user")
                    findOption[event.author]?.add(FindContainer(link, title, time))
            }
            findOption[event.author]?.forEach {
                println("Title: ${it.title} Time: ${it.time} ")
            }

            var message = "Search results:\n" +
                    "```md\n" +
                    "0. Abort\n"
            var flag = 1
            findOption[event.author]?.forEach {
                message += "${flag++}. Title: ${it.title} Time: ${it.time}\n"
            }
            message += "```"

            bot.sendMessage(event.channel, message)
            null
        } else {
            bot.sendMessage(event.channel, "Search failed!")
            false
        }

    }

    private fun loadAndPlay(trackUrl: String, event: MessageReceivedEvent, shuffle: Boolean = false): Short {
        val musicManager = getGuildAudioPlayer(event.channel.guild)

        bot.sendMessage(event.channel, "Processing request")

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                bot.sendMessage(event.channel, "Added to queue `${track.info.title}`")

                play(event.channel.guild, musicManager, track, event)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (shuffle)
                    playlist.tracks.shuffle()

                var firstTrack: AudioTrack? = playlist.selectedTrack

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                ///
                //println(playlist.tracks.size)
                ///

                bot.sendMessage(
                    event.channel,
                    "Adding to queue ${playlist.tracks.size} songs from playlist `${playlist.name}`.\n" +
                            "First track: `${firstTrack!!.info.title}`"
                )

                //play(channel.guild, musicManager, firstTrack)

                playlist.tracks.forEach {
                    play(event.channel.guild, musicManager, it, event)
                }
            }

            override fun noMatches() {
                bot.sendMessage(event.channel, "Nothing found by `$trackUrl`")
            }

            override fun loadFailed(exception: FriendlyException) {
                bot.sendMessage(event.channel, "Could not play: " + exception.message)
            }
        })
        return 0
    }

    private fun play(guild: IGuild, musicManager: GuildMusicManager, track: AudioTrack?, event: MessageReceivedEvent) {
        connectToFirstVoiceChannel(guild.audioManager, event)

        musicManager.scheduler.queue(track!!)

        getGuildAudioPlayer(guild).player.volume =
            if (bot.properties.get(event.guild.longID, PropertiesList.Volume).isNotEmpty())
                bot.properties.get(event.guild.longID, PropertiesList.Volume).first().toInt()
            else
                100
    }

    private fun titleOf(nr: String, event: MessageReceivedEvent): Short {
        return try {
            val title = getGuildAudioPlayer(event.guild).scheduler.titleOf(nr.toInt() - 1)
            bot.sendMessage(event.channel, "Title: $title")
            0
        } catch (e: Exception) {
            bot.sendMessage(event.channel, "Found $nr, expected: any integer")
            0
        }
    }

    private fun skipTrack(channel: IChannel): Short {
        val musicManager = getGuildAudioPlayer(channel.guild)
        musicManager.scheduler.nextTrack(channel)
        return 0
    }

    private fun skipTrack(channel: IChannel, nr: String): Short {
        return if (isInt(nr)) {
            val musicManager = getGuildAudioPlayer(channel.guild)
            musicManager.scheduler.skipTo(channel, nr.toInt() - 1)
            0
        } else {
            bot.sendMessage(channel, "Track number is not a number")
            0
        }
    }

    private fun connectToFirstVoiceChannel(audioManager: IAudioManager, event: MessageReceivedEvent) {
        for (voiceChannel in audioManager.guild.voiceChannels) {
            if (voiceChannel.isConnected) {
                return
            }
        }

        var joined = false

        audioManager.guild.voiceChannels.forEach {
            if (it.connectedUsers.contains(event.author)) {
                try {
                    joined = true
                    it.join()
                    liveView[event.guild.longID]?.stop()
                    liveView[event.guild.longID] =
                        MusicInfoUpdater(event, getGuildAudioPlayer(event.guild), it, bot)

                    // leave if there is no one in voice channel
                    thread {
                        Thread.sleep(5000)
                        while (it.connectedUsers.size > 1) Thread.sleep(1000)

                        if (it.isConnected && !getGuildAudioPlayer(event.guild).scheduler.isQueueEmpty() && joined) {
                            clearQueue(event.channel)   // bo ni umią clearować queue
                            getGuildAudioPlayer(event.guild).scheduler.nextTrack(null)
                        }
                        liveView[event.guild.longID]?.stop()
                        it.leave()
                        joined = false
                    }
                } catch (e: Exception) {
                    InformationFormatter.displayError(
                        event.author,
                        event.guild,
                        event.channel,
                        "Cannot enter voice channel",
                        e.stackTrace
                    )
                }
            }
        }

        if (!joined) {
            bot.sendMessage(event.channel, "Cannot enter voice channel. Maybe you're not on voice channel?")
        }
    }
}