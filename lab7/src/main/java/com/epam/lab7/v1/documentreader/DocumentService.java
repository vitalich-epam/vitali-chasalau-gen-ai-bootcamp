package com.epam.lab7.v1.documentreader;

import java.io.File;

public interface DocumentService {

    String searchDocumentsContent(String message);

    void addDocument(File file);
}
