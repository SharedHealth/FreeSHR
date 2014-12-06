package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.UUID;

public class EncounterSearchResponseFeedConverter extends AbstractHttpMessageConverter<EncounterSearchResponse> {

    public EncounterSearchResponseFeedConverter() {
        super(new MediaType("application", "atom+xml"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return EncounterSearchResponse.class.equals(clazz);
    }

    @Override
    protected EncounterSearchResponse readInternal(Class<? extends EncounterSearchResponse> clazz,
                                                   HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {
        return null; //we never read
    }

    @Override
    protected void writeInternal(EncounterSearchResponse result, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        Feed resultFeed = new EncounterFeedHelper().generateFeed(result, UUID.randomUUID().toString());
        String wireFeedEncoding = resultFeed.getEncoding();
        if (!StringUtils.hasLength(wireFeedEncoding)) {
            wireFeedEncoding = "UTF-8";
        }
        MediaType contentType = outputMessage.getHeaders().getContentType();
        if (contentType != null) {
            Charset wireFeedCharset = Charset.forName(wireFeedEncoding);
            contentType = new MediaType(contentType.getType(), contentType.getSubtype(), wireFeedCharset);
            outputMessage.getHeaders().setContentType(contentType);
        }

        WireFeedOutput feedOutput = new WireFeedOutput();

        try {
            Writer writer = new OutputStreamWriter(outputMessage.getBody(), wireFeedEncoding);
            feedOutput.output(resultFeed, writer);
        } catch (FeedException ex) {
            throw new HttpMessageNotWritableException("Could not write WiredFeed: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return mediaType.equals(MediaType.APPLICATION_ATOM_XML) && Charsets.UTF_8.equals(mediaType.getCharSet());
    }
}
