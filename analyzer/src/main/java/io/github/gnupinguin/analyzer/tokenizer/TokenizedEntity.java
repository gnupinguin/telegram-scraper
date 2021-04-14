package io.github.gnupinguin.analyzer.tokenizer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenizedEntity<T extends Serializable> implements Serializable {

    private List<String> tokens;

    private T entity;

}
