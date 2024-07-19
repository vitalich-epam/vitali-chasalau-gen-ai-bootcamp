package com.epam.lab7.v1.documentreader;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final VectorStore vectorStore;

    @Override
    public String searchDocumentsContent(String message) {
        List<Document> result = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(3));
        return result.stream().map(Document::getContent).collect(Collectors.joining("\n"));
    }

    @Override
    public void addDocument(File file) {
        vectorStore.add(readAndSplitPdf(file.getAbsolutePath()));
    }

    private List<Document> readAndSplitPdf(String filePath) {
        PdfDocumentReaderConfig readerConfig = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(3)
                .build();
        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, readerConfig);
            return pagePdfDocumentReader.get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF", e);
        }
    }
}



