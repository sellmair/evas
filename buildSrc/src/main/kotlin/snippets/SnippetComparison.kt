package snippets

import java.io.File

private val sampleRegex = Regex("""\[snippet]: \((.*)\)""")

internal fun findSnippets(
    root: File,
    docs: Iterable<File>,
    sources: Iterable<File>,
): Map<DocSnippet, Snippet?> = buildMap {
    docs.forEach { doc ->
        putAll(findSnippets(root, doc, sources))
    }
}

internal fun findSnippets(
    root: File,
    doc: File,
    sources: Iterable<File>
): Map<DocSnippet, Snippet?> {
    val result = mutableMapOf<DocSnippet, Snippet?>()

    val text = doc.readText()
    val docLines = text.lines()
    val endIndices = docLines.mapIndexedNotNull { index, line ->
        if (line.trim().startsWith("```")) index else null
    }

    text.lines().forEachIndexed { docLineIndex, docLine ->
        val sampleMatch = sampleRegex.find(docLine) ?: return@forEachIndexed
        val docNextLine = docLines.getOrNull(docLineIndex + 1) ?: return@forEachIndexed
        if (!docNextLine.trim().startsWith("```kotlin"))
            error("e: file://$doc:${docLineIndex +1} Missing ```kotlin after [snippet]")

        val docEndLineIndex = endIndices.firstOrNull { endIndex -> endIndex > docLineIndex + 1 }
            ?: return@forEachIndexed

        val sourceFilePath = sampleMatch.groupValues[1]
        val sourceFile = root.resolve("snippets/src").resolve(sourceFilePath)

        val docSnippet = DocSnippet(
            Snippet(
                file = doc,
                startLineIndex = docLineIndex + 2,
                endLineIndex = docEndLineIndex,
                lines = docLines.subList(docLineIndex + 2, docEndLineIndex),
                text = docLines.subList(docLineIndex + 2, docEndLineIndex).joinToString(System.lineSeparator()),
            ), sourceFile
        )

        val sourceSnippetFile = sources.find { it == sourceFile }
        if (sourceSnippetFile == null) {
            result[docSnippet] = null
            return@forEachIndexed
        }

        val sourceLines = sourceSnippetFile.readText().lines()
        val sourceSnippetStartIndex = sourceLines.indexOfFirst { it.trim() == "//Start" }
        val sourceSnippetIndent = sourceLines[sourceSnippetStartIndex].takeWhile { it.isWhitespace() }
        val sourceSnippetEndIndex = sourceLines.indexOfFirst { it.trim() == "//End" }
        val sourceSnippetLines = sourceLines.subList(sourceSnippetStartIndex + 1, sourceSnippetEndIndex)
            .joinToString(System.lineSeparator()) { it.removePrefix(sourceSnippetIndent) }

        result[docSnippet] = Snippet(
            file = sourceSnippetFile,
            startLineIndex = sourceSnippetStartIndex + 1,
            endLineIndex = sourceSnippetEndIndex,
            lines = sourceSnippetLines.lines(),
            text = sourceSnippetLines
        )
    }

    return result
}

data class DocSnippet(
    val snippet: Snippet,
    val expectedSourceFile: File
)

data class Snippet(
    val file: File,
    val startLineIndex: Int,
    val endLineIndex: Int,
    val lines: List<String>,
    val text: String
)
