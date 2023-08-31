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
* detect file changes and allow reload
* 

## Bugs
### Accidentally give the wrong placeholder:
    2… > Ferrix > Ferrix (112) > Free_Trade_sector >> new Sector &1 https://starwars.fandom.com/wiki/Free_Trade_sector
    3… > Ferrix (112) > Free_Trade_sector > ontologies#&1 >> show
    0) ontologies#&1 Type Sector...
### Cannot markup parse error for Type Axiom if incomplete (works if parses)
	8) Jedi_Temple_Guard
	9) Guard
     find Pre > Verlo_Skiff > find Gua >> <
     find Pre > Verlo_Skiff >> + &0 Type hadRole some (Guard and inOr
     find Pre > Verlo_Skiff > + Verlo_Skiff ?? hadRole some (Guard and inOrganisation >>

It's when there are brackets!!

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

suggestions.owl

     >> wiki https://starwars.fandom.com/wiki/Kassa_(episode) suggest
       https://starwars.fandom.com/wiki/Kassa_(episode)
       0) Maarva_Carassi_Andor
       1) B2EMO
       2) Brasso
       3) Bix_Caleen
       4) Femmi
       5) Gani
       6) Hyne
       7) Jeef_(Wobani)
       8) Jezzi
       9) Timm_Karlo
       10) Syril_Karn
       11) Kerri
       12) Leisure_Zone_brothel_doorman
       13) Nurchi
       14) Salman_Paak
       15) Wilmon_Paak
       16) Pegla
       17) Luthen_Rael
       18) Rashi
       19) Unidentified_Alpha_leader
       20) Unidentified_bartender_(Morlana_One)
       21) Unidentified_brothel_hostess
       22) Unidentified_Pre-Mor_officer_1
       23) Unidentified_Pre-Mor_officer_2
       24) Unidentified_Pre-Mor_officer_3
       25) Vetch
       26) Zorby
       27) Free_Trade_sector
       28) Morlani_system
       29) Andor_household
       30) Caleen_Salyard
       31) Carl's
       32) Cavo's
       33) Ferrix_bell_tower
       34) Ferrix_Depot
       35) Gavalox_Communications
       36) Greyble_Service
       37) Gyo_(business)
       38) Hotel_Bridge
       39) North_Stairs_Lane
       40) Rix_Road
       41) Repaak_Salyard
       42) Unidentified_administration_building
       43) Unidentified_chair_store
       44) Unidentified_droid_store
       45) Unidentified_droid_parts_store
       46) Unidentified_hotel_(Ferrix)
       47) Unidentified_screen_store
       48) Zorby's_Western_Shiplot
       49) Corporate_Security_Headquarters
       50) Leisure_Zone
       51) Leisure_Zone_brothel
       52) Preox-Morlana_Corporate_Zone
       53) Morlana_Four
       54) Mid_Rim_Territories
       55) Kenari_system
       56) Kenari_Village
       57) Tahina
       58) Republic_Era
       59) Republican_Shipwreck_of_Kenari
       60) Starship
       61) Breon_Dayvan
       62) Tac_Corvette
       63) D-85_freighter
       64) Light_freighter
       65) VCX_series
       66) Andor_family_hauler
       67) KGZ-54_Starcrane
       68) Lancer-class_pursuit_craft
       69) Orlean_Star_Cab
       70) Transport
       71) Mobile_Tac-Pod
       72) Transpo_corsair
       73) Unidentified_transport_(Corellia)
       74) WTK-85A_interstellar_transport
       75) Steelpecker
       76) Dog
       77) Corellian_hound
       78) Luggabeast
       79) Rat
       80) Groundmech_salvage_assist_unit
       81) Service_droid
       82) L-1g_general_purpose_droid
       83) Abednedo
       84) Anomid
       85) Candovantan
       86) Caspus_Pillar's_species
       87) Didynon
       88) Euphaus_Biro's_species
       89) Frigosian
       90) Hoogenz's_species
       91) Kenari_(people)
       92) Kakala
       93) Ongidae
       94) Scrapjaw_Motito's_species
       95) Silvasu_Fi's_species
       96) Urodel
       97) Blaster
       98) Blaster_pistol
       99) Bryar_pistol
       100) MW-20_Bryar_pistol
       101) Clem_Andor's_blaster_pistol
       102) Blowgun
       103) Computer
       104) Datapad
       105) Electro-shock_prod
       106) Engine
       107) Headset
       108) Melee_weapon
       109) Staff
       110) scan_processor
       111) Imperial_N-S9_Starpath_Unit
       112) Alien
       113) Blood
       114) Bone
       115) Brothel
       116) Chobb
       117) Clothing
       118) Boot
       119) Glove
       120) Goggles
       121) Hat
       122) Jacket
       123) Crystal
       124) Cyborg
       125) Death
       126) Eye
       127) Foodstuff
       128) Nog_(beverage)
       129) Noodle
       130) Glowblue_noodles
       131) Revnog
       132) Fuel
       133) Hand-to-hand_combat
       134) Imperial_crest
       135) Jungle
       136) Language
       137) Galactic_Basic_Standard
       138) Kenari_(language)
       139) Writing
       140) Aurebesh
       141) Unidentified_Aurebesh_script_(Jedha)
       142) Bazeese_script
       143) Futhork
       144) Laser
       145) Currency
       146) Credit
       147) Moon
       148) Music
       149) Necklace
       150) Parade
       151) Parent
       152) Planet
       153) Reproduction
       154) Salvage_yard
       155) Sleep
       156) Time
       157) Standard_day
       158) Standard_month
       159) Standard_week
       160) Standard_year
       161) City
       162) Urine
       163) Water
       164) Viewport
       165) Youngling
   https://starwars.fandom.com/w… >> show 0
   https://starwars.fandom.com/w… >> show &0
        0) Maarva_Carassi_Andor Type Living_thing
        1) Maarva_Carassi_Andor editorLabel "Maarva_Carassi_Andor"
        2) Maarva_Carassi_Andor seeAlso "https://starwars.fandom.com/wiki/Maarva_Carassi_Andor"^^anyURI
        3) Maarva_Carassi_Andor legacyId "-526189388"
        4) Maarva_Carassi_Andor label "Maarva Carassi Andor"@en
        https://starwars.fandom.com/w… > axioms >> 

Need to have 2 commands

accept &0 -> copies instances to star-wars.owl and classes to base.owl

specialise Human -> removes "Maarva_Carassi_Andor Type Living_thing" and adds "Maarva_Carassi_Andor Type Human"