package io.github.gnupinguin.tlgscraper.tlgservice.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TlgClientProperties {

    private String databaseDirectory;

    private boolean useMessageDatabase;

    private boolean useSecretChats;

    private int apiId;

    private String apiHash;

    private String systemLanguageCode;

    private String deviceModel;

    private String systemVersion;

    private String applicationVersion;

    private boolean enableStorageOptimizer;

}
