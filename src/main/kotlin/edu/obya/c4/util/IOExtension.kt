package edu.obya.c4.util

import org.slf4j.Logger
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

fun String.toResourceStream(logger: Logger = "edu.obya.c4.util.toResourceStream".logger()): InputStream? =
    runCatching { ClassLoader.getSystemResource(this).openStream() }
        .onFailure { logger.warn("Unable to load resource file $this.") }
        .onSuccess { logger.info("Resource file $this has been successfully loaded.") }
        .getOrNull()

fun Path.toFileStream(logger: Logger = "edu.obya.c4.util.toFileStream".logger()): InputStream? =
    runCatching { this.toFile().inputStream() }
        .onFailure { logger.warn("Unable to load file $this.") }
        .onSuccess { logger.info("File $this has been successfully loaded.") }
        .getOrNull()

fun Set<Path>.toFilePaths(pattern: String) = this.flatMap { path ->
    if (Files.exists(path)) {
        if (Files.isDirectory(path)) {
            Files.newDirectoryStream(path, pattern).iterator().asSequence().toList()
        } else {
            listOf(path)
        }
    } else emptyList()
}

