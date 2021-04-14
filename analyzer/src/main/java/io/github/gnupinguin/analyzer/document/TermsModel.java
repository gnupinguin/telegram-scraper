package io.github.gnupinguin.analyzer.document;

import java.io.Serializable;

public interface TermsModel extends Serializable {

    int termIdentifier(String word);

}
