package com.nickd.builder;

import org.semanticweb.owlapi.model.IRI;

public class Constants {

    public static String BASE = "https://nickdrummond.github.io/star-wars-ontology/ontologies";

    public static final IRI SUGGESTIONS_BASE = IRI.create(BASE + "/suggestions.owl.ttl");

    public static final String UTIL_BASE = "https://nickdrummond.github.io/star-wars-ontology/util";

    public static final String EDITOR_LABEL = "editorLabel";

    public static final String LEGACY_ID = "legacyId";

    public static final String DEFAULT_CLASSES_ONT = "base.owl.ttl";

    public static final String DEFAULT_INDIVIDUALS_ONT = "star-wars.owl.ttl";

    public static final String BREADCRUMB = " > ";

    public static final String PROMPT = " >> ";

    public static final String DEFAULT_LANG = "en";
    public static final String CACHES = "caches/";
    public static final int PROMPT_DEPTH = 3;
    public static final int TRUNCATE_LENGTH = 29;
    public static final int MAX_BEFORE_TRUNCATE = 30;
}
