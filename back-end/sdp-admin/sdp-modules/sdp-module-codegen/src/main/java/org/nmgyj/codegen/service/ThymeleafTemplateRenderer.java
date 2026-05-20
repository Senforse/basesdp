package org.nmgyj.codegen.service;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Locale;
import java.util.Map;

@Component
public class ThymeleafTemplateRenderer implements TemplateRenderer {

    private final SpringTemplateEngine templateEngine;

    public ThymeleafTemplateRenderer() {
        this.templateEngine = new SpringTemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver());
    }

    @Override
    public String render(String templatePath, Map<String, Object> model) {
        Context context = new Context(Locale.SIMPLIFIED_CHINESE);
        context.setVariables(model);
        return templateEngine.process(templatePath, context);
    }

    private ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("codegen/templates/");
        resolver.setSuffix(".th.txt");
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }
}
