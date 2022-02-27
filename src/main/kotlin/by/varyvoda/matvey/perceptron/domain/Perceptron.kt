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

    fun evaluateClass(sample: Vector): Map<Int, Double> {
        val summaryStatistics =
            clusters.entries.stream().mapToInt { sample.scalarMul(it.value.weights) }.summaryStatistics()
        return clusters.entries.stream()
            .collect(
                { HashMap() },
                { acc, entry ->
                    acc[entry.key] = if(summaryStatistics.sum == 0L) 0.0 else sample.scalarMul(entry.value.weights).toDouble() / summaryStatistics.sum
                },
                { a, b -> a.putAll(b) }
            )
    }

    fun train(sample: Sample): Boolean {
        if (isMissed(sample)) {
            punish(sample)
            return true
        }
        return false
    }

    private fun isMissed(sample: Sample): Boolean {
        val parentCluster = clusters[sample.clusterId]!!
        clusters.forEach clusters@{ entry ->
            val cluster = entry.value
            if (sample.clusterId == cluster.id) return@clusters

            if (sample.vector.scalarMul(parentCluster.weights) <= sample.vector.scalarMul(cluster.weights)) {
                return true
            }
        }
        return false
    }

    private fun punish(sample: Sample) {
        clusters.forEach clusters@{ entry ->
            val cluster = entry.value
            if (sample.clusterId == cluster.id) {
                cluster.weights = cluster.weights.plus(sample.vector.mul(c))
                return@clusters
            }
            cluster.weights = cluster.weights.minus(sample.vector.mul(c))
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