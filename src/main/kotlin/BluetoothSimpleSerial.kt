import com.fazecast.jSerialComm.SerialPort
import javafx.beans.InvalidationListener
import javafx.beans.Observable

class BluetoothSimpleSerial: Observable {
    private val listeners = arrayListOf<InvalidationListener>()

    override fun removeListener(listener: InvalidationListener?) {
        if (listener == null) return
        listeners.remove(listener)
    }

    override fun addListener(listener: InvalidationListener?) {
        if (listener == null) return
        listeners.add(listener)
    }

    private fun invalidate() {
        listeners.forEach { it.invalidated(this) }
    }

    val ready: Boolean
        get() = port != null
    val ports = arrayListOf<SerialPort>()
    private var port: SerialPort? = null

    fun pollPorts() {
        ports.clear()

        val tPorts = SerialPort.getCommPorts()

        for (port in tPorts) {
            println("Found available port: ${port.descriptivePortName}")
            ports.add(port)
        }

        invalidate()
    }

    fun selectPort(tPort: SerialPort) {
        port = tPort
        println("Selected port: ${tPort.descriptivePortName}")
    }

    fun write(bytes: ByteArray) {
        val tPort = port
        if (tPort == null) {
            println("ERR: Select a port first.")
            return
        }

        if (!tPort.isOpen) {
            tPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 200, 200)
            tPort.openPort()
        }

        tPort.writeBytes(bytes, bytes.size.toLong())
    }

    fun read(bytes: Int): ByteArray {
        val tPort: SerialPort
        if (port == null) {
            println("ERR: Select a port first.")
            return ByteArray(0)
        } else {
            tPort = port ?: return ByteArray(0)
        }

        if (!tPort.isOpen) {
            tPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 200, 200)
            tPort.openPort()
        }

        val buffer = ByteArray(bytes)
        tPort.readBytes(buffer, bytes.toLong())

        return buffer
    }

    fun close(): Boolean {
        val tPort = port ?: return true
        port = null
        return tPort.closePort()
    }

    fun testWrite() {
        val tPort = port ?: return

        tPort.openPort();

        tPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 100, 100)

        val writeArray = ByteArray(1)
        writeArray[0] = '0'.toByte()

        tPort.writeBytes(writeArray, 1L)
    }
}

fun main() {
    val serial = BluetoothSimpleSerial()
}