package org.nmgyj.codegen.service;

import java.util.Map;

public interface TemplateRenderer {

    String render(String templatePath, Map<String, Object> model);
}
