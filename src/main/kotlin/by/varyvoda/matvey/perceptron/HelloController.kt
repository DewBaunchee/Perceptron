package by.varyvoda.matvey.perceptron

import by.varyvoda.matvey.perceptron.domain.Perceptron
import by.varyvoda.matvey.perceptron.domain.Sample
import by.varyvoda.matvey.perceptron.domain.Vector
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.io.File
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList


const val cols = 3
const val rows = 5

val activeBackground = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
val nonActiveBackground = Background(BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))
const val activeClass = "active"

class HelloController {

    @FXML
    private lateinit var grid: GridPane

    @FXML
    private lateinit var log: TextArea

    @FXML
    private lateinit var clustersIds: ComboBox<Int>

    private lateinit var perceptron: Perceptron

    private var drawing: Boolean = true

    private val sample: IntArray = IntArray(rows * cols) { 0 }

    private val clusters: ObservableList<Int> = FXCollections.observableList(IntStream.rangeClosed(0, 9).toList())

    @FXML
    fun initialize() {
        for (col in 0 until cols) {
            for (row in 0 until rows) {
                val pane = Pane()
                pane.background = nonActiveBackground

                pane.styleProperty().addListener { _, _, new ->
                    pane.background = if (new == activeClass) activeBackground else nonActiveBackground
                    sample[row * cols + col] = if (new == activeClass) 1 else 0
                }

                val drawing = { pane.style = if (drawing) activeClass else ""; evaluateClasses() }
                pane.setOnMouseDragOver { drawing() }
                pane.setOnMouseClicked { drawing() }

                grid.widthProperty().addListener { _, _, new ->
                    val width = new.toDouble() / cols
                    pane.minWidth = width
                    pane.maxWidth = width
                }

                grid.heightProperty().addListener { _, _, new ->
                    val height = new.toDouble() / rows
                    pane.minHeight = height
                    pane.maxHeight = height
                }

                grid.add(pane, col, row)
            }
        }

        clustersIds.items = clusters
        clustersIds.selectionModel.selectFirst()
        initializePerceptron()
    }

    private fun initializePerceptron() {
        val fileName = "perceptron.txt"
        val file = File(fileName)
        if (file.exists()) {
            perceptron = Perceptron.fromFile(file)
        } else {
            createPerceptron()
        }

        grid.sceneProperty().addListener { _, _, scene ->
            scene.windowProperty().addListener { _, _, window ->
                window.setOnCloseRequest {
                    perceptron.toFile(file)
                }
            }
        }
    }

    @FXML
    private fun onPenClick() {
        drawing = true
    }

    @FXML
    private fun onEraseClick() {
        drawing = false
    }

    @FXML
    private fun onClearClick() {
        grid.children.forEach { it.style = "" }
    }

    @FXML
    private fun onSaveClick() {
        val folderPath: String = "samples/" + clustersIds.selectionModel.selectedItem
        val folder = File(folderPath)
        if (!folder.exists()) folder.mkdirs()

        val file = File(folderPath + "/" + UUID.randomUUID())

        val sb = StringBuilder()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                sb.append(sample[row * cols + col]).append(if (col == cols - 1) "\n" else " ")
            }
        }
        file.writeText(sb.toString())
    }

    @FXML
    private fun onTrainClick() {
        train()
    }

    private fun evaluateClasses() {
        log.text = perceptron.evaluateClasses(Vector(values = sample))
            .entries.stream()
            .collect(
                { HashMap<Int, Int>() },
                { acc, entry -> acc[entry.key] = (entry.value * 100).toInt() },
                { a, b -> a.putAll(b) }
            )
            .entries.joinToString("\n")
    }

    private fun createPerceptron() {
        perceptron = Perceptron(1, rows * cols, clusters.toSet())
        train()
    }

    private fun train() {
        val samplesFolder = File("samples")
        if (!samplesFolder.exists()) return
        val samples = samplesFolder.listFiles()!!.flatMap { folder ->
            if (!folder.exists()) listOf()
            else folder.listFiles()!!.map { file ->
                Sample(
                    Vector(values = file.readText()
                        .split(" ", "\r\n")
                        .map { it.toInt() }
                        .toIntArray()),
                    folder.name.toInt()
                )
            }
        }
        IntStream.iterate(0) { it + 1 }
            .limit(100000)
            .peek {
                println(
                    "Iteration #$it (evaluating '0'): " +
                            perceptron.evaluateClasses(Vector(1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1))
                                .map { entry -> entry.key.toString() + " = " + "%.2f".format(entry.value) })
            }
            .forEach {
                perceptron.train(samples.random())
            }
    }
}
