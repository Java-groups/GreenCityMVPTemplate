package greencity.exception.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response object with information of occur exception.
 *
 * @author Marian Milian
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class ExceptionResponse {
    private String message;
    @JsonIgnore
    private String timeStamp;
    @JsonIgnore
    private String trace;
    @JsonIgnore
    private String path;

    /**
     * Constructor with parameters.
     */
    public ExceptionResponse(Map<String, Object> errorAttributes) {
        Object path = errorAttributes.get("path");
        Object message = errorAttributes.get("message");
        Object timestamp = errorAttributes.get("timestamp");
        Object trace = errorAttributes.get("trace");

        this.setPath(path != null ? path.toString() : null);
        this.setMessage(message != null ? message.toString() : null);
        this.setTimeStamp(timestamp != null ? timestamp.toString() : null);
        this.setTrace(trace != null ? trace.toString() : null);
    }
}
