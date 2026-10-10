package com.example.vigil.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.LongBuffer

enum class MlLabel { SAFE, SCAM, HARASSMENT}

data class MlClassification(val label: MlLabel, val confidence: Float)

/**
 * probs is [safe, scam, harassment]. Flags the stronger threat class when it beats "safe" by at least
 * [threshold] of their combined weight. At 0.5 this is plain argmax, so default behavior is unchanged.
 */
internal fun decide(probs: FloatArray, threshold: Float): MlClassification {
    val safe = probs[0]
    val threatIndex = if (probs[1] >= probs[2]) 1 else 2
    val threat = probs[threatIndex]
    val total = threat + safe
    if (total > 0f && threat / total >= threshold) {
        return MlClassification(MlLabel.values()[threatIndex], threat)
    }
    return MlClassification(MlLabel.SAFE, safe)
}

/* runs DistilBert SMS classifer fully on device using ONNX Runtime */

class OnnxMessageClassifier(context: Context) : AutoCloseable {
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer = WordPieceTokenizer(context)

    init {
        val modelBytes = context.assets.open("model_quantized.onnx").use { it.readBytes()}
        session = env.createSession(modelBytes, OrtSession.SessionOptions())
    }

    fun classify(text: String, threshold: Float = DetectionSensitivity.DEFAULT_THRESHOLD): MlClassification {
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
                    return decide(probs, threshold)
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
