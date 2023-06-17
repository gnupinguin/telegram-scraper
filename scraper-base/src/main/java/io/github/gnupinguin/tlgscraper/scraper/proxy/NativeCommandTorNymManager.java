package io.github.gnupinguin.tlgscraper.scraper.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;

@Slf4j
public class NativeCommandTorNymManager implements TorNymManager {

    private final String changeNymCommand;

    public NativeCommandTorNymManager(TorConfiguration torConfiguration) {
        this.changeNymCommand = String.format("(echo authenticate '%s'; echo signal newnym; echo quit) " +
                        "| nc %s %d", torConfiguration.password(), torConfiguration.host(), torConfiguration.controlPort());
    }

    @Override
    public boolean nextNode() {
        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", changeNymCommand).start();
            process.waitFor();
            String result = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
            return result.startsWith("250 OK");
        } catch (Exception e) {
            log.info("Can not update tor node", e);
        }
        return false;
    }

}
