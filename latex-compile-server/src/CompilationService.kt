package org.sjoblomj

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import java.io.File
import java.util.*

const val zipFileName = "zip.zip"
const val numberOfCompilations = 3


fun Route.routing() {
	route("/compile") {

		post {
			val mainDir = "uploads/" + UUID.randomUUID().toString()
			File(mainDir).mkdirs()

			val mainFile = saveAttachment(mainDir)
			unzip(mainDir)
			compilePdf(mainDir, mainFile)
			respondWithFile(mainDir, mainFile)
		}
	}
}

private suspend fun PipelineContext<Unit, ApplicationCall>.saveAttachment(mainDir: String): String {
	var mainFile = "*"
	call.receiveMultipart().forEachPart { part ->
		if (part is PartData.FileItem)
			saveToFile(part, mainDir)
		else if (part is PartData.FormItem && part.name == "mainFile")
			mainFile = part.value
		part.dispose() // Make sure to dispose of the part after use to prevent leaks
	}
	return mainFile
}

private fun saveToFile(part: PartData.FileItem, mainDir: String) {
	val file = File("$mainDir/$zipFileName")

	part.streamProvider().use { its ->
		file.outputStream().buffered().use {
			its.copyTo(it)
		}
	}
}

private fun unzip(dir: String) {
	try {
		ProcessBuilder()
			.command("unzip", "$dir/$zipFileName", "-d", dir)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.start()
			.waitFor()
	} catch (e: Exception) {
		println(e)
	}
}

fun compilePdf(dir: String, mainFile: String) {
	try {
		(0..numberOfCompilations).forEach { _ ->
			ProcessBuilder()
				.directory(File(dir))
				.command("xelatex", "$mainFile.tex")
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.redirectOutput(ProcessBuilder.Redirect.INHERIT)
				.start()
				.waitFor()
		}
	} catch (e: Exception) {
		println(e)
	}
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondWithFile(mainDir: String, mainFile: String) {
	val file = File("$mainDir/$mainFile.pdf")
	if (file.exists())
		call.respondFile(file)
	else
		call.respond(HttpStatusCode.NotFound)
}

fun Application.registerRoutes() {
	routing {
		routing()
	}
}
