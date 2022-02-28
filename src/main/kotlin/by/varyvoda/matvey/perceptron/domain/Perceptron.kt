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
            val threshold = scanner.nextLine().toInt()
            val clusters = HashMap<Int, Cluster>()

            while (scanner.hasNextLine()) {
                val cluster = scanner.nextLine().split(" ").map { it.toInt() }
                clusters[cluster[0]] =
                    Cluster(cluster[0], Vector(values = cluster.subList(1, cluster.size).toIntArray()))
            }

            return Perceptron(c, threshold, clusters)
        }
    }

    private val c: Int

    private val threshold: Int

    private val clusters: Map<Int, Cluster>

    constructor(c: Int, threshold: Int, weightsSize: Int, clusters: Set<Int>) : this(
        c,
        threshold,
        clusters.stream()
            .collect(
                toMap(Function.identity()) { cluster ->
                    Cluster(cluster, Vector(values = IntArray(weightsSize) { 0 }))
                }
            )
    )


    private constructor(c: Int, threshold: Int, clusters: Map<Int, Cluster>) {
        this.c = c
        this.clusters = clusters
        this.threshold = threshold
    }

    fun evaluateClasses(sample: Vector): Map<Int, Double> {
        val summaryStatistics =
            clusters.entries.stream().mapToInt { sample.scalarMul(it.value.weights) }.summaryStatistics()
        return clusters.entries.stream()
            .collect(
                { HashMap() },
                { acc, entry ->
                    acc[entry.key] = if (summaryStatistics.sum == 0L) 0.0 else sample.scalarMul(entry.value.weights)
                        .toDouble() / summaryStatistics.sum
                },
                { a, b -> a.putAll(b) }
            )
    }

    fun evaluateClass(sample: Vector): Int {
        return evaluateClasses(sample).entries.maxWithOrNull(compareBy { it.value })!!.key
    }

    fun train(sample: Sample) {
        val parentCluster = clusters[sample.clusterId]!!
        var hasMiss = false
        clusters.forEach clusters@{ entry ->
            val cluster = entry.value
            if (sample.clusterId == cluster.id) return@clusters

            if (evaluateClasses(sample.vector)[sample.clusterId]!! < threshold) {
                cluster.weights = cluster.weights.minus(sample.vector.mul(c))
                parentCluster.weights = parentCluster.weights.plus(sample.vector.mul(c))
                hasMiss = true
            }
        }
        if (hasMiss) {
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