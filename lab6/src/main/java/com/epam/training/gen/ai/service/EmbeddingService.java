package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.VectorSearchResult;
import com.epam.training.gen.ai.entity.ContentEntity;
import com.epam.training.gen.ai.entity.FileEntity;
import com.epam.training.gen.ai.repository.ContentRepository;
import com.epam.training.gen.ai.repository.FileRepository;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private static final String INDEX = "index";
    private static final String FILE_NAME = "fileName";

    private final VectorStore vectorStore;
    private final FileRepository fileRepository;
    private final ContentRepository contentRepository;

    @Value("${embedding.segment.chunk-size}")
    @Setter
    private Integer maxSegmentChunkSize;

    @Value("${embedding.search.threshold}")
    @Setter
    private Double searchThreshold;

    @Value("${embedding.search.topk}")
    @Setter
    private Integer topk;

    @Value("${embedding.search.segment.added-overlap}")
    @Setter
    private Integer searchAddedOverlap;

    @Transactional
    public void addDocument(File file, String fileName) {

        cleanExistingFileIfExists(fileName);

        List<TextSegment> segments = splitPdfToSegments(file);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(fileName);

        var fileContents = segments.stream()
                .map(segment ->
                        new ContentEntity(UUID.randomUUID().toString(),
                                segment.text(),
                                segment.metadata().getInteger(INDEX),
                                fileEntity))
                .toList();

        fileEntity.setItems(fileContents);

        var documents = fileContents.stream()
                .map(item -> new Document(item.getId(), item.getContent(),
                        Map.of(FILE_NAME, fileName, INDEX, item.getIndex())))
                .toList();

        fileRepository.save(fileEntity);
        vectorStore.add(documents);
    }

    public VectorSearchResult searchClosestDocuments(String searchText) {
        SearchRequest searchRequest = SearchRequest.query(searchText)
                .withSimilarityThreshold(searchThreshold)
                .withTopK(topk);
        var documents = vectorStore.similaritySearch(searchRequest);
        var records = documents.stream()
                .collect(Collectors.groupingBy(document -> (String) document.getMetadata().get(FILE_NAME), Collectors.toList()))
                .entrySet().stream()
                .flatMap(entry -> {
                    var fileName = entry.getKey();
                    var indexes = entry.getValue().stream()
                            .map(document -> (Integer) document.getMetadata().get(INDEX))
                            .flatMap(index -> IntStream.rangeClosed(Math.max(0, index - searchAddedOverlap), index + searchAddedOverlap).boxed())
                            .collect(Collectors.toSet());
                    var contents = contentRepository.findByFileNameAndIndexInOrderByIndex(fileName, indexes).stream()
                            .toList();
                    List<VectorSearchResult.Record> combinedRecords = new ArrayList<>();
                    VectorSearchResult.Record lastRecord = null;

                    for (ContentEntity content : contents) {
                        if (lastRecord == null || content.getIndex() - lastRecord.getIndexTo() > 1) {
                            //create a new record for a separate blocks
                            lastRecord = new VectorSearchResult.Record(fileName, content.getIndex(),
                                    content.getIndex(), content.getContent());
                            combinedRecords.add(lastRecord);
                        } else {
                            //add sequential block to last record
                            lastRecord.setIndexTo(content.getIndex());
                            lastRecord.setContent(lastRecord.getContent() + content.getContent());
                        }
                    }
                    return combinedRecords.stream();
                })
                .toList();
        return new VectorSearchResult(records);

    }

    public List<Document> searchDocuments(String searchText, double threshold, int limit) {
        SearchRequest searchRequest = SearchRequest.query(searchText)
                .withSimilarityThreshold(threshold)
                .withTopK(limit);
        return vectorStore.similaritySearch(searchRequest);
    }

    private List<TextSegment> splitPdfToSegments(File pdfFile) {

        dev.langchain4j.data.document.Document document = FileSystemDocumentLoader.loadDocument(pdfFile.getAbsolutePath(), new ApachePdfBoxDocumentParser());

        return DocumentSplitters.recursive(maxSegmentChunkSize, 0).split(document);
    }

    private void cleanExistingFileIfExists(String fileName) {
        Optional<FileEntity> fileOptional = fileRepository.findByName(fileName);
        if (fileOptional.isPresent()) {
            FileEntity file = fileOptional.get();
            var vectorIdList = file.getItems().stream().map(ContentEntity::getId).toList();
            fileRepository.delete(file);
            vectorStore.delete(vectorIdList);
        }
    }
}
