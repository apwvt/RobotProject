import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*

class MyApp: App(FirstView::class)

class FirstView: View() {
    private var instructionPane: Pane by singleAssign()
    private var instructionPalette: Pane by singleAssign()
    private var controller: MyController by singleAssign()

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
                    runAsync {
                        controller.getInstructions()
                    }
                }
            }

            button("Clear") {
                action {
                    instructionPane.children.clear()
                }
            }
        }
    }

    init {
        controller = MyController(instructionPane, instructionPalette)
    }
}


class MyController(private val instructionPane: Pane, private val instructionPalette: Pane): Controller() {
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
         * The types of instruction that the robot can be programmed to recieve.
         */
        enum class RobotInstruction {
            FORWARD, BACKWARD, LEFT, RIGHT, LIGHT, KILL_ALL_HUMANS
        }
    }

    /**
     * The instruction queue to send to the robot.
     */
    private val instructions = FXCollections.observableArrayList<RobotInstruction>()

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

    fun getInstructions() {
        val list = instructionPane.children.filtered { it is InstructionButton }
        for (item in list) {
            println(item)
        }
    }
}

fun main() {
    launch<MyApp>()
}