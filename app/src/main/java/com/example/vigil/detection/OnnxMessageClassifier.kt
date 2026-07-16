package com.example.vigil.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.LongBuffer

enum class MlLabel { SAFE, SCAM, HARASSMENT}

data class MlClassification(val label: MlLabel, val confidence: Float)

/* runs DistilBert SMS classifer fully on device using ONNX Runtime */

class OnnxMessageClassifier(context: Context) : AutoCloseable {
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer = WordPieceTokenizer(context)

    private val labels = arrayOf(MlLabel.SAFE, MlLabel.SCAM, MlLabel.HARASSMENT)

    init {
        val modelBytes = context.assets.open("model_quantized.onnx").use { it.readBytes()}
        session = env.createSession(modelBytes, OrtSession.SessionOptions())
    }

    fun classify(text: String): MlClassification {
        val (inputIds, attentionMask) = tokenizer.tokenize(text)
        val shape = longArrayOf(1, inputIds.size.toLong())

        OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape).use { inputIdsTensor ->
            OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape).use { attentionMaskTensor ->
                val inputs = mapOf(
                    "input_ids" to inputIdsTensor,
                    "attention_mask" to attentionMaskTensor,
                )
                session.run(inputs).use { results ->
                    @Suppress("UNCHECKED_CAST")
                    val logits = (results[0].value as Array<FloatArray>)[0]
                    val probs = softmax(logits)
                    val bestIndex = probs.indices.maxByOrNull { probs[it] }!!
                    return MlClassification(labels[bestIndex], probs[bestIndex])
                }
            }
        }
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.max()
        val exps = logits.map { Math.exp((it - max).toDouble())}
        val sum = exps.sum()
        return exps.map { (it / sum).toFloat()}.toFloatArray()
    }

    override fun close() {
        session.close()
    }
}
