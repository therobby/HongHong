import org.apache.commons.io.output.WriterOutputStream
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@kotlin.ExperimentalUnsignedTypes
object InformationFormatter {
    private const val errorLog = "Logs/ErrorLog.txt"

    private fun date() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

    fun displayCommandInfo(user: IUser, server: IGuild, channel: IChannel, message: String) {
        val command = "Time: ${date()}\n" +
                "User: ${user.name}#${user.discriminator}\n" +
                "Server: ${server.name}\n" +
                "Channel: ${channel.name}\n" +
                "Send command: $message\n" +
                "\n"
        System.out.println(command)
    }

    fun displayInfo(info: String) {
        val information = "Time: ${date()}\n" +
                "Info: $info\n" +
                "\n"
        System.out.println(information)
    }

    fun displayWarning(user: IUser?, server: IGuild?, channel: IChannel?, message: String) {
        var warning = "Time: ${date()}\n"
        if (user != null)
            warning += "User: ${user.name}#${user.discriminator}\n"
        if (server != null)
            warning += "Server: ${server.name}\n"
        if (channel != null)
            warning += "Channel: ${channel.name}\n"

        warning += "Warning: $message\n"
        warning += "\n"
        System.err.println(warning)
    }

    fun displayError(
        user: IUser?,
        server: IGuild?,
        channel: IChannel?,
        message: String,
        stackTrace: Array<StackTraceElement>
    ) {
        saveError(printError(user, server, channel, message, stackTrace))
    }

    private fun saveError(message: String) {
        try {
            //println("${File("Logs").exists()} ${File("Logs").isDirectory}")
            if (!(File("Logs").exists() && File("Logs").isDirectory))
                File("Logs").mkdir()
            val file = BufferedWriter(FileWriter(errorLog, true))
            file.write(message)
            file.close()
        } catch (e: Exception) {
            printError(null, null, null, "Error Log Save Error", e.stackTrace)
        }
    }

    private fun printError(
        user: IUser?,
        server: IGuild?,
        channel: IChannel?,
        message: String,
        stackTrace: Array<StackTraceElement>
    ): String {
        var error = "Time: ${date()}\n"
        if (user != null)
            error += "User: ${user.name}#${user.discriminator}\n"
        if (server != null)
            error += "Server: ${server.name}\n"
        if (channel != null)
            error += "Channel: ${channel.name}\n"

        error += "Error: $message\n"
        error += "StackTrace:\n"
        stackTrace.forEach {
            error += "$it\n"
        }
        error += "\n"
        System.err.println(error)
        return error
    }

    fun clearErrorLog() {
        try {
            //println("${File("Logs").exists()} ${File("Logs").isDirectory}")
            if (!(File("Logs").exists() && File("Logs").isDirectory))
                File("Logs").mkdir()
            val file = BufferedWriter(FileWriter(errorLog, false))
            file.write("")
            file.close()
        } catch (e: Exception) {
            printError(null, null, null, "Error Log clear Error", e.stackTrace)
        }
    }

    fun readErrorLogLines(): Int {
        return try {
            BufferedReader(FileReader(errorLog)).readLines().size
        } catch (e: Exception) {
            0
        }
    }

    fun readErrorLog(x: UInt, y: UInt): String {
        return try {
            val file = BufferedReader(FileReader(errorLog))
            var errors = ""
            val lines = file.readLines()
            if(x.toInt() > lines.size || y.toInt() > lines.size)
                throw Exception("Lines size is lower than provided value (${lines.size})")

            for (i in x until y) {
                errors += "${lines[i.toInt()]}\n"
            }
            //println(errors.length)
            errors
        } catch (e: Exception) {
            e.localizedMessage
        }
    }

    fun readErrorLog(): String {
        return try {
            val file = BufferedReader(FileReader(errorLog))
            var errors = ""
            val lines = file.readLines()
            if (lines.size < 30) {
                lines.forEach {
                    errors += "$it\n"
                }
            } else {
                for (i in lines.size - 1 downTo lines.size - 30) {
                    errors = lines[i] + "\n" + errors
                }
            }
            //println(errors.length)
            errors
        } catch (e: Exception) {
            ""
        }
    }
}