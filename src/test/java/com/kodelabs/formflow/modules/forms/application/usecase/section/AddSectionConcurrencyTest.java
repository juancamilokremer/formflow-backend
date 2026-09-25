package com.kodelabs.formflow.modules.forms.application.usecase.section;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards backend#162. {@code AddSectionService} used to compute the new section's position
 * from a plain {@code count()} read before inserting, with no locking — under real concurrent
 * writes to the same form, two requests could read the same count and insert at the same
 * position. Deliberately NOT {@code @Transactional}: each concurrent call must run in its own
 * transaction/connection to actually race, same as production traffic. Cleans up its own rows
 * at the end since there is no rollback to rely on.
 */
@SpringBootTest
@ActiveProfiles("test")
class AddSectionConcurrencyTest {

    @Autowired private AddSectionUseCase addSection;
    @Autowired private FormRepositoryPort formRepository;
    @Autowired private FormSectionRepositoryPort sectionRepository;

    @Test
    void concurrentAddSectionRequestsNeverProduceDuplicatePositions() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Form form = formRepository.save(Form.builder()
                .tenantId(tenantId).name("Concurrencia").type(FormType.CANDIDATES).build());

        int n = 15;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<Callable<Object>> tasks = IntStream.range(0, n)
                    .<Callable<Object>>mapToObj(i -> () ->
                            addSection.execute(new AddSectionCommand(
                                    form.getId(), tenantId, userId, "Seccion " + i, null, null)))
                    .toList();

            List<Future<Object>> futures = pool.invokeAll(tasks);
            for (Future<Object> f : futures) {
                f.get(); // propagate any exception, none expected
            }
        } finally {
            pool.shutdown();
        }

        var sections = sectionRepository.findActiveByFormIdAndTenantId(form.getId(), tenantId);
        assertThat(sections).hasSize(n);

        List<Integer> positions = sections.stream().map(s -> s.getPosition()).toList();
        List<Integer> distinctPositions = positions.stream().distinct().toList();
        assertThat(distinctPositions)
                .as("positions must be unique, got %s", positions)
                .hasSize(n);

        Form reloaded = formRepository.findByIdAndTenantId(form.getId(), tenantId).orElseThrow();
        assertThat(reloaded.getVersion())
                .as("every successful addSection must increment the form version exactly once")
                .isEqualTo(1 + n);
    }
}
