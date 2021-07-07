package music
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import HongHong

@kotlin.ExperimentalUnsignedTypes
internal class GuildMusicManager (
    bot : HongHong,
    manager: AudioPlayerManager
) {
    val player: AudioPlayer = manager.createPlayer()

    val audioProvider: AudioProvider
        get() = AudioProvider(player)

    val scheduler: TrackScheduler = TrackScheduler(player, bot)

    init {
        player.addListener(scheduler)
    }
}