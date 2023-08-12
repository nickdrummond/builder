#Builder

## Status

Purely an experiment in navigating/building ontologies in a command line interface

## Features?

* Pluggable commands
* Search ability
* New entity creation
* Axiom creation
* Referencing / wikiPage
* Autocompletion ?
* Undo
* History / Context (scope)



## TODO

* Complete parser for all axiom types
* Axiom show - order referenced entities in MOS order (if possible)
* ont (searchterm)
* 

## Bugs
### Accidentally give the wrong placeholder:
    2… > Ferrix > Ferrix (112) > Free_Trade_sector >> new Sector &1 https://starwars.fandom.com/wiki/Free_Trade_sector
    3… > Ferrix (112) > Free_Trade_sector > ontologies#&1 >> show
    0) ontologies#&1 Type Sector...
### Cannot parse Subclass Axiom
    add Verlo_Skiff ?SubClassOf? hadRole some (Police and inOrganisation value Pre-Mor_Security_Inspection_team)

## Thoughts

### Autocompletion
       >> add Han_S lov Leia
       0) Birth_of_Han_Solo
       1) Han_Solo
       2) Rescue_of_Han_Solo
       add ?Han_S? lov Leia >> 1
       0) Leia_Organa
       1) Leia_vs_Jabba
       2) Rescue_of_Leia
       3) Kidnapping_of_Leia
       ...
       11) Bail_asks_for_help_finding_Leia
       add Han_Solo loved ?Leia? >> 0
       Han_Solo loved Leia_Organa >> show
       0) loved
       1) Han_Solo
       2) Leia_Organa
       Han_Solo loved Leia_Organa > entities >>



         >> add Han_Solo loved bleagh
        add Han_Solo loved ?? >> find Chewb
        0) Chewbacca
       1) Birth_of_Chewbacca
       2) Boushh_trades_Chewbacca
       3) Rescue_of_Chewbacca
       add Han_Solo loved ?? > find Chewb >> 0
       Han_Solo loved Chewbacca >>

Can't use autocomplete unless its for final symbol as it either needs to be c+p to edit or automatically
runs again which causes problems:

    ont
    0
    find Planet
    new &0 Ferrix https://starwars.fandom.com/wiki/Ferrix
    wiki &0 suggest
    new System &2
    <
    new &0 Kenari https://starwars.fandom.com/wiki/Kenari
    wiki &0 suggest
    <
    add &0 hasT?
    0
    %(@£$%($£%£%$) 
    add &0 Type hasTerrain some Forest // did nothing
    save


### New from suggestion
Should just be able to select the number without the extras - each suggestion should be a **set of axioms**
Its also wrong as the label should be without _underscores

suggestions.owl?

    >> wiki Ferrix	suggest
    https://starwars.fandom.com/wiki/Ferrix
    0) Canon
    1) Free_Trade_sector
    2) Morlani_system
    ...
    Ferrix (112) >> new Sector &1 https://starwars.fandom.com/wiki/Free_Trade_sector
    Ferrix (112) > Free_Trade_sector >>

Context will have to be more sophisticated!!
