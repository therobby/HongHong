import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor
import kotlin.random.Random

@kotlin.ExperimentalUnsignedTypes
class Extra(
    private val bot: HongHong
) {
    private val sectionName = "Extra"

    fun processCommand(commands: ArrayList<String>, event: MessageReceivedEvent): Short {
        with(commands) {
            return when {
                isEmpty() -> {
                    bot.sendMessage(event.channel, "Incomplete command")
                    0
                }
                first().toLowerCase() == "help" -> help(event)
                first().toLowerCase() == "ping" -> ping(event)
                first().toLowerCase() == "meme" -> meme(event)
                first().toLowerCase() == "rng" -> {
                    removeAt(0)
                    when (size) {
                        1 -> {
                            rng(event, first(),"1")
                        }
                        2 -> {
                            if(last().toLowerCase().contains("t")) {
                                rng(event, first(), last().toLowerCase().removePrefix("t"))
                            }
                            else{
                                rng(event, first(), last(), "1")
                            }
                        }
                        3 -> {
                            if(last().toLowerCase().contains("t")){
                                rng(event, first(), this[1], last().toLowerCase().removePrefix("t"))
                            }
                            else {
                                1
                            }
                        }
                        else -> 1
                    }
                }
                first().toLowerCase() == "say" -> {
                    removeAt(0)
                    var message = ""
                    this.forEach {
                        message += "$it "
                    }
                    message.removeSuffix(" ")
                    say(event, message)
                }
                else -> 1
            }
        }
    }

    private fun help(event: MessageReceivedEvent): Short {
        bot.sendEmbed(
            event.channel,
            EmbedBuilder().apply {
                withTitle("Extra Help:")
                withColor(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
                appendField(
                    "${bot.prefix(event.channel.guild)}$sectionName rng [x] [y] [T+How many times (Max: 100)/EMPTY]",
                    "Return random number from x until y",
                    false)
                appendField(
                    "${bot.prefix(event.channel.guild)}$sectionName rng [x] [T+How many times (Max: 100)/EMPTY]",
                    "Returns random number from 0 until x or from x until 0",
                    false)
                appendField(
                    "${bot.prefix(event.channel.guild)}$sectionName say [message]",
                    "Says message",
                    false)
                appendField(
                    "${bot.prefix(event.channel.guild)}$sectionName ping",
                    "Pong",
                    false)
                appendField(
                    "${bot.prefix(event.channel.guild)}$sectionName meme",
                    "9gag.com/random",
                    false)
            }.build()
        )
        /*
        val help = "***%-30s*** %s\n"
            .format(
                "${bot.prefix(event.channel.guild)}$sectionName rng [How many times/EMPTY] [x] [y]",
                "Return random number between x and y"
            ) +
                "***%-30s*** %s\n"
                    .format(
                        "${bot.prefix(event.channel.guild)}$sectionName rng [How many times/EMPTY] [x]",
                        "Returns random number between 0 and x or x and 0"
                    ) +
                "***%-30s*** %s\n"
                    .format("${bot.prefix(event.channel.guild)}$sectionName say [message]", "Says message") +
                "***%-30s*** %s\n"
                    .format("${bot.prefix(event.channel.guild)}$sectionName ping", "Pong") +
                "***%-30s*** %s"
                    .format("${bot.prefix(event.channel.guild)}$sectionName meme", "9gag.com/random")
        bot.sendMessage(event.channel, "```\n$help\n```")*/
        return 0
    }

    private fun say(event: MessageReceivedEvent, message: String): Short {
        bot.sendMessage(event.channel, message)
        return 0
    }

    private fun meme(event: MessageReceivedEvent): Short {
        bot.sendLink(event.channel, "https://9gag.com/random")
        return 0
    }

    private fun ping(event: MessageReceivedEvent): Short {
        val ping = event.message.timestamp.toEpochMilli() - Instant.now().toEpochMilli()
        if (ping > 0)
            bot.sendMessage(event.channel, "Pong!\n$ping ms")
        else
            bot.sendMessage(event.channel, "Pong!\n${-ping} ms")
        return 0
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun rng(event: MessageReceivedEvent, numberA: String, numberB: String, times : String): Short {
        var a: Double
        var b: Double
        val t: UInt
        val results = arrayListOf<String>()

        try {
            t = times.toUInt()
            if(t <= 0.toUInt())
                throw Exception()
        } catch (e: Exception) {
            bot.sendMessage(
                event.channel, "Wrong RNG syntax!\n" +
                        "Found [How many times]: t$times, expected: T+any unsigned integer > 0"
            )
            return 0
        }

        if(t.toInt() > 100){
            bot.sendMessage(event.channel, "Too big [How many times] value! Max 100")
            return 0
        }

        try {
            a = numberA.toDouble()

            b = numberB.toDouble()

        } catch (e: Exception) {
            bot.sendMessage(
                event.channel, "Wrong RNG syntax!\n" +
                        "Found: [x] $numberA and [y] $numberB, expected: any numbers")
            return 0
        }

        if (a > b) {
            val c = a
            a = b
            b = c
        }

        b++

        for(i in 0 until t.toInt()) {
            results.add(if (a == b) {
                a.toString()
            } else if (((a == Math.floor(a)) && !a.isInfinite()) &&
                ((b == Math.floor(b)) && !b.isInfinite())
            ) {
                // INT
                ThreadLocalRandom.current().nextInt(a.toInt(), b.toInt()).toString()
            } else {
                // DOUBLE
                ThreadLocalRandom.current().nextDouble(a, b).toString()
            })
        }

        if(results.size <= 0)
            return 0

        if(results.size < 2){
            bot.sendMessage(event.channel, "RNG number: ${results.first()}")
        }
        else{
            var rng = "RNG numbers:\n"
            for(i in 0 until results.size){
                rng += "**${i+1}.** ${results[i]}\n"
            }
            bot.sendMessage(event.channel, rng)
        }

        return 0
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun rng(event: MessageReceivedEvent, number: String, times : String): Short {
        val a: Double
        val t: UInt
        val results = arrayListOf<String>()

        try {
            t = times.toUInt()
            if(t <= 0.toUInt())
                throw Exception()
        } catch (e: Exception) {
            bot.sendMessage(
                event.channel, "Wrong RNG syntax!\n" +
                        "Found [How many times]: t$times, expected: T+any unsigned integer > 0"
            )
            return 0
        }

        if(t.toInt() > 100){
            bot.sendMessage(event.channel, "Too big [How many times] value! Max 100")
            return 0
        }

        try {
            a = number.toDouble() + 1
        } catch (e: Exception) {
            bot.sendMessage(
                event.channel, "Wrong RNG syntax!\n" +
                        "Found [x]: $number, expected: any number"
            )
            return 0
        }

        for(i in 0 until t.toInt()){
            results.add(if (a < 0) {
                if ((a == floor(a)) && !a.isInfinite())
                    ThreadLocalRandom.current().nextInt(a.toInt(), 0).toString()
                else
                    ThreadLocalRandom.current().nextDouble(a, 0.0).toString()
            } else {
                if ((a == floor(a)) && !a.isInfinite())
                    ThreadLocalRandom.current().nextInt(0, a.toInt()).toString()
                else
                    ThreadLocalRandom.current().nextDouble(0.0, a).toString()
            })
        }

        if(results.size <= 0)
            return 0

        if(results.size < 2){
            bot.sendMessage(event.channel, "RNG number: ${results.first()}")
        }
        else{
            var rng = "RNG numbers:\n"
            for(i in 0 until results.size){
                rng += "**${i+1}.** ${results[i]}\n"
            }
            bot.sendMessage(event.channel, rng)
        }

        return 0
    }
}