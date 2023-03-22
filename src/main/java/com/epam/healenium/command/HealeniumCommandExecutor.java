package com.epam.healenium.command;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpResponseCodec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

@Slf4j
public class HealeniumCommandExecutor extends HttpCommandExecutor {

    private final String sessionId;
    private final Map<String, Object> sessionCapabilities;

    public HealeniumCommandExecutor(URL addressOfRemoteServer, String sessionId, Map<String, Object> sessionCapabilities) {
        super(addressOfRemoteServer);
        this.sessionId = sessionId;
        this.sessionCapabilities = sessionCapabilities;
    }

    @Override
    public Response execute(Command command) throws IOException {
        if (!DriverCommand.NEW_SESSION.equals(command.getName())) {
            return super.execute(command);
        }
        Response response = new Response();
        response.setSessionId(sessionId);
        response.setStatus(0);
        response.setValue(sessionCapabilities);
        updateCodec();
        return response;
    }

    protected void updateCodec() {
        try {
            Field commandCodec;
            commandCodec = this.getClass().getSuperclass().getDeclaredField("commandCodec");
            commandCodec.setAccessible(true);
            commandCodec.set(this, new W3CHttpCommandCodec());

            Field responseCodec;
            responseCodec = this.getClass().getSuperclass().getDeclaredField("responseCodec");
            responseCodec.setAccessible(true);
            responseCodec.set(this, new W3CHttpResponseCodec());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error during update codec. Message: {}, Exception: {}", e.getMessage(), e);
        }
    }
}
