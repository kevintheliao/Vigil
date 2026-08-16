package com.example.vigil.detection

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class WordPieceTokenizerTest {

    private val vocab = mapOf(
        "[CLS]" to 0L,
        "[SEP]" to 1L,
        "[UNK]" to 2L,
        "hello" to 3L,
        "world" to 4L,
        "test" to 5L,
        "##s" to 6L,
    )

    @Test
    fun knownWords_mapToVocabIds() {
        val (ids, mask) = WordPieceTokenizer(vocab).tokenize("hello world")
        assertArrayEquals(longArrayOf(0L, 3L, 4L, 1L), ids)
        assertArrayEquals(longArrayOf(1L, 1L, 1L, 1L), mask)
    }

    @Test
    fun wordSplitsIntoSubwordPieces() {
        // "tests" isn't in vocab whole, but "test" + "##s" covers it
        val (ids, _) = WordPieceTokenizer(vocab).tokenize("tests")
        assertArrayEquals(longArrayOf(0L, 5L, 6L, 1L), ids)
    }

    @Test
    fun unmatchedWord_becomesUnk() {
        val (ids, _) = WordPieceTokenizer(vocab).tokenize("xyz")
        assertArrayEquals(longArrayOf(0L, 2L, 1L), ids)
    }

    @Test
    fun longInput_isTruncatedToMaxSequenceLength() {
        val (ids, mask) = WordPieceTokenizer(vocab, maxSequenceLength = 4).tokenize("hello world hello world")
        // 4 - 2 special tokens = 2 word pieces kept, plus [CLS]/[SEP]
        assertArrayEquals(longArrayOf(0L, 3L, 4L, 1L), ids)
        assertArrayEquals(longArrayOf(1L, 1L, 1L, 1L), mask)
    }
}
