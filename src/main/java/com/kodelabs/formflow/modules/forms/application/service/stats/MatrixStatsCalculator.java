package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixColumn;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixRow;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.MatrixCellStats;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.MatrixRowStats;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatrixStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.MATRIX;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        if (!(question.getConfig() instanceof MatrixConfig cfg)) {
            return new QuestionStatsResult(
                    question.getId(), question.getTitle(), type().code(),
                    totalResponses, 0, null, null, null, null, List.of());
        }

        List<MatrixRow> rows = cfg.getRows();
        List<MatrixColumn> columns = cfg.getColumns();

        // counts[rowId][columnId] = respondents who selected that column for that row
        Map<String, Map<String, Integer>> counts = new HashMap<>();
        Map<String, Integer> rowAnswerCounts = new HashMap<>();

        for (Object answer : answers) {
            if (!(answer instanceof Map<?, ?> matrix)) continue;
            for (MatrixRow row : rows) {
                Object selected = matrix.get(row.getId());
                if (selected instanceof String colId) {
                    counts.computeIfAbsent(row.getId(), k -> new HashMap<>())
                            .merge(colId, 1, Integer::sum);
                    rowAnswerCounts.merge(row.getId(), 1, Integer::sum);
                }
            }
        }

        int answered = (int) answers.stream().filter(a -> a instanceof Map<?, ?> m && !m.isEmpty()).count();

        List<MatrixRowStats> matrixRows = rows.stream().map(row -> {
            int rowTotal = rowAnswerCounts.getOrDefault(row.getId(), 0);
            Map<String, Integer> colCounts = counts.getOrDefault(row.getId(), Map.of());

            List<MatrixCellStats> cells = columns.stream().map(col -> {
                int count = colCounts.getOrDefault(col.getId(), 0);
                double pct = rowTotal == 0 ? 0.0 : count * 100.0 / rowTotal;
                return new MatrixCellStats(col.getId(), col.getLabel(), count, pct);
            }).toList();

            return new MatrixRowStats(row.getId(), row.getLabel(), cells);
        }).toList();

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, null, null, null, null, matrixRows);
    }
}
