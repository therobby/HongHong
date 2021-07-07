package music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import sx.blah.discord.handle.obj.IChannel

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

import HongHong
import InformationFormatter

@kotlin.ExperimentalUnsignedTypes
internal class TrackScheduler(
    private val player: AudioPlayer,
    private val bot: HongHong
) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    fun queue(track: AudioTrack) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun isQueueEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        super.onTrackStart(player, track)
        InformationFormatter.displayInfo("Started playing: ${track?.info?.title}")
    }

    fun secToTime(seconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(seconds)
        val min = TimeUnit.MILLISECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(hours)
        val sec = TimeUnit.MILLISECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(min) - TimeUnit.HOURS.toSeconds(hours)

        return (if (hours >= 10) hours.toString() else "0$hours") +
                ":" +
                (if (min >= 10) min.toString() else "0$min") +
                ":" +
                if (sec >= 10) sec.toString() else "0$sec"
    }

    fun getNext10(): ArrayList<AudioTrack> {
        val next10 = arrayListOf<AudioTrack>()
        while(next10.size < 10 && next10.size < queue.size) {
            next10.add(queue.elementAt(next10.size))
        }
        return next10
    }

    fun getFullLength(): String {
        var length = 0.toLong()
        queue.forEach {
            length += it.info.length
        }

        return secToTime(length)
    }

    /*
    fun showQueue(channel: IChannel) {
        var queueList = ""
        if (player.playingTrack != null) {
            queueList += "Playing:\n" +
                    "```fix\n" +
                    "${player.playingTrack.info.title}\n" +
                    "Time: %s/%s Author: %10s\n```".format(
                        secToTime(player.playingTrack.position),
                        secToTime(player.playingTrack.duration),
                        player.playingTrack.info.author
                    ) +
                    "\n\n"
        }

        queueList += "QUEUE(${queue.size}):\nQUEUE START\n"
        if (queue.isNotEmpty()) {
            var i = 0

            thread {
                queue.forEach {
                    var length = queueList.length
                    var title = "```md\n${++i}. ${it.info.title}\n" +
                            "Time: %-10s Author: %s\n```".format(secToTime(it.info.length), it.info.author)
                    //println(length)

                    length += title.length
                    if (length <= 1500) {
                        queueList += title
                        //println(queueList[queueList.lastIndex])
                    } else {
                        bot.sendMessage(channel, queueList)
                        Thread.sleep(1000)
                        title = "\n$title"
                        queueList = title
                    }

                    if(i >= bot.properties.get(channel.guild.longID, "QueueShowListCount").first().toInt())
                        return@forEach
                }
                if(i != queue.size){
                    if(queueList.isNotBlank())
                        bot.sendMessage(channel, "$queueList\nAND ${queue.size - i} MORE")
                    else
                        bot.sendMessage(channel, "AND ${queue.size - i} MORE")
                }
                else {
                    if(queueList.isNotBlank())
                        bot.sendMessage(channel, "$queueList\nQUEUE END")
                    else
                        bot.sendMessage(channel, "QUEUE END")
                }
            }

            /*thread {
                queueList.forEach {
                    bot.sendMessage(channel, it)
                    Thread.sleep(2200)
                }
                bot.sendMessage(channel, "QUEUE END")
            }*/
        } else {
            bot.sendMessage(channel, "Empty")
        }
    }*/

    fun clearQueue(channel: IChannel) {
        queue.clear()
        bot.sendMessage(channel, "Queue cleared!")
    }

    fun titleOf(nr: Int): String {
        return if (queue.size > nr || nr > 0) {
            queue.toList()[nr].info.title
        } else {
            if (queue.size > nr)
                "Number is bigger than queue count"
            else
                "Number is lower than queue count"
        }
    }

    fun lengthOf(nr: Int): String {
        return if (queue.size > nr || nr > 0) {
            secToTime(queue.toList()[nr].info.length)
        } else {
            if (queue.size > nr)
                "Number is bigger than queue count"
            else
                "Number is lower than queue count"
        }
    }

    fun queueCount(): Int = queue.size

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack(channel: IChannel?) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if (channel != null)
            bot.sendMessage(
                channel, "Skipping: \n" +
                        "```fix\n" +
                        "${player.playingTrack.info.title}\n" +
                        "Time: %s Author: %10s\n```".format(
                            secToTime(player.playingTrack.duration),
                            player.playingTrack.info.author
                        )
            )
        player.startTrack(queue.poll(), false)
    }

    fun skipTo(channel: IChannel, nr: Int) {
        if (nr < 0 || nr >= queue.size) {
            bot.sendMessage(channel, "Invalid track number!")
            return
        }
        for (i in 0 until nr)
            queue.poll()
        if (queue.isEmpty())
            bot.sendMessage(channel, "Queue is cleared!")
        else {
            player.startTrack(queue.poll(), false)
            bot.sendMessage(
                channel, "Skipping to:\n" +
                        "```fix\n" +
                        "${player.playingTrack.info.title}\n" +
                        "Time: %s Author: %10s\n```".format(
                            secToTime(player.playingTrack.duration),
                            player.playingTrack.info.author
                        )
            )
        }
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason!!.mayStartNext) {
            nextTrack(null)
        }
        InformationFormatter.displayInfo("Stopped playing ${track?.info?.title}")
    }
}