package org.vitrivr.cineast.core.features;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.AestheticPredictorConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.BooleanSegmentScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CombinedAestheticMetricsRetriever implements Retriever {

    private static final Logger LOGGER = LogManager.getLogger();
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

    protected DBSelector selector;
    protected final List<String> entities;

    public static <T, C extends Collection<T>> C findIntersection(C newCollection,
                                                                  Collection<Collection<T>> collections) {
        boolean first = true;
        for (Collection<T> collection : collections) {
            if (first) {
                newCollection.addAll(collection);
                first = false;
            } else {
                newCollection.retainAll(collection);
            }
        }
        return newCollection;
    }

    public CombinedAestheticMetricsRetriever(LinkedHashMap<String, String> properties) {
        if (!properties.containsKey("entities")) {
            throw new RuntimeException("no entity specified in properties map of BooleanRetriever");
        }

        this.entities = Arrays.stream(properties.get("entities").split(",")).map(String::trim)
                .collect(
                        Collectors.toList());
    }

    public CombinedAestheticMetricsRetriever() {
        List<AestheticPredictorConfig> aestheticPredictorConfigs = AestheticPredictorsConfigStorage.getInstance().getAestheticPredictorsConfig();
        entities = new ArrayList<>();
        for (AestheticPredictorConfig aestheticPredictorConfig :
                aestheticPredictorConfigs) {
            entities.add(aestheticPredictorConfig.getTableName());
        }
    }

    protected boolean canProcess(BooleanExpression be) {
        return SUPPORTED_OPERATORS.contains(be.getOperator());
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        List<BooleanExpression> relevantExpressions = sc.getBooleanExpressions().stream().filter(this::canProcess).collect(Collectors.toList());
        if (relevantExpressions.isEmpty()) {
            LOGGER.debug("No relevant expressions in {} for query {}", this.getClass().getSimpleName(), sc.toString());
            return Collections.emptyList();
        }

        return getMatching(relevantExpressions, qc);
    }

    protected List<ScoreElement> getMatching(List<BooleanExpression> expressions, ReadableQueryConfig qc) {

        List<Collection<String>> allRowsIds = new ArrayList<>();

        for (String entity : entities) {
            selector.open(entity);
            ArrayList<BooleanExpression> entityRelevantExpressions = new ArrayList<>();
            for (BooleanExpression be :
                    expressions) {
                if (be.getAttribute().contains(entity)) {
                    entityRelevantExpressions.add(be);
                }
            }
            if (entityRelevantExpressions.size() == 0)
                continue;
            List<Map<String, PrimitiveTypeProvider>> rows = selector.getRowsAND(entityRelevantExpressions.stream().map(be -> Triple.of(be.getAttribute().substring(entity.length() + 1), be.getOperator(), be.getValues())).collect(Collectors.toList()), "id", Collections.singletonList("id"), qc);
            selector.close();
            if (rows.size() > 0)
                allRowsIds.add(new ArrayList<>(rows.stream().map(row -> row.get("id").getString()).collect(Collectors.toList())));
            else
                return Collections.emptyList();
        }

        int numOfLists = allRowsIds.size();

        if (numOfLists > 1) {
            Set<String> intersection = findIntersection(new HashSet<>(), allRowsIds);
            return intersection.stream().map(BooleanSegmentScoreElement::new).collect(Collectors.toList());
        } else if (numOfLists == 1)
            return allRowsIds.get(0).stream().map(BooleanSegmentScoreElement::new).collect(Collectors.toList());
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        return Collections.emptyList();
    }

    @Override
    public void finish() {
        this.selector.close();
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        //nop
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        //nop
    }
}
