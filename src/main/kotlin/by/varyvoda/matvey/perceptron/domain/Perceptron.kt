package by.varyvoda.matvey.perceptron.domain

import java.io.File
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors.toMap

class Perceptron {

    companion object {

        fun fromFile(file: File): Perceptron {
            val scanner = Scanner(file)
            val c = scanner.nextLine().toInt()
            val clusters = HashMap<Int, Cluster>()

            while (scanner.hasNextLine()) {
                val cluster = scanner.nextLine().split(" ").map { it.toInt() }
                clusters[cluster[0]] =
                    Cluster(cluster[0], Vector(values = cluster.subList(1, cluster.size).toIntArray()))
            }

            return Perceptron(c, clusters)
        }
    }

    private val c: Int

    private val clusters: Map<Int, Cluster>

    constructor(c: Int, weightsSize: Int, clusters: Set<Int>) : this(
        c,
        clusters.stream()
            .collect(
                toMap(Function.identity()) { cluster ->
                    Cluster(cluster, Vector(values = IntArray(weightsSize) { 0 }))
                }
            )
    )


    private constructor(c: Int, clusters: Map<Int, Cluster>) {
        this.c = c
        this.clusters = clusters
    }

    fun evaluateClasses(sample: Vector): Map<Int, Double> {
        val evaluated: Map<Int, Int> = clusters.entries.stream()
            .collect(
                { HashMap() },
                { acc, entry ->
                    acc[entry.key] = sample.scalarMul(entry.value.weights)
                },
                { a, b -> a.putAll(b) }
            )
        val summaryStatistics = evaluated.entries.stream().mapToInt { it.value }.summaryStatistics()
        val scale = Scale(
            domainFrom = summaryStatistics.min.toDouble(),
            domainTo = summaryStatistics.max.toDouble(),
            realFrom = 0.0,
            realTo = 1.0
        )
        return evaluated.mapValues { entry -> scale.scale(entry.value.toDouble()) }
    }

    fun train(sample: Sample) {
        val parentCluster = clusters[sample.clusterId]!!
        var hasMiss = false
        clusters.forEach clusters@{ entry ->
            val cluster = entry.value
            if (sample.clusterId == cluster.id) return@clusters

            if (parentCluster.weights.scalarMul(sample.vector) <= cluster.weights.scalarMul(sample.vector)) {
                cluster.weights = cluster.weights.minus(sample.vector.mul(c))
                hasMiss = true
            }
        }
        if (hasMiss) {
            parentCluster.weights = parentCluster.weights.plus(sample.vector.mul(c))
        }
    }

    fun toFile(file: File) {
        file.writeText(toString())
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(c.toString()).append("\n")
        clusters.forEach {
            sb.append(it.key.toString())
                .append(" ")
                .append(it.value.weights.toString())
                .append("\n")
        }
        return sb.toString()
    }
}