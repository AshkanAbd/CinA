package ir.ashkanabd.cina.database;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class DataBaseCallbacks<A> implements AsyncCallback<A> {

    private HandleResponse handleResponse;
    private HandleFault handleFault;

    public DataBaseCallbacks(HandleResponse handleResponse, HandleFault handleFault) {
        this.handleResponse = handleResponse;
        this.handleFault = handleFault;
    }

    @Override
    public void handleResponse(A response) {
        if (handleResponse != null)
            handleResponse.handleResponse(response);
    }

    @Override
    public void handleFault(BackendlessFault fault) {
        if (handleFault != null)
            handleFault.handleFault(fault);
    }

    public void setHandleFault(HandleFault handleFault) {
        this.handleFault = handleFault;
    }

    public void setHandleResponse(HandleResponse handleResponse) {
        this.handleResponse = handleResponse;
    }

    public interface HandleResponse {
        void handleResponse(Object o);
    }

    public interface HandleFault {
        void handleFault(BackendlessFault fault);
    }
}
