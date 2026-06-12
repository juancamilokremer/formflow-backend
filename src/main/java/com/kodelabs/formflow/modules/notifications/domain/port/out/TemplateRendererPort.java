package com.kodelabs.formflow.modules.notifications.domain.port.out;

import java.util.Map;

/**
 * Output port for HTML template rendering. Current adapter: Thymeleaf.
 */
public interface TemplateRendererPort {

    String render(String templateName, Map<String, Object> variables);
}
