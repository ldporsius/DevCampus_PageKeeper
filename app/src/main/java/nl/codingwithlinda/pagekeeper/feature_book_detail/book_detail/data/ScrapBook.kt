package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

/* private suspend fun buildPages(sections: List<String>, imageMap: Map<String, ByteArray>, book: Book): List<Page> = buildList {
       val textBuffer = mutableListOf<FormattedLine>()

       fun flushText() {
           if (textBuffer.isNotEmpty()) {
               add(Page.TextPage(textBuffer.toList()))
               textBuffer.clear()
           }
       }

       sections.forEach { section ->
           currentCoroutineContext().ensureActive()
           pRegex.findAll(section).forEach { match ->
               val content = match.groupValues[1]
               val imageHref = imageRegex.find(content)?.groupValues?.get(1)
               if (imageHref != null) {
                   flushText()
                   val fileUri = saveImageToStorage(imageHref, imageMap, book)
                   add(Page.ImagePage(fileUri ?: imageHref))
               } else {
                   textBuffer += parseSpans(content)
                   if (textBuffer.size >= 100) flushText()
               }
           }
       }
       flushText()
   }*/