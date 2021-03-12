package io.github.gnupinguin.tlgscraper.db.pool;

import java.sql.Connection;

public interface DbConnectionProvider {

    Connection getConnection();

}
