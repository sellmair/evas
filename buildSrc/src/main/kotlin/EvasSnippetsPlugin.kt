import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import snippets.findSnippets

class EvasSnippetsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val checkSnippetsTask = project.tasks.register("checkSnippets", EvasCheckSnippetsTask::class.java)
        val checkTask = project.tasks.named("check")
        checkTask.dependsOn(checkSnippetsTask)

        project.tasks.register("updateSnippets", EvasUpdateSnippetsTask::class.java)
    }
}

private fun Project.sources(): FileCollection = project.files({
    project.kotlinExtension.sourceSets.flatMap { sourceSet ->
        sourceSet.kotlin.asFileTree
    }
})

private fun Project.docs(): FileCollection = project.fileTree(project.rootDir) {
    include("**/*.md")
    exclude("**/build/**")
    exclude("**/.*/**")
}


open class EvasCheckSnippetsTask : DefaultTask() {

    private val root = project.rootDir

    @InputFiles
    protected val sources = project.sources()

    @InputFiles
    protected val docs = project.docs()

    @TaskAction
    fun check() {
        val reports = mutableListOf<String>()
        findSnippets(root, docs, sources).forEach { (docSnippet, sourceSnippet) ->
            val docUrl = "file://${docSnippet.snippet.file.path}:${docSnippet.snippet.startLineIndex}"
            logger.quiet("Checking snippet $docUrl (${docSnippet.expectedSourceFile.name})")
            if (sourceSnippet == null) {
                reports += buildString {
                    append("e: $docUrl missing source '${docSnippet.expectedSourceFile}'")
                }
                return@forEach
            }

            if (docSnippet.snippet.text != sourceSnippet.text) {
                reports += buildString {
                    appendLine("e: $docUrl snippet does not match source")
                    appendLine("expected:")
                    appendLine(sourceSnippet.text.prependIndent("    |"))
                    appendLine()
                    appendLine("actual:")
                    appendLine(docSnippet.snippet.text.prependIndent("    |"))
                }
            }
        }

        if (reports.isNotEmpty()) {
            error(reports.joinToString("\n"))
        }
    }
}


open class EvasUpdateSnippetsTask : DefaultTask() {
    private val root = project.rootDir

    @InputFiles
    protected val sources = project.sources()

    @InputFiles
    protected val docs = project.docs()

    @TaskAction
    fun update() {
        val offsets = mutableMapOf</* Line Index */ Int, /*Offset */ Int>()

        fun resolveLineIndex(originalLineIndex: Int): Int {
            return originalLineIndex + offsets.filterKeys { lineIndex -> lineIndex <= originalLineIndex }
                .values.sum()
        }

        findSnippets(root, docs, sources).forEach { (docSnippet, sourceSnippet) ->
            if (sourceSnippet == null) return@forEach
            val originalDocText = docSnippet.snippet.file.readText()
            val originalDocLines = originalDocText.lines()

            val resolvedStartLineIndex = resolveLineIndex(docSnippet.snippet.startLineIndex)
            val resolvedEndLineIndex = resolveLineIndex(docSnippet.snippet.endLineIndex)

            val linesBeforeSnippet = originalDocLines.subList(0, resolvedStartLineIndex)
            val linesAfterSnippet = originalDocLines.subList(resolvedEndLineIndex, originalDocLines.size)
            val lines = linesBeforeSnippet + sourceSnippet.lines + linesAfterSnippet

            val newOffset = offsets.getOrDefault(docSnippet.snippet.endLineIndex, 0) +
                    (sourceSnippet.lines.size - docSnippet.snippet.lines.size)

            offsets[docSnippet.snippet.endLineIndex] = newOffset

            val updatedDocText = lines.joinToString(System.lineSeparator())
            docSnippet.snippet.file.writeText(updatedDocText)
        }
    }
}