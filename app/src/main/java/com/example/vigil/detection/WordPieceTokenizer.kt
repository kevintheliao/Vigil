package com.example.vigil.detection

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/** BERT-style WordPiece tokenizer. */
class WordPieceTokenizer internal constructor(
    private val vocab: Map<String, Long>,
    private val maxSequenceLength: Int = 128,
) {
    constructor(context: Context, maxSequenceLength: Int = 128) : this(loadVocab(context), maxSequenceLength)

    private val clsId: Long = vocab.getValue("[CLS]")
    private val sepId: Long = vocab.getValue("[SEP]")
    private val unkId: Long = vocab.getValue(unkToken)

    companion object {
        private const val unkToken = "[UNK]"

        private fun loadVocab(context: Context): Map<String, Long> {
            val map = HashMap<String, Long>()
            context.assets.open("vocab.txt").use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).forEachLine { line ->
                    if (line.isNotEmpty()) map[line] = map.size.toLong()
                }
            }
            return map
        }
    }

    fun tokenize(text: String): Pair<LongArray, LongArray> {
        val wordPieces = basicTokenize(collapseRepeatedChars(text)).flatMap { wordPieceTokenize(it) }
        val maxPieces = maxSequenceLength - 2 
        val truncated = wordPieces.take(maxPieces)

        val ids = LongArray(truncated.size + 2)
        ids[0] = clsId
        truncated.forEachIndexed { i, piece -> ids[i + 1] = vocab[piece] ?: unkId }
        ids[ids.size - 1] = sepId

        val attentionMask = LongArray(ids.size) { 1L }
        return ids to attentionMask
    }

    private fun basicTokenize(text: String): List<String> {
        val lower = text.lowercase()
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        for (ch in lower) {
            when {
                ch.isWhitespace() -> {
                    if (current.isNotEmpty()) { tokens.add(current.toString()); current.clear() }
                }
                isPunctuation(ch) -> {
                    if (current.isNotEmpty()) { tokens.add(current.toString()); current.clear() }
                    tokens.add(ch.toString())
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) tokens.add(current.toString())
        return tokens
    }

    private fun isPunctuation(ch: Char): Boolean = !ch.isLetterOrDigit() && !ch.isWhitespace()

    private fun wordPieceTokenize(word: String): List<String> {
        if (word.length > 100) return listOf(unkToken)

        val pieces = mutableListOf<String>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var matched: String? = null
            while (start < end) {
                val candidate = if (start > 0) "##${word.substring(start, end)}" else word.substring(start, end)
                if (vocab.containsKey(candidate)) {
                    matched = candidate
                    break
                }
                end--
            }
            if (matched == null) return listOf(unkToken)
            pieces.add(matched)
            start = end
        }
        return pieces
    }
}
