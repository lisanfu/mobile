package com.example.hp.mobile.internal;

/**
 * Created by hp on 2016/6/25.
 */
public interface ITelephony {
    boolean endCall();
    void answerRingingCall();

    /**
     * Allow mobile data connections.
     */
    boolean enableDataConnectivity();

    /**
     * Disallow mobile data connections.
     */
    boolean disableDataConnectivity();

    /**
     * Report whether data connectivity is possible.
     */
    boolean isDataConnectivityPossible();
}
