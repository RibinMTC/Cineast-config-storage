package org.vitrivr.cineast.core.features;

import org.apache.commons.lang3.tuple.Triple;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveProviderComparator;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;

import java.util.*;
import java.util.stream.Collectors;

public class SortedByAestheticScoreRetriever extends BooleanRetriever {

    private static final List<RelationalOperator> SUPPORTED_OPERATORS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            RelationalOperator.BETWEEN,
                            RelationalOperator.EQ,
                            RelationalOperator.NEQ,
                            RelationalOperator.GEQ,
                            RelationalOperator.GREATER,
                            RelationalOperator.LEQ,
                            RelationalOperator.LESS,
                            RelationalOperator.IN));

    private final HashMap<String, PrimitiveTypeProvider> minimumMap = new HashMap<>();
    private final HashMap<String, PrimitiveTypeProvider> maximumMap = new HashMap<>();


    protected SortedByAestheticScoreRetriever(String entity, Collection<String> attributes) {
        super(entity, attributes);
    }

    public SortedByAestheticScoreRetriever(LinkedHashMap<String, String> properties) {
        super(properties);
    }

    public PrimitiveTypeProvider getMinimum(String column){
        if (this.attributes.contains(column) && !this.minimumMap.containsKey(column)){
            populateExtremaMap();
        }
        return minimumMap.get(column);
    }

    public PrimitiveTypeProvider getMaximum(String column){
        if (this.attributes.contains(column) && !this.maximumMap.containsKey(column)){
            populateExtremaMap();
        }
        return maximumMap.get(column);
    }

    private void populateExtremaMap() {

        PrimitiveProviderComparator comparator = new PrimitiveProviderComparator();

        for (String column : this.attributes) {
            List<PrimitiveTypeProvider> col = this.selector.getAll(column);
            if (col.isEmpty()) {
                continue;
            }
            PrimitiveTypeProvider min = col.get(0), max = col.get(0);
            for (PrimitiveTypeProvider t : col) {
                if (comparator.compare(t, min) < 0) {
                    min = t;
                }
                if (comparator.compare(max, t) > 0) {
                    max = t;
                }
            }
            this.minimumMap.put(column, min);
            this.maximumMap.put(column, max);
        }
    }

    @Override
    protected Collection<RelationalOperator> getSupportedOperators() {
        return SUPPORTED_OPERATORS;
    }

    @Override
    protected List<ScoreElement> getMatching(List<BooleanExpression> expressions, ReadableQueryConfig qc) {
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRowsAND(expressions.stream().map(be -> Triple.of(be.getAttribute().contains(this.entity) ? be.getAttribute().substring(this.entity.length() + 1) : be.getAttribute(), be.getOperator(), be.getValues())).collect(Collectors.toList()), "id", Collections.singletonList("id"), qc);

        List<Map<String, PrimitiveTypeProvider>> features = selector.getRowsAND(expressions.stream().map(be -> Triple.of(be.getAttribute().contains(this.entity) ? be.getAttribute().substring(this.entity.length() + 1) : be.getAttribute(), be.getOperator(), be.getValues())).collect(Collectors.toList()), "id", Collections.singletonList("feature"), qc);
        int rowsCount = rows.size();
        List<ScoreElement> scoreElements = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            scoreElements.add(new SegmentScoreElement(rows.get(i).get("id").getString(), features.get(i).get("feature").getDouble()/10d));
        }

        return scoreElements;
    }
}
