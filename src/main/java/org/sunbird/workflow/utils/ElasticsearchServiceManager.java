package org.sunbird.workflow.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sunbird.workflow.config.Constants;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class ElasticsearchServiceManager {
    Logger logger = LogManager.getLogger(ElasticsearchServiceManager.class);

    @Value("${sunbird_user_index}")
    private String sbUserIndex;

    @Autowired
    private RestHighLevelClient client;

    public static final String _DOC = "_doc";

    public boolean upsertWfRequest(String userId, List<String> wfRequests) {
        try {
            // Create or update the document
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.WF_REQUESTS_KEY, wfRequests);

            UpdateRequest updateRequest = new UpdateRequest(sbUserIndex, _DOC, userId)
                    .doc(updates)
                    .upsert(new IndexRequest(sbUserIndex).id(userId).source(updates));

            client.update(updateRequest, RequestOptions.DEFAULT);
            logger.info("WfRequests Upsert successful for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to upsert wfRequests for userId: {}", userId, e);
            return false;
        }
        return true;
    }

    /**
     * Removes a specific wfRequest from the wfRequests list in user_alias index.
     */
    public boolean removeWfRequest(String userId, String wfRequestToRemove) {
        try {
            // Fetch the existing document
            GetResponse getResponse = client.get(new org.elasticsearch.action.get.GetRequest(sbUserIndex, _DOC, userId),
                    RequestOptions.DEFAULT);

            if (!getResponse.isExists()) {
                logger.error("Failed to find user information from ES for Id: {}", userId);
                return false;
            }

            Map<String, Object> source = getResponse.getSourceAsMap();
            List<String> wfRequests = (List<String>) source.get(Constants.WF_REQUESTS_KEY);

            if (wfRequests == null || !wfRequests.contains(wfRequestToRemove)) {
                logger.error("WFRequests object is empty or doesn't contain wf_id: {} for user: {}", wfRequestToRemove,
                        userId);
                return false;
            }

            // Remove the request
            wfRequests.remove(wfRequestToRemove);
            source.put(Constants.WF_REQUESTS_KEY, wfRequests);

            // Update document
            UpdateRequest updateRequest = new UpdateRequest(sbUserIndex, _DOC, userId).doc(source);
            client.update(updateRequest, RequestOptions.DEFAULT);

            logger.info("wfRequest with id: {} is removed successfully from user: {}", wfRequestToRemove, userId);
        } catch (IOException e) {
            logger.error("Failed to remove wf_id: {} from WFRequests for user: {}", wfRequestToRemove, userId, e);
            return false;
        }
        return true;
    }

    public List<String> searchUsers(String queryString, String rootOrgId, int from, int size) {
        List<String> result = new ArrayList<String>();

        try {
            // Construct the search request
            SearchRequest searchRequest = new SearchRequest(sbUserIndex);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.from(from);
            sourceBuilder.size(size);

            // Build the bool query
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            SimpleQueryStringBuilder simpleQuery = QueryBuilders.simpleQueryStringQuery(queryString)
                    .defaultOperator(org.elasticsearch.index.query.Operator.OR)
                    .analyzeWildcard(false)
                    .autoGenerateSynonymsPhraseQuery(true)
                    .fuzzyPrefixLength(0)
                    .fuzzyMaxExpansions(50)
                    .fuzzyTranspositions(true)
                    .boost(1.0f);

            boolQuery.must(simpleQuery);

            boolQuery.must(QueryBuilders.existsQuery(Constants.WF_REQUESTS_KEY));

            if (StringUtils.isNotBlank(rootOrgId)) {
                boolQuery.filter(QueryBuilders.termQuery(Constants.ROOT_ORG_ID_RAW_KEY, rootOrgId));
            }

            sourceBuilder.query(boolQuery);

            sourceBuilder.fetchSource(
                    new String[] { Constants.ID },
                    new String[] {});

            searchRequest.source(sourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = (Map<String, Object>) hit.getSourceAsMap();
                result.add((String) sourceMap.get(Constants.ID));
            }
        } catch (IOException e) {
            logger.error("Failed to query ES to find records for given query.", e);
        }
        return result;
    }
}
