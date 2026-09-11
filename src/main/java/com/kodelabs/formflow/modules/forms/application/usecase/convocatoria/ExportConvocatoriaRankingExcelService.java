package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.application.service.ResponseDetailAssembler;
import com.kodelabs.formflow.modules.forms.domain.port.in.ExportConvocatoriaRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportConvocatoriaRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.AnswerDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ExportConvocatoriaRankingResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingFormScoreResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.export.ExcelRowWriter;
import com.kodelabs.formflow.shared.export.ExcelSheet;
import com.kodelabs.formflow.shared.export.ExcelSheetNames;
import com.kodelabs.formflow.shared.export.ExportFilenames;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportConvocatoriaRankingExcelService implements ExportConvocatoriaRankingUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final GetRankingUseCase getRanking;
    private final FormResponseRepositoryPort responseRepository;
    private final ResponseDetailAssembler responseDetailAssembler;
    private final Messages messages;
    private final ExcelRowWriter excelRowWriter;

    @Override
    @Transactional(readOnly = true)
    public ExportConvocatoriaRankingResult execute(ExportConvocatoriaRankingQuery query) {
        Convocatoria convocatoria = loadConvocatoria(query.convocatoriaId(), query.tenantId());
        List<RankingEntryResult> allEntries = getRanking.execute(
                new GetRankingQuery(query.convocatoriaId(), query.tenantId()));

        Set<String> categoryColumns = collectCategoryColumns(allEntries);
        Map<UUID, String> formColumns = collectFormColumns(allEntries);

        List<RankingEntryResult> selectedEntries = filterBySelection(allEntries, query.candidateIds());

        List<List<String>> resumenRows = new ArrayList<>();
        resumenRows.add(buildHeaderRow(categoryColumns, formColumns));
        for (RankingEntryResult entry : selectedEntries) {
            resumenRows.add(buildDataRow(entry, categoryColumns, formColumns));
        }

        String resumenSheetName = messages.get("export.excel.ranking_sheet_name");
        List<ExcelSheet> sheets = new ArrayList<>();
        sheets.add(new ExcelSheet(resumenSheetName, resumenRows));
        sheets.addAll(buildFormDetailSheets(query, selectedEntries, formColumns, resumenSheetName));

        byte[] content = excelRowWriter.write(sheets);
        return new ExportConvocatoriaRankingResult(content, ExportFilenames.build(convocatoria.getName(), "xlsx"));
    }

    private List<ExcelSheet> buildFormDetailSheets(
            ExportConvocatoriaRankingQuery query,
            List<RankingEntryResult> selectedEntries,
            Map<UUID, String> formColumns,
            String resumenSheetName) {
        Set<UUID> selectedCandidateIds = selectedEntries.stream()
                .map(RankingEntryResult::candidateId).collect(Collectors.toSet());

        List<FormResponse> allResponses = responseRepository.findAllByConvocatoriaIdAndTenantId(
                query.convocatoriaId(), query.tenantId(), null, null);
        Map<UUID, List<FormResponse>> responsesByFormId = allResponses.stream()
                .filter(response -> selectedCandidateIds.contains(response.getCandidateId()))
                .collect(Collectors.groupingBy(FormResponse::getFormId));

        Map<UUID, RankingEntryResult> entryByCandidateId = selectedEntries.stream()
                .collect(Collectors.toMap(RankingEntryResult::candidateId, entry -> entry));

        Set<String> usedSheetNames = new HashSet<>();
        usedSheetNames.add(resumenSheetName);

        List<ExcelSheet> sheets = new ArrayList<>();
        for (Map.Entry<UUID, String> form : formColumns.entrySet()) {
            List<FormResponse> formResponses = responsesByFormId.getOrDefault(form.getKey(), List.of());
            // Order rows by ranking position rather than however the bulk query returned them.
            List<FormResponse> orderedResponses = selectedEntries.stream()
                    .flatMap(entry -> formResponses.stream()
                            .filter(response -> response.getCandidateId().equals(entry.candidateId())))
                    .toList();

            String sheetName = ExcelSheetNames.uniqueName(form.getValue(), usedSheetNames);
            usedSheetNames.add(sheetName);
            sheets.add(new ExcelSheet(sheetName, buildFormDetailRows(orderedResponses, entryByCandidateId)));
        }
        return sheets;
    }

    /** Every candidate answers the same frozen form snapshot — taking the first response's questions is enough. */
    private List<List<String>> buildFormDetailRows(
            List<FormResponse> responses, Map<UUID, RankingEntryResult> entryByCandidateId) {
        List<List<String>> rows = new ArrayList<>();
        if (responses.isEmpty()) {
            rows.add(List.of(messages.get("export.ranking.header.name"), messages.get("export.ranking.header.email")));
            return rows;
        }

        List<AnswerDetailResult> questionColumns = responseDetailAssembler.buildOrderedAnswers(responses.get(0));

        List<String> header = new ArrayList<>(List.of(
                messages.get("export.ranking.header.name"), messages.get("export.ranking.header.email")));
        questionColumns.forEach(question -> header.add(question.questionTitle()));
        rows.add(header);

        for (FormResponse response : responses) {
            RankingEntryResult entry = entryByCandidateId.get(response.getCandidateId());
            Map<UUID, String> displayValuesByQuestionId = responseDetailAssembler.buildOrderedAnswers(response).stream()
                    .collect(Collectors.toMap(AnswerDetailResult::questionId,
                            answer -> answer.displayValue() != null ? answer.displayValue() : ""));

            List<String> row = new ArrayList<>(List.of(entry.name(), entry.email()));
            questionColumns.forEach(question ->
                    row.add(displayValuesByQuestionId.getOrDefault(question.questionId(), "")));
            rows.add(row);
        }
        return rows;
    }

    private Convocatoria loadConvocatoria(UUID convocatoriaId, UUID tenantId) {
        return convocatoriaRepository.findByIdAndTenantId(convocatoriaId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        "error.convocatoria.not_found", HttpStatus.NOT_FOUND, convocatoriaId));
    }

    private List<RankingEntryResult> filterBySelection(List<RankingEntryResult> entries, List<UUID> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) return entries;
        Set<UUID> selected = Set.copyOf(candidateIds);
        return entries.stream().filter(entry -> selected.contains(entry.candidateId())).toList();
    }

    /** Alphabetical — scoresByCategory is a plain Map, its own iteration order isn't guaranteed. */
    private Set<String> collectCategoryColumns(List<RankingEntryResult> entries) {
        Set<String> categoryNames = new TreeSet<>();
        for (RankingEntryResult entry : entries) {
            if (entry.scoresByCategory() != null) categoryNames.addAll(entry.scoresByCategory().keySet());
        }
        return categoryNames;
    }

    /** Every candidate shares the same convocatoria forms in the same order — taking the first entry is enough. */
    private Map<UUID, String> collectFormColumns(List<RankingEntryResult> entries) {
        Map<UUID, String> formNames = new LinkedHashMap<>();
        if (!entries.isEmpty()) {
            for (RankingFormScoreResult formScore : entries.get(0).formScores()) {
                formNames.put(formScore.formId(), formScore.formName());
            }
        }
        return formNames;
    }

    private List<String> buildHeaderRow(Set<String> categoryColumns, Map<UUID, String> formColumns) {
        List<String> header = new ArrayList<>(List.of(
                messages.get("export.ranking.header.name"),
                messages.get("export.ranking.header.email"),
                messages.get("export.ranking.header.status"),
                messages.get("export.ranking.header.rank"),
                messages.get("export.ranking.header.total_score"),
                messages.get("export.ranking.header.classification")));
        header.addAll(categoryColumns);
        header.addAll(formColumns.values());
        return header;
    }

    private List<String> buildDataRow(RankingEntryResult entry, Set<String> categoryColumns, Map<UUID, String> formColumns) {
        List<String> row = new ArrayList<>(List.of(
                entry.name(),
                entry.email(),
                entry.status(),
                entry.rank() != null ? String.valueOf(entry.rank()) : "",
                formatScore(entry.totalScore()),
                entry.classification() != null ? entry.classification().name() : ""));

        Map<String, Double> scoresByCategory = entry.scoresByCategory() != null ? entry.scoresByCategory() : Map.of();
        for (String category : categoryColumns) {
            row.add(formatScore(scoresByCategory.get(category)));
        }

        // Built with a plain loop, not Collectors.toMap: RankingFormScoreResult.score() is null
        // for a form the candidate hasn't completed yet, and HashMap.merge() (used internally by
        // toMap's collector) throws NPE on a null value — HashMap.put() accepts it fine.
        Map<UUID, Double> scoresByForm = new HashMap<>();
        for (RankingFormScoreResult formScore : entry.formScores()) {
            scoresByForm.put(formScore.formId(), formScore.score());
        }
        for (UUID formId : formColumns.keySet()) {
            row.add(formatScore(scoresByForm.get(formId)));
        }

        return row;
    }

    private String formatScore(Double value) {
        return value == null ? "" : String.format(Locale.ROOT, "%.1f", value);
    }
}
