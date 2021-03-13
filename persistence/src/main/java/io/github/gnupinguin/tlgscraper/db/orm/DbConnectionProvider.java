package io.github.gnupinguin.tlgscraper.db.orm;

import java.sql.Connection;

public interface DbConnectionProvider {

    Connection getConnection();

}
