package io.github.gnupinguin.tlgscraper.db.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbProperties {

    private String url;

    private String username;

    private String password;

}
