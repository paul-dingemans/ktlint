package com.pinterest.ktlint.internal

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.system.exitProcess

internal val workDir: String = File(".").canonicalPath
private val tildeRegex = Regex("^(!)?~")

internal fun FileSystem.fileSequence(
    globs: List<String>,
    rootDir: Path = Paths.get(".").toAbsolutePath().normalize()
): Sequence<Path> {
    checkGlobsContainAbsolutePath(globs)

    val pathMatchers = if (globs.isEmpty()) {
        setOf(
            getPathMatcher("glob:**$globSeparator*.kt"),
            getPathMatcher("glob:**$globSeparator*.kts")
        )
    } else {
        globs
            .filterNot { it.startsWith("!") }
            .map {
                getPathMatcher(it.toGlob(rootDir))
            }
    }

    val negatedPathMatchers = if (globs.isEmpty()) {
        emptySet()
    } else {
        globs
            .filter { it.startsWith("!") }
            .map { getPathMatcher(it.removePrefix("!").toGlob(rootDir)) }
    }

    val result = mutableListOf<Path>()
    Files.walkFileTree(
        rootDir,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(
                filePath: Path,
                fileAttrs: BasicFileAttributes
            ): FileVisitResult {
                if (negatedPathMatchers.none { it.matches(filePath) } &&
                    pathMatchers.any { it.matches(filePath) }
                ) {
                    result.add(filePath)
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(
                dirPath: Path,
                dirAttr: BasicFileAttributes
            ): FileVisitResult {
                return if (Files.isHidden(dirPath)) {
                    FileVisitResult.SKIP_SUBTREE
                } else {
                    FileVisitResult.CONTINUE
                }
            }
        }
    )

    return result.asSequence()
}

private fun FileSystem.checkGlobsContainAbsolutePath(globs: List<String>) {
    val rootDirs = rootDirectories.map { it.toString() }
    globs
        .map { it.removePrefix("!") }
        .filter { glob ->
            rootDirs.any { glob.startsWith(it) }
        }
        .run {
            if (isNotEmpty()) {
                throw IllegalArgumentException(
                    "KtLint does not support absolute path in globs:\n${joinToString(separator = "\n")}"
                )
            }
        }
}

private fun String.toGlob(
    rootDir: Path
): String {
    val expandedPath = expandTilde(this)
    val rootDirPath = rootDir
        .toAbsolutePath()
        .toString()
        .run {
            val normalizedPath = if (!endsWith(File.separator)) "$this${File.separator}" else this
            normalizedPath.replace(File.separator, globSeparator)
        }
    return "glob:$rootDirPath$expandedPath"
}

internal val globSeparator: String get() {
    val os = System.getProperty("os.name")
    return when {
        os.startsWith("windows", ignoreCase = true) -> "\\\\"
        else -> "/"
    }
}

/**
 * List of paths to Java `jar` files.
 */
internal typealias JarFiles = List<String>

internal fun JarFiles.toFilesURIList() = map {
    val jarFile = File(expandTilde(it))
    if (!jarFile.exists()) {
        println("Error: $it does not exist")
        exitProcess(1)
    }
    jarFile.toURI().toURL()
}

// a complete solution would be to implement https://www.gnu.org/software/bash/manual/html_node/Tilde-Expansion.html
// this implementation takes care only of the most commonly used case (~/)
private fun expandTilde(path: String): String = path.replaceFirst(tildeRegex, System.getProperty("user.home"))

internal fun File.location(
    relative: Boolean
) = if (relative) this.toRelativeString(File(workDir)) else this.path

/**
 * Run lint over common kotlin file or kotlin script file.
 */
internal fun lintFile(
    fileName: String,
    fileContents: String,
    ruleSets: List<RuleSet>,
    userData: Map<String, String> = emptyMap(),
    editorConfigPath: String? = null,
    debug: Boolean = false,
    lintErrorCallback: (LintError) -> Unit = {}
) {
    KtLint.lint(
        KtLint.Params(
            fileName = fileName,
            text = fileContents,
            ruleSets = ruleSets,
            userData = userData,
            script = !fileName.endsWith(".kt", ignoreCase = true),
            editorConfigPath = editorConfigPath,
            cb = { e, _ ->
                lintErrorCallback(e)
            },
            debug = debug,
            isInvokedFromCli = true
        )
    )
}

/**
 * Format a kotlin file or script file
 */
internal fun formatFile(
    fileName: String,
    fileContents: String,
    ruleSets: Iterable<RuleSet>,
    userData: Map<String, String>,
    editorConfigPath: String?,
    debug: Boolean,
    cb: (e: LintError, corrected: Boolean) -> Unit
): String =
    KtLint.format(
        KtLint.Params(
            fileName = fileName,
            text = fileContents,
            ruleSets = ruleSets,
            userData = userData,
            script = !fileName.endsWith(".kt", ignoreCase = true),
            editorConfigPath = editorConfigPath,
            cb = cb,
            debug = debug,
            isInvokedFromCli = true
        )
    )
