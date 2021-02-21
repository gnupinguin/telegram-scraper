package io.github.gnupinguin.tlgscraper.tlgservice.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TlgLoginProperties {

    private String phoneNumber;

    private String authCodeFile;

}
