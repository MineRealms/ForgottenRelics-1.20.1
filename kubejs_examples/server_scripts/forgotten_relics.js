// Example KubeJS integration for Forgotten Relics (1.20.1 port).
//
// The 1.7.10 original exposed MineTweaker ZenScript classes:
//   mods.forgottenrelics.JusticeHandler.addTrigger(key, stack)
//   mods.forgottenrelics.JusticeHandler.obliterateJusticeTriggers(key)
//   mods.forgottenrelics.Research.setHidden(key, bool)
//   mods.forgottenrelics.Research.setLost(key, bool)
//   mods.forgottenrelics.Research.obliterateDefaultTriggers(key)
//
// In this port:
//  - justice triggers are edited through the `ForgottenRelics` script binding;
//  - research flags/parents/triggers are data-pack JSON, so they are edited
//    through KubeJS `ServerEvents.highPriorityData` using the same file ids
//    (this is persistent and reload-safe, unlike the old runtime mutation).
//
// Place this file in: <minecraft>/kubejs/server_scripts/

ServerEvents.highPriorityData(event => {
    // Equivalent of Research.obliterateDefaultTriggers('ELDRITCHSPELL') plus
    // making the entry permanently hidden: replace the research entry JSON.
    // Replace the id below with a real entry from
    // data/forgottenrelics/research/*.json (or any other mod's research).
    /*
    event.addJson('thaumcraft:research/eldritch_spell', {
        key: 'thaumcraft:ELDRITCHSPELL',
        category: 'thaumcraft:ELDRITCH',
        flags: ['HIDDEN', 'CONCEALED'],
        object_triggers: [],
        entity_triggers: [],
        aspect_triggers: [],
        // ... copy the remaining fields from the original entry
    })
    */
})

// Justice handler triggers (server script).
// Equivalent of JusticeHandler.addTrigger / obliterateJusticeTriggers.
ForgottenRelics.obliterateJusticeTriggers('forgottenrelics:EldritchSpell')
ForgottenRelics.addJusticeTrigger('forgottenrelics:EldritchSpell', 'minecraft:nether_star')

// Inspect what is currently registered:
// console.info(JSON.stringify(ForgottenRelics.forgottenKnowledge()))
