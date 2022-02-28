package by.varyvoda.matvey.perceptron

import by.varyvoda.matvey.perceptron.domain.Perceptron
import by.varyvoda.matvey.perceptron.domain.Sample
import by.varyvoda.matvey.perceptron.domain.Vector

fun main() {
    val perceptron = Perceptron(1, 3, setOf(1, 2, 3))
    perceptron.train(Sample(Vector(0, 0, 1), 1))
    perceptron.train(Sample(Vector(1, 1, 1), 2))
    perceptron.train(Sample(Vector(-1, 1, 1), 3))
    perceptron.train(Sample(Vector(0, 0, 1), 1))
    perceptron.train(Sample(Vector(1, 1, 1), 2))
    perceptron.train(Sample(Vector(-1, 1, 1), 3))
    println(perceptron)
}