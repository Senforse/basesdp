package org.nmgyj.codegen.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FrontendGenerateResult {

    private final Map<String, String> files;
    private final List<String> generatedFiles;

    public FrontendGenerateResult(Map<String, String> files) {
        this.files = new LinkedHashMap<>(files);
        this.generatedFiles = List.copyOf(files.keySet());
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public List<String> getGeneratedFiles() {
        return generatedFiles;
    }
}
