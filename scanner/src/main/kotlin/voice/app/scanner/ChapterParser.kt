package voice.app.scanner

import androidx.documentfile.provider.DocumentFile
import voice.data.Chapter
import voice.data.ChapterId
import voice.data.repo.ChapterRepo
import voice.data.supportedAudioFormats
import java.time.Instant
import javax.inject.Inject

class ChapterParser
@Inject constructor(
  private val chapterRepo: ChapterRepo,
  private val mediaAnalyzer: MediaAnalyzer,
) {

  suspend fun parse(documentFile: DocumentFile): List<Chapter> {
    val result = mutableListOf<Chapter>()

    suspend fun parseChapters(file: DocumentFile) {
      if (file.isAudioFile()) {
        val id = ChapterId(file.uri)
        val chapter = chapterRepo.getOrPut(id, Instant.ofEpochMilli(file.lastModified())) {
          val metaData = mediaAnalyzer.analyze(file) ?: return@getOrPut null
          Chapter(
            id = id,
            duration = metaData.duration,
            fileLastModified = Instant.ofEpochMilli(file.lastModified()),
            name = metaData.chapterName,
            markData = metaData.chapters,
          )
        }
        if (chapter != null) {
          result.add(chapter)
        }
      } else if (file.isDirectory) {
        file.listFiles()
          .forEach {
            parseChapters(it)
          }
      }
    }

    parseChapters(file = documentFile)
    return result.sorted()
  }
}

private fun DocumentFile.isAudioFile(): Boolean {
  if (!isFile) return false
  val name = name ?: return false
  val extension = name.substringAfterLast(".")
  return extension.lowercase() in supportedAudioFormats
}
