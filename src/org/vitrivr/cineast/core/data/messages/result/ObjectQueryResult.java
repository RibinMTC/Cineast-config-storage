package org.vitrivr.cineast.core.data.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageTypes;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class ObjectQueryResult extends AbstractQueryResultMessage<MultimediaObjectDescriptor> {

    /**
     *
     * @param queryId
     * @param content
     */
    @JsonCreator
    public ObjectQueryResult(String queryId, List<MultimediaObjectDescriptor> content) {
        super(queryId, content);
    }

    /**
     *
     * @return
     */
    public MessageTypes getMessagetype() {
        return MessageTypes.QR_OBJECT;
    }
}