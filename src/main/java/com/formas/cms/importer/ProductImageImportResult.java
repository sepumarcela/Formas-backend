package com.formas.cms.importer;

import java.util.List;

public record ProductImageImportResult(
    int uploaded,
    int matched,
    int skipped,
    List<String> updatedProductIds,
    List<String> unmatchedFiles) {
}
