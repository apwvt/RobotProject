import com.fazecast.jSerialComm.SerialPort
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*
import javafx.collections.ObservableList

class MyApp: App(FirstView::class)

class FirstView: View() {
    var instructionPane: Pane by singleAssign()
    var instructionPalette: Pane by singleAssign()
    var statusLabel: Label by singleAssign()
    var portBox: ComboBox<SerialPort> by singleAssign()
    private var controller: MyController by singleAssign()

    val selectedPortProperty = SimpleObjectProperty<SerialPort>()

    override val root = vbox {
        label("Doom Robot Control Program") {
            minWidth = this@vbox.width //Full width of parent
            minHeight = 50.0
        }

        hbox {
            instructionPane = flowpane {
                minWidth = 450.0
                minHeight = 450.0
                hgap = 10.0
                vgap = 10.0

                background = Background(BackgroundFill(
                    Color.AZURE,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                ))
            }

            instructionPalette = vbox {
                minWidth = 150.0
            }
        }

        /* Bottom row of the UI */
        hbox {
            //Tell the controller to send the data to the arduino
            button("Send Instructions") {
                action {
                    // runAsync here in case this blocks and
                    // freezes the UI
                    runAsync {
                        controller.sendInstructions()
                    }
                }
            }

            button("Clear") {
                action {
                    controller.clearInstructions()
                }
            }

            statusLabel = label("Status: Not Connected")
            portBox = combobox(selectedPortProperty)

            button("Refresh Ports") {
                action {
                    controller.btSerial.pollPorts()
                }
            }
        }
    }

    init {
        controller = MyController(this)
        portBox.items = controller.ports
        selectedPortProperty.addListener { _, _, newVal ->
            val value = newVal ?: return@addListener
            controller.btSerial.selectPort(value)
        }
    }
}


class MyController(private val robotView: FirstView): Controller() {
    companion object {
        /**
         * The 'nice' representations of the RobotInstruction class.
         */
        val nameMap = mapOf(
            RobotInstruction.FORWARD to "Forward",
            RobotInstruction.BACKWARD to "Backward",
            RobotInstruction.LEFT to "Turn Left",
            RobotInstruction.RIGHT to "Turn Right",
            RobotInstruction.LIGHT to "Turn On Light",
            RobotInstruction.KILL_ALL_HUMANS to "Kill All Humans"
        )

        /**
         * The types of instruction that the robot can be programmed to receive.
         */
        enum class RobotInstruction {
            NOTHING, FORWARD, BACKWARD, LEFT, RIGHT, LIGHT, KILL_ALL_HUMANS
        }
    }

    private val instructionPane = robotView.instructionPane
    private val instructionPalette = robotView.instructionPalette

    /**
     * The instruction queue to send to the robot.
     */
    private val instructions = FXCollections.observableArrayList<RobotInstruction>()

    /**
     * The ports list for the serial connection
     */
    val ports: ObservableList<SerialPort> by lazy {
        FXCollections.observableArrayList<SerialPort>()
    }

    //Set up the change listener to update the instruction pane
    init {
        instructions.addListener { _: ListChangeListener.Change<out RobotInstruction> ->
            instructionPane.children.clear()

            for ((i, inst) in instructions.withIndex()) {
                val newButton = InstructionButton(inst, this, i)
                instructionPane.children.add(newButton)
            }
        }

        //Add a button in the instruction palette for each instruction
        enumValues<RobotInstruction>().forEach { i ->
            val newButton = InstructionSelectionButton(i, this)
            instructionPalette.children.add(newButton)
        }
    }

    /**
     * Buttons in the instruction palette for adding instructions.
     */
    class InstructionSelectionButton(
        private val robotInstruction: RobotInstruction,
        private val controller: MyController
    ): Button() {
        init {
            text = nameMap[robotInstruction]

            minHeight = 50.0
            maxHeight = 50.0

            minWidth = 150.0
            maxWidth = 150.0

            action {
                controller.instructions.add(robotInstruction)
            }
        }
    }

    /**
     * Buttons representing a single instruction for the robot to execute.
     */
    class InstructionButton(
        robotInstruction: RobotInstruction,
        private val controller: MyController,
        private val index: Int
    ): Button() {
        init {
            text = nameMap[robotInstruction]

            minHeight = 100.0
            maxHeight = 100.0

            minWidth = 100.0
            maxWidth = 100.0

            action {
                controller.instructions.removeAt(index)
            }
        }
    }

    // Bluetooth tunnel
    val btSerial = BluetoothSimpleSerial()

    init {
        btSerial.addListener {
            this.ports.clear()
            for (port in btSerial.ports) {
                this.ports.add(port)
            }
        }

        btSerial.pollPorts()
    }

    fun sendInstructions() {
        if (!btSerial.ready) {
            println("ERR: Ensure that the serial connection is ready to write!")
            return
        }

        val bytes = ByteArray(instructions.size + 1)
        bytes[0] = instructions.size.toByte()

        for (i in 1..instructions.size)
            bytes[i] = instructions.get(i - 1).ordinal.toByte()

        println("Instruction packet size: ${bytes[0] + 1}")

        btSerial.write(bytes)
    }

    fun clearInstructions() {
        instructions.clear()
    }
}

fun main() {
    launch<MyApp>()
}