package com.formas.cms.importer;

public record ImportSummary(int processed, int createdOrUpdated, int skipped, String message) {
}
