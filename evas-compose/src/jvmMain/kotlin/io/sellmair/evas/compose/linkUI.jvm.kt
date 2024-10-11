@file:Suppress("LoggingStringTemplateAsArgument")

package io.sellmair.evas.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.net.URLClassLoader
import kotlin.io.path.Path

private val logger = createLogger()

private val coldUIClasspath
    get() = System.getProperty("evas.ui.class.path.cold")?.split(File.pathSeparator)?.map(::Path).orEmpty()

private val hotUIClasspath
    get() = System.getProperty("evas.ui.class.path.hot")?.split(File.pathSeparator)?.map(::Path).orEmpty()

/* Magic value 🧙, pick any and remember */
private const val linkedUIGroupId = 1902

public class EvasHotReloadState(
    public val iteration: Int,
    internal val uiClass: Class<*>,
)

@Composable
public fun linkUI(className: String, funName: String = "Main") {
    logger.debug("Linking $className/$funName")

    if (hotUIClasspath.orEmpty().isNotEmpty()) {
        return linkHotReloadableUI(className, funName)
    }

    val uiClass = Class.forName(className)
    invokeUI(uiClass, funName)
}

private val hotReloadState = MutableStateFlow<EvasHotReloadState?>(null)

@Composable
private fun linkHotReloadableUI(
    className: String, funName: String
) {
    val coldUIClasspath = coldUIClasspath
    val hotUIClasspath = hotUIClasspath

    LaunchedEffect(coldUIClasspath) {
        coldUIClasspath.forEach { path ->
            logger.debug("cold: $path")
        }
    }

    LaunchedEffect(hotUIClasspath) {
        hotUIClasspath.forEach { path ->
            logger.debug("hot: $path")
        }
    }

    val baseUIClassLoader = URLClassLoader(
        coldUIClasspath.map { it.toUri().toURL() }.toTypedArray(),
        Thread.currentThread().contextClassLoader
    )

    fun reload() = runCatching {
        val loader = URLClassLoader(hotUIClasspath.map { it.toUri().toURL() }.toTypedArray(), baseUIClassLoader)
        val clazz = Class.forName(className, true, loader)
        hotReloadState.update { current ->
            EvasHotReloadState(iteration = current?.iteration?.inc() ?: 0, uiClass = clazz)
        }

        logger.info("Reloaded ($className/$funName)")
    }.onFailure { exception ->
        logger.error("Failed to reload ($className/$funName)", exception)
    }

    LaunchedEffect(className, funName, hotUIClasspath) {
        withContext(Dispatchers.IO) {
            reload()
            watchClasspath(hotUIClasspath).collect { change ->
                logger.debug("hot ui classpath changed")
                change.context.forEach { path ->
                    logger.debug("changed: $path")
                }
                reload()
            }
        }
    }

    LaunchedEffect(className, funName, hotUIClasspath) {
        withContext(Dispatchers.IO) {
            launchRecompiler()
        }
    }

    val reloadState = hotReloadState.collectAsState().value ?: run {
        logger.debug("Waiting for linkage of($className/$funName)")
        return
    }

    key(reloadState.iteration, className, funName, coldUIClasspath, hotUIClasspath) {
        logger.debug("Showing ($className/$funName) (iteration: ${reloadState.iteration})")
        invokeUI(reloadState.uiClass, funName)
    }
}

@Composable
private fun invokeUI(uiClass: Class<*>, funName: String) {
    val uiMethodHandle = MethodHandles.lookup().findStatic(
        uiClass, funName, methodType(Void.TYPE, Composer::class.java, Int::class.javaPrimitiveType)
    )

    invokeUI(uiMethodHandle)
}

@Composable
private fun invokeUI(ui: MethodHandle) {
    currentComposer.startRestartGroup(linkedUIGroupId)
    ui.invokeWithArguments(currentComposer, 0 /* 0 means not changed!*/)

    currentComposer.endRestartGroup()?.updateScope { composer, i ->
        ui.invokeWithArguments(composer, i)
    }
}
