package com.regulafin.assistant.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    @Value("classpath:data/res-4658-2018.pdf")
    private Resource regulationPdf;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Add o PDF no startup, se o vector estiver vazio (idempotencia)
     */
    @PostConstruct
    public void ingestOnStartup() {
        if (isVectorStoreEmpty()) {
            log.info("Vector store vazio. Iniciando ingestão da Res. 4.658/2018...");
            ingest();
        } else {
            log.info("Vector store já tem documentos. Pulando ingestão.");
        }
    }

    public void ingest() {
        log.info("Lendo PDF: {}", regulationPdf.getFilename());

        var pdfReader = new PagePdfDocumentReader(
                regulationPdf,
                PdfDocumentReaderConfig.builder()
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> rawDocs = pdfReader.get();
        log.info("PDF lido. {} páginas extraídas.", rawDocs.size());

        var splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);
        log.info("Splitting concluído. {} chunks gerados.", chunks.size());

        log.info("Gerando embeddings e indexando no pgvector. Isso pode levar 30-60s...");
        vectorStore.add(chunks);
        log.info("Ingestão concluída. {} chunks indexados.", chunks.size());
    }

    private boolean isVectorStoreEmpty() {
        var probe = vectorStore.similaritySearch(
                SearchRequest.builder().query("teste").topK(1).build()
        );
        return probe == null || probe.isEmpty();
    }
}
