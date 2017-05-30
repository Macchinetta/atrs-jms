/*
 * Copyright(c) 2014-2017 NTT Corporation.
 */
package jp.co.ntt.atrs.listener.common.error;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import org.terasoluna.gfw.common.exception.ExceptionLogger;

/**
 * JMSリスナーで発生した例外を処理する例外ハンドラ
 * @author NTT 電電次郎
 */
@Component
public class AtrsJmsErrorHandler implements ErrorHandler {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AtrsJmsErrorHandler.class);

    /**
     * ExceptionLogger
     */
    @Inject
    ExceptionLogger exceptionLogger;

    /**
     * JMS処理に失敗した事をログ出力して終了する。
     * @param t 発生例外
     */
    @Override
    public void handleError(Throwable t) {

        // Listenerでの例外はListenerExecutionFailedExceptionにwrapされている為、その原因例外を処理ログ出力する
        if (t instanceof ListenerExecutionFailedException) {
            Throwable wrappedException = t.getCause();
            if (wrappedException instanceof Exception) {
                exceptionLogger.error((Exception) wrappedException);
            } else {
                LOGGER.error("UNEXPECTED ERROR", wrappedException);
            }
        } else if (t instanceof Exception) {
            exceptionLogger.error((Exception) t);
        } else {
            LOGGER.error("UNEXPECTED ERROR", t);
        }
    }

}
