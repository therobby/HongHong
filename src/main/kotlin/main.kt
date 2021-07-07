@kotlin.ExperimentalUnsignedTypes
fun main(args : Array<String>) {
    println("/////////////////////////////////////////////\n" +
            "\n" +
            "               STARTING HONGHONG\n" +
            "\n" +
            "/////////////////////////////////////////////")


    if(args.size != 1){
        println("Wrong args count!\n" +
                "Run this like that: java -jar HongHong.jar <token>")
        return
    }

    val bot = HongHong(args.first())

    println("/////////////////////////////////////////////\n" +
            "\n" +
            "               HONGHONG IS RUNNING\n" +
            "               version: ${bot.version}\n" +
            "\n" +
            "/////////////////////////////////////////////")
}