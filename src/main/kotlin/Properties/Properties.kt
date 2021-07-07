package properties

import InformationFormatter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable


class Properties(
    private val owner : String
) : Serializable {
    private var serverProperties = HashMap<Long, HashMap<PropertiesList, ArrayList<String>>>()
    private var owners = arrayListOf<String>()
    private var admins = arrayListOf<String>()
    private val saveFile = "ServersSettings.properties"
    fun owner() = owner
    init {
        load()
    }

    private fun load(){
        try {
            val objWriter = ObjectInputStream(FileInputStream(saveFile))
            val prop = objWriter.readObject() as Properties
            serverProperties = prop.serverProperties
            owners = prop.owners
            admins = prop.admins
            objWriter.close()
        } catch (e: Exception) {
            InformationFormatter.displayWarning(null,null,null,
                "Couldn't load \"$saveFile\" file!\n" +
                        "Bot will use default properties")
        }
        if(!owners.contains(owner))
            owners.add(owner)
        serverProperties.forEach {
            if(it.value[PropertiesList.Admins]?.contains(owner) != true){
                if(it.value[PropertiesList.Admins] == null)
                    it.value[PropertiesList.Admins]= arrayListOf(owner)
                else
                    it.value[PropertiesList.Admins]?.add(owner)
            }
        }

        checkProperties()
    }

    private fun checkProperties(){
        try {
            serverProperties.forEach { _, u ->
                if (!u.containsKey(PropertiesList.FoundMusicCount))
                    u[PropertiesList.FoundMusicCount] = arrayListOf("5")
                if (!u.containsKey(PropertiesList.Volume))
                    u[PropertiesList.Volume] = arrayListOf("10")
                if (!u.containsKey(PropertiesList.BotPrefix))
                    u[PropertiesList.BotPrefix] = arrayListOf("?")
                if (!u.containsKey(PropertiesList.Admins))
                    u[PropertiesList.Admins] = arrayListOf(owner)
                if (!u.containsKey(PropertiesList.IgnoredUsers))
                    u[PropertiesList.IgnoredUsers] = arrayListOf()
            }
            save()
        } catch (e : Exception) {
            InformationFormatter.displayError(null,null,null,"Check properties failed!",e.stackTrace)
        }
    }

    fun save() {
        try {
            val objWriter = ObjectOutputStream(FileOutputStream(saveFile))
            objWriter.writeObject(this)
            objWriter.close()
        } catch (e: Exception) {
            InformationFormatter.displayError(null,null,null,"Save error", e.stackTrace)
        }
    }

    private fun initDefault(server: Long): Boolean {
        serverProperties[server] = hashMapOf(Pair(PropertiesList.Volume, arrayListOf("10")))
        return if (serverProperties[server] != null) {
            serverProperties[server]!![PropertiesList.FoundMusicCount] = arrayListOf("5")
            serverProperties[server]!![PropertiesList.BotPrefix] = arrayListOf("?")
            serverProperties[server]!![PropertiesList.Admins] = arrayListOf(owner)
            serverProperties[server]!![PropertiesList.IgnoredUsers] = arrayListOf()
            save()
            true
        } else {
            false
        }
    }

    fun get(server: Long, property: PropertiesList): ArrayList<String> = when {
        property == PropertiesList.Owners -> {
            owners
        }

        serverProperties.containsKey(server) -> {
            if (serverProperties[server]!!.containsKey(property)) {
                serverProperties[server]!![property]!!
            } else
                ArrayList()
        }

        else -> {
            if (initDefault(server))
                serverProperties[server]!![property]!!
            else
                ArrayList()
        }
    }

    fun set(server: Long, property: PropertiesList, value: ArrayList<String>) : String {
        val message = when {
            serverProperties.containsKey(server) -> {
                serverProperties[server]!![property] = value
                ""
            }
            else -> {
                if (initDefault(server)) {
                    serverProperties[server]!![property] = value
                    ""
                }
                else {
                    val message = "Error changing property $property for server $server ($value)"
                    InformationFormatter.displayError(null,null,null,message, arrayOf())
                    message
                }

            }
        }
        save()
        return message
    }
}